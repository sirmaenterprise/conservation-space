package com.sirma.sep.model.management.deploy.semantic;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.domain.semantic.persistence.MultiLanguageValue;
import com.sirma.itt.seip.domain.validation.ValidationMessage;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.validation.InstanceValidationResult;
import com.sirma.itt.seip.instance.validation.InstanceValidationService;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.sep.model.management.deploy.configuration.ModelManagementDeploymentConfigurations;

/**
 * Deployer encapsulating the logic for validating and deploying of {@link com.sirma.sep.model.management.ModelClass} as {@link Instance}
 * via {@link DomainInstanceService}.
 *
 * @author Mihail Radkov
 */
@Singleton
public class SemanticClassDeployer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Operation EDIT = new Operation(ActionTypeConstants.EDIT_DETAILS);
	private static final String TYPE_MISMATCH = "Instance id={} property={} was expected to be of multi language type";

	@Inject
	private DomainInstanceService domainInstanceService;
	@Inject
	private InstanceValidationService instanceValidationService;
	@Inject
	private ModelManagementDeploymentConfigurations deploymentConfigurations;

	List<ValidationMessage> validate(SemanticClassDeploymentPayload payload) {
		Instance instance = domainInstanceService.loadInstance(payload.getClassId());

		List<ValidationMessage> validationMessages = new LinkedList<>();

		List<ValidationMessage> propertyValidation = validateInstanceProperties(instance, payload);
		validationMessages.addAll(propertyValidation);

		applyPayload(instance, payload);

		ValidationContext validationContext = new ValidationContext(instance, EDIT);
		InstanceValidationResult validationResult = instanceValidationService.validate(validationContext);

		List<ValidationMessage> instanceValidation = convertValidation(instance, validationResult);
		validationMessages.addAll(instanceValidation);

		return validationMessages;
	}

	void deploy(SemanticClassDeploymentPayload payload) {
		Instance instance = domainInstanceService.loadInstance(payload.getClassId());

		applyPayload(instance, payload);

		InstanceSaveContext saveContext = InstanceSaveContext.create(instance, EDIT);
		saveContext.put("SEMANTIC_PERSISTENCE_CONTEXT", deploymentConfigurations.getSemanticContext().get());
		domainInstanceService.save(saveContext);
	}

	private List<ValidationMessage> validateInstanceProperties(Instance instance, SemanticClassDeploymentPayload payload) {
		SemanticClassValidationMessageBuilder messageBuilder = new SemanticClassValidationMessageBuilder(instance);

		Map<String, Serializable> properties = instance.getProperties();
		payload.getToRemove().forEach((key, values) -> values.forEach(value -> {
			String propertyId = resolveClassPropertyId(key, payload);
			if (properties.containsKey(propertyId)) {
				if (value instanceof LanguagePair) {
					validateMultilangProperty(instance, propertyId, (LanguagePair) value, messageBuilder);
				} else if (!instance.containsValue(propertyId, value)) {
					LOGGER.warn("Instance id={} with property={} is missing value={}", instance.getId(), propertyId, value);
					messageBuilder.missingValue(propertyId, value);
				}
			}
		}));

		return messageBuilder.getMessages();
	}

	private static void validateMultilangProperty(Instance instance, String propertyId, LanguagePair value,
			SemanticClassValidationMessageBuilder messageBuilder) {
		Serializable currentValue = instance.get(propertyId);
		if (currentValue instanceof MultiLanguageValue) {
			List<String> existingLabels = ((MultiLanguageValue) currentValue).getValues(value.getLanguage()).collect(Collectors.toList());
			if (!existingLabels.isEmpty()) {
				boolean match = existingLabels.stream().anyMatch(l -> l.equals(value.getLabel()));
				if (!match) {
					LOGGER.warn("Instance id={} with property={} and lang={} is missing label={}", instance.getId(), propertyId,
							value.getLanguage(), value.getLabel());
					messageBuilder.missingLabel(propertyId, value.getLanguage(), value.getLabel());
				}
			}
		} else {
			// TODO: Remove the loggers ?
			LOGGER.warn(TYPE_MISMATCH, instance.getIdentifier(), propertyId);
			messageBuilder.propertyTypeMismatch(propertyId);
		}
	}

	private void applyPayload(Instance instance, SemanticClassDeploymentPayload payload) {
		payload.getToRemove().forEach((uri, values) -> values.forEach(value -> {
			String property = resolveClassPropertyId(uri, payload);
			if (value instanceof LanguagePair) {
				removeMultiLanguageValue(instance, property, (LanguagePair) value);
			} else {
				instance.remove(property, value);
			}
		}));

		payload.getToAdd().forEach((uri, values) -> values.forEach(value -> {
			String property = resolveClassPropertyId(uri, payload);
			if (value instanceof LanguagePair) {
				addMultiLanguageValue(instance, property, (LanguagePair) value);
			} else {
				instance.add(property, value);
			}
		}));
	}

	private static String resolveClassPropertyId(String propertyUri, SemanticClassDeploymentPayload payload) {
		if (payload.getClassMetaInfo().containsKey(propertyUri)) {
			return payload.getClassMetaInfo().get(propertyUri).getId();
		}
		throw new IllegalArgumentException("Missing meta info for class property " + propertyUri);
	}

	private static void removeMultiLanguageValue(Instance instance, String propertyId, LanguagePair value) {
		Serializable currentValue = instance.get(propertyId);
		if (currentValue instanceof MultiLanguageValue) {
			((MultiLanguageValue) currentValue).removeValue(value.getLanguage());
		} else {
			// Handling a case where the could types differ
			LOGGER.warn(TYPE_MISMATCH, instance.getIdentifier(), propertyId);
			instance.remove(propertyId, value.getLabel());
		}
	}

	private static void addMultiLanguageValue(Instance instance, String propertyId, LanguagePair value) {
		String lang = value.getLanguage();
		String label = value.getLabel();
		Serializable currentValue = instance.getProperties().computeIfAbsent(propertyId, p -> new MultiLanguageValue());
		if (currentValue instanceof MultiLanguageValue) {
			((MultiLanguageValue) currentValue).addValue(lang, label);
		} else if (currentValue != null) {
			// Handling a case where types could differ
			// Theoretically it should be removed by removeMultiLanguageValue before trying to add it
			LOGGER.warn(TYPE_MISMATCH, instance.getIdentifier(), propertyId);
			instance.add(propertyId, label);
		}
	}

	private static List<ValidationMessage> convertValidation(Instance instance, InstanceValidationResult validationResult) {
		return validationResult.getErrorMessages()
				.stream()
				.map(PropertyValidationError::toString)
				.map(errorMessage -> ValidationMessage.error(instance.getIdentifier(), errorMessage))
				.collect(Collectors.toList());
	}
}