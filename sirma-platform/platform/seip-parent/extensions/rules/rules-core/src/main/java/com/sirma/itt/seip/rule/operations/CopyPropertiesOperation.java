package com.sirma.itt.seip.rule.operations;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.rule.model.OnDuplicateConfig;
import com.sirma.itt.seip.rule.model.PropertyMapping;

/**
 * Rule instance operation that can copy properties from matched instance to the current instance.
 *
 * @author hlungov
 */
@Named(CopyPropertiesOperation.NAME)
public class CopyPropertiesOperation extends BaseRuleOperation {

	private static final Logger LOGGER = LoggerFactory.getLogger(CopyPropertiesOperation.class);

	public static final String NAME = "copyProperties";
	private static final String PROPERTY_MAPPING = PropertyMapping.NAME;
	private static final String ON_DUPLICATE = "onDuplicate";

	private Collection<PropertyMapping> propertyMapping;
	private OnDuplicateConfig onDuplicateConfig = new OnDuplicateConfig();
	private boolean isDisabled = false;
	private Operation operation;

	@Inject
	private InstanceService instanceService;

	@Override
	public String getPrimaryOperation() {
		return NAME;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean configure(Context<String, Object> configuration) {
		if (!super.configure(configuration)) {
			isDisabled = true;
			return false;
		}
		Collection<?> mappingConfig = configuration.getIfSameType(PROPERTY_MAPPING, Collection.class);
		if (mappingConfig == null) {
			LOGGER.warn(
					"{} will be disabled because no properties to match for are found in the configuration. Configuration is {}",
					NAME, configuration);
			isDisabled = true;
			return false;
		}
		propertyMapping = PropertyMapping.parse(mappingConfig);
		if (CollectionUtils.isEmpty(propertyMapping)) {
			// no mappings so nothing to do
			LOGGER.warn("{} will be disabled because mapping configuration found where invalid. Configuration is {}",
					NAME, configuration);
			isDisabled = true;
			return false;
		}
		operation = new Operation(configuration.getIfSameType(EVENT_ID, String.class));
		Map<String, Object> duplicateConf = configuration.getIfSameType(ON_DUPLICATE, Map.class);
		if (duplicateConf != null) {
			onDuplicateConfig = OnDuplicateConfig.parse(duplicateConf);
		}
		if (onDuplicateConfig != null && !propertyMapping.isEmpty()) {
			// if per mapping configuration is not defined then set the global configuration
			for (PropertyMapping mapping : propertyMapping) {
				if (mapping.getOnDuplicate() == null) {
					mapping.setOnDuplicate(onDuplicateConfig);
				}
			}
		}
		isDisabled = false;
		return true;
	}

	@Override
	public void processingStarted(Context<String, Object> processingContext, Context<String, Object> context) {
		Instance processedInstance = getProcessedInstance(processingContext);
		instanceService.refresh(processedInstance);
	}

	@Override
	public void processingEnded(Context<String, Object> processingContext, Context<String, Object> context) {
		try {
			Options.DISABLE_RULES.enable();
			if (operation.getOperation() == null) {
				Options.DISABLE_AUDIT_LOG.enable();
			}
			Instance processedInstance = getProcessedInstance(processingContext);
			instanceService.save(processedInstance, operation);
		} finally {
			Options.DISABLE_RULES.disable();
			if (operation.getOperation() == null) {
				Options.DISABLE_AUDIT_LOG.disable();
			}
		}
	}

	@Override
	public void execute(Context<String, Object> context, Instance matchedInstance,
			Context<String, Object> processingContext) {
		Instance processedInstance = getProcessedInstance(context);
		Map<String, Serializable> currentProcessedProperties = processedInstance.getProperties();
		Map<String, Serializable> matchedInstanceProperties = matchedInstance.getProperties();
		processOperation(currentProcessedProperties, matchedInstanceProperties);
	}

	/**
	 * Process operation.
	 *
	 * @param currentProcessedProperties
	 *            the properties from
	 * @param matchedInstanceProperties
	 *            the properties to
	 */
	private void processOperation(Map<String, Serializable> currentProcessedProperties,
			Map<String, Serializable> matchedInstanceProperties) {
		for (PropertyMapping mapping : propertyMapping) {
			switch (mapping.getOnDuplicate().getOperation()) {
				case CONCATENATE:
					concatenate(mapping, currentProcessedProperties, matchedInstanceProperties);
					break;
				case OVERRIDE:
					CollectionUtils.copyValueIfExist(matchedInstanceProperties, mapping.getFrom(),
							currentProcessedProperties, mapping.getTo());
					break;
				case SKIP:
					if (!currentProcessedProperties.containsKey(mapping.getTo())) {
						CollectionUtils.copyValueIfExist(matchedInstanceProperties, mapping.getFrom(),
								currentProcessedProperties, mapping.getTo());
					}
					break;
				default:
					break;
			}
		}
	}

	/**
	 * Concatenate.
	 *
	 * @param mapping
	 *            the mapping
	 * @param currentProcessedProperties
	 *            the properties from
	 * @param matchedInstanceProperties
	 *            the properties to
	 */
	@SuppressWarnings("unchecked")
	private static void concatenate(PropertyMapping mapping, Map<String, Serializable> currentProcessedProperties,
			Map<String, Serializable> matchedInstanceProperties) {
		Serializable valueToUpdate = currentProcessedProperties.get(mapping.getTo());
		Serializable valueToAdd = matchedInstanceProperties.get(mapping.getFrom());
		if (valueToUpdate == null) {
			valueToUpdate = "";
		}
		if (valueToAdd == null) {
			valueToAdd = "";
		}
		if (valueToAdd instanceof String && valueToUpdate instanceof String) {
			String newValue = concatenateString(mapping.getOnDuplicate(), (String) valueToAdd, (String) valueToUpdate);
			currentProcessedProperties.put(mapping.getTo(), newValue);
		} else if (valueToAdd instanceof Collection<?> && valueToUpdate instanceof Collection<?>) {
			mergeCollections(valueToUpdate, valueToAdd);
		} else if (valueToUpdate instanceof Collection<?>) {
			addValueToCollection((Collection<Serializable>) valueToUpdate, valueToAdd);
		} else {
			LOGGER.warn("Unsupported concatenation between {}/{}", valueToAdd, valueToUpdate);
		}
	}

	/**
	 * Concatenate string.
	 *
	 * @param duplicateConfig
	 *            the mapping
	 * @param valueToAdd
	 *            the value to add
	 * @param toValue
	 *            the to value
	 * @return the string
	 */
	private static String concatenateString(OnDuplicateConfig duplicateConfig, String valueToAdd, String toValue) {
		if (checkIfAlreadyPresent(valueToAdd, toValue)) {
			// if the target value currently contains the new value we should not add it
			return toValue;
		}
		String newTargetValue = StringUtils.trimToEmpty(toValue);
		String newValueToAdd = StringUtils.trimToEmpty(valueToAdd);

		int capacity = newTargetValue.length() + duplicateConfig.getSeparator().length() + newValueToAdd.length();
		StringBuilder builder = new StringBuilder(capacity);
		builder.append(newTargetValue);
		if (StringUtils.isNotBlank(newTargetValue) && StringUtils.isNotBlank(newValueToAdd)) {
			builder.append(duplicateConfig.getSeparator());
		}
		builder.append(newValueToAdd);
		return builder.toString();
	}

	/**
	 * Check if already present.
	 *
	 * @param valueToAdd
	 *            the value to add
	 * @param toValue
	 *            the to value
	 * @return true, if successful
	 */
	private static boolean checkIfAlreadyPresent(String valueToAdd, String toValue) {
		return Pattern.compile("\\b" + Pattern.quote(valueToAdd) + "\\b").matcher(toValue).find();
	}

	/**
	 * Adds the value to collection.
	 *
	 * @param propToValue
	 *            the prop to value
	 * @param propFromValue
	 *            the prop from value
	 */
	private static void addValueToCollection(Collection<Serializable> propToValue, Serializable propFromValue) {
		if (!propToValue.contains(propFromValue)) {
			propToValue.add(propFromValue);
		}
	}

	/**
	 * Merge collections.
	 *
	 * @param propToValue
	 *            the prop to value
	 * @param propFromValue
	 *            the prop from value
	 */
	@SuppressWarnings("unchecked")
	private static void mergeCollections(Serializable propToValue, Serializable propFromValue) {
		Collection<Serializable> targetCollection = (Collection<Serializable>) propToValue;
		Collection<Serializable> newCollection = (Collection<Serializable>) propFromValue;
		Set<Serializable> temp = new LinkedHashSet<>(newCollection);
		temp.removeAll(new HashSet<>(targetCollection));

		// add only new unique values
		targetCollection.addAll(temp);
	}

	@Override
	public boolean isApplicable(Context<String, Object> context) {
		return !isDisabled && super.isApplicable(context);
	}

	@Override
	public String getName() {
		return NAME;
	}

}
