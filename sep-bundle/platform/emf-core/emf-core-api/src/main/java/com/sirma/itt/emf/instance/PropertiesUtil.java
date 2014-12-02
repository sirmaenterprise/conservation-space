package com.sirma.itt.emf.instance;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.model.ControlDefinition;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.evaluation.ExpressionContext;
import com.sirma.itt.emf.evaluation.ExpressionContextProperties;
import com.sirma.itt.emf.evaluation.ExpressionsManager;
import com.sirma.itt.emf.exceptions.TypeConversionException;
import com.sirma.itt.emf.instance.model.CommonInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.model.PropertyModel;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.PathHelper;

/**
 * Utility class for common method for working with instance properties.
 *
 * @author BBonev
 */
public class PropertiesUtil {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtil.class);

	/**
	 * Instantiates a new properties util.
	 */
	private PropertiesUtil() {
		// utility class
	}

	/**
	 * Clean null properties from the given model.
	 * 
	 * @param <E>
	 *            the element type
	 * @param model
	 *            the model
	 * @return the e
	 */
	public static <E extends PropertyModel> E cleanNullProperties(E model) {
		for (Iterator<Entry<String, Serializable>> it = model.getProperties().entrySet().iterator(); it
				.hasNext();) {
			Entry<String, Serializable> entry = it.next();
			if ((entry.getValue() == null)
					|| ((entry.getValue() instanceof String) && entry.getValue().equals("null"))) {
				it.remove();
			}
		}
		return model;
	}

	/**
	 * Shallow-copies to a new map except for maps and collections that are binary serialized.
	 *
	 * @param original
	 *            the original map to copy
	 * @return a copy of the source map
	 */
	public static Map<String, Serializable> cloneProperties(Map<String, Serializable> original) {
		return cloneProperties(original, Collections.<String> emptySet());
	}

	/**
	 * Shallow-copies to a new map except for maps and collections that are binary serialized.
	 * 
	 * @param original
	 *            the original map to copy
	 * @param skipKeys
	 *            the skip keys
	 * @return a copy of the source map
	 */
	public static Map<String, Serializable> cloneProperties(Map<String, Serializable> original,
			String... skipKeys) {
		return cloneProperties(original, skipKeys == null ? Collections.<String> emptySet()
				: new LinkedHashSet<String>(Arrays.asList(skipKeys)));
	}

	/**
	 * Shallow-copies to a new map except for maps and collections that are binary serialized.
	 * 
	 * @param original
	 *            the original map to copy
	 * @param skipKeys
	 *            the skip keys
	 * @return a copy of the source map
	 */
	public static Map<String, Serializable> cloneProperties(Map<String, Serializable> original,
			Set<String> skipKeys) {
		// Copy the values, ensuring that any collections are copied as well
		Map<String, Serializable> copy = CollectionUtils.createLinkedHashMap(original.size());
		for (Map.Entry<String, Serializable> element : original.entrySet()) {
			String key = element.getKey();
			if (skipKeys.contains(key)) {
				continue;
			}
			Serializable value = element.getValue();
			if ((value instanceof Collection<?>) || (value instanceof Map<?, ?>)) {
				value = (Serializable) SerializationUtils.deserialize(SerializationUtils
						.serialize(value));
			} else if (value instanceof CommonInstance) {
				// the only way to guarantee the object separation is via
				// cloning of the object
				value = ((CommonInstance) value).clone();
			}
			copy.put(key, value);
		}
		return copy;
	}

	/**
	 * Populates the given {@link PropertyModel} instance with default values evaluated from the
	 * given collection of property definitions. The method provides option to override or not the
	 * existing values in the {@link PropertyModel}. The methods builds complex properties also.
	 * 
	 * @param <E>
	 *            the property definition type
	 * @param model
	 *            the model to update
	 * @param fields
	 *            the fields to process
	 * @param evaluatorManager
	 *            the evaluator manager to use for evaluation
	 * @param overrideExisting
	 *            if <code>true</code> to override existing properties in the model.
	 */
	public static <E extends PropertyDefinition> void populateProperties(PropertyModel model,
			Collection<E> fields, ExpressionsManager evaluatorManager,
			boolean overrideExisting) {
		Map<String, Serializable> properties = model.getProperties();
		if (properties == null) {
			properties = new LinkedHashMap<>();
			model.setProperties(properties);
		}

		ExpressionContext context = evaluatorManager.createDefaultContext((Instance) model, null,
				null);
		for (PropertyDefinition fieldDefinition : fields) {
			// does not override filled properties, but we want to repopulate
			// with the default
			Serializable currentValue = properties.get(fieldDefinition.getName());
			if (!overrideExisting && (currentValue != null)) {
				continue;
			}
			Serializable value = null;
			if (DataTypeDefinition.INSTANCE.equals(fieldDefinition.getType())) {
				// Instance types must have a control definition!
				if (fieldDefinition.getControlDefinition() == null) {
					LOGGER.warn("Instance type fields must have a control definition to be instantiated: "
							+ fieldDefinition.getIdentifier());
					continue;
				}
				ControlDefinition controlDefinition = fieldDefinition.getControlDefinition();
				String name = fieldDefinition.getName();
				String path = PathHelper.getPath(controlDefinition);
				CommonInstance instance = new CommonInstance(name, model.getRevision(), path);
				SequenceEntityGenerator.generateStringId(instance, true);

				populateProperties(instance, controlDefinition.getFields(), evaluatorManager,
						overrideExisting);
				value = instance;
			} else {
				try {
					context.put(ExpressionContextProperties.TARGET_FIELD,
							(Serializable) fieldDefinition);
					value = evaluatorManager.evaluate(fieldDefinition, context);
				} catch (TypeConversionException e) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Failed to convert " + fieldDefinition.getDefaultValue()
								+ " to " + fieldDefinition.getDataType().getJavaClassName(), e);
					} else {
						LOGGER.warn("Failed to convert " + fieldDefinition.getDefaultValue()
								+ " to " + fieldDefinition.getDataType().getJavaClassName());
					}
				}
			}
			if ((value != null) || Boolean.TRUE.equals(fieldDefinition.isPreviewEnabled())) {
				properties.put(fieldDefinition.getName(), value);
			}
		}
	}

	/**
	 * Evaluate default properties for the given definition model.
	 * 
	 * @param model
	 *            the model
	 * @param currentInstance
	 *            the current instance
	 * @param manager
	 *            the manager
	 * @return the map
	 */
	public static Map<String, Serializable> evaluateDefaultPropertiesForModel(
			DefinitionModel model,
			Instance currentInstance, ExpressionsManager manager) {
		if ((model == null) || (currentInstance == null)) {
			return Collections.emptyMap();
		}
		ExpressionContext context = manager.createDefaultContext(currentInstance, null, null);
		Map<String, Serializable> map = new LinkedHashMap<>();
		map.putAll(evaluateDefaultValues(model.getFields(), currentInstance.getRevision(), manager,
				context));
		if (model instanceof RegionDefinitionModel) {
			for (RegionDefinition definition : ((RegionDefinitionModel) model).getRegions()) {
				map.putAll(evaluateDefaultValues(definition.getFields(),
						currentInstance.getRevision(), manager, context));
			}
		}
		return map;
	}

	/**
	 * Evaluates all default values for the given collection of property definitions and returns a
	 * mapping of evaluated values. If default value is missing then entry in the result will be
	 * missing for that field.
	 *
	 * @param fields
	 *            the collection of fields to evaluate.
	 * @param revision
	 *            the revision to use when creating new {@link CommonInstance} when building complex
	 *            instances.
	 * @param evaluatorManager
	 *            the evaluator manager to use for evaluations
	 * @param context
	 *            the context to use for the evaluations. Note the current instance is recommended
	 *            to be passed in the context.
	 * @return the map of evaluated properties.
	 */
	public static Map<String, Serializable> evaluateDefaultValues(
			Collection<PropertyDefinition> fields, Long revision,
			ExpressionsManager evaluatorManager, ExpressionContext context) {
		Map<String, Serializable> result = CollectionUtils.createLinkedHashMap(fields.size());

		evaluateDefaultValuesInternal(fields, revision, evaluatorManager, result, context);

		return result;
	}

	/**
	 * Evaluate default values internal.
	 *
	 * @param <E>
	 *            the element type
	 * @param fields
	 *            the fields
	 * @param revision
	 *            the revision
	 * @param evaluatorManager
	 *            the evaluator manager
	 * @param properties
	 *            the properties
	 * @param context
	 *            the context to use when evaluating expressions
	 */
	private static <E extends PropertyDefinition> void evaluateDefaultValuesInternal(
			Collection<E> fields, Long revision, ExpressionsManager evaluatorManager,
			Map<String, Serializable> properties, ExpressionContext context) {

		for (E fieldDefinition : fields) {
			// does not override filled properties, but we want to repopulate
			// with the default
			Serializable value = null;
			if (DataTypeDefinition.INSTANCE.equals(fieldDefinition.getType())) {
				// Instance types must have a control definition!
				if (fieldDefinition.getControlDefinition() == null) {
					LOGGER.warn("Instance type fields must have a control definition to be instantiated: "
							+ fieldDefinition.getIdentifier());
					continue;
				}
				ControlDefinition controlDefinition = fieldDefinition.getControlDefinition();
				String name = fieldDefinition.getName();
				String path = PathHelper.getPath(controlDefinition);
				CommonInstance instance = new CommonInstance(name, revision, path);
				SequenceEntityGenerator.generateStringId(instance, true);

				// populate properties of the newly created common instance
				evaluateDefaultValuesInternal(controlDefinition.getFields(), revision,
						evaluatorManager, instance.getProperties(), context);
				value = instance;
			} else {
				try {
					context.put(ExpressionContextProperties.TARGET_FIELD,
							(Serializable) fieldDefinition);
					value = evaluatorManager.evaluate(fieldDefinition, context);
				} catch (TypeConversionException e) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Failed to convert " + fieldDefinition.getDefaultValue()
								+ " to " + fieldDefinition.getDataType().getJavaClassName(), e);
					} else {
						LOGGER.warn("Failed to convert " + fieldDefinition.getDefaultValue()
								+ " to " + fieldDefinition.getDataType().getJavaClassName());
					}
				}
			}
			if (value != null) {
				properties.put(fieldDefinition.getName(), value);
			}
		}
	}

	/**
	 * Merge properties from the source map to the destination map. If the value of the source and
	 * destination map is of type {@link PropertyModel} then model properties will be merged also.
	 * The method copies missing properties from the source to the destination but could also
	 * override all properties from the source map if the argument {@code overrideExisting} is equal
	 * to <code>true</code>.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param source
	 *            the source map to get properties from
	 * @param destination
	 *            the destination map to write the result
	 * @param overrideExisting
	 *            if <code>true</code> the source will be copied to the destination map if full.
	 * @return true, if the destination map has been modified in any way.
	 */
	public static <K, V> boolean mergeProperties(Map<K, V> source, Map<K, V> destination,
			boolean overrideExisting) {
		if ((source == null) || (destination == null)) {
			// nothing to do
			return false;
		}
		boolean modified = false;
		for (Entry<K, V> entry : source.entrySet()) {
			V destValue = destination.get(entry.getKey());
			V sourceValue = entry.getValue();
			if ((destValue instanceof PropertyModel) && (sourceValue instanceof PropertyModel)) {
				modified |= mergeProperties(((PropertyModel) sourceValue).getProperties(),
						((PropertyModel) destValue).getProperties(), overrideExisting);
			} else if ((destValue == null) || overrideExisting) {
				destination.put(entry.getKey(), sourceValue);
				modified = true;
			}
		}
		return modified;
	}
}
