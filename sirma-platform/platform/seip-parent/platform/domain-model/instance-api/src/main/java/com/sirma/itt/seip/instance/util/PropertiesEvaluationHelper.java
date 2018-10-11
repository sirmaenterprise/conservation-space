package com.sirma.itt.seip.instance.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.convert.TypeConversionException;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.RegionDefinitionModel;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionContextProperties;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.instance.CommonInstance;

/**
 * Utility class for common method for working with instance properties.
 *
 * @author BBonev
 */
public class PropertiesEvaluationHelper {

	private static final String FAILED_TO_CONVERT_TO = "Failed to convert {} to {}";
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesEvaluationHelper.class);
	/**
	 * Instantiates a new properties util.
	 */
	private PropertiesEvaluationHelper() {
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
		return PropertiesUtil.cleanNullProperties(model);
	}

	/**
	 * Shallow-copies to a new map except for maps and collections that are binary serialized.
	 *
	 * @param original
	 *            the original map to copy
	 * @return a copy of the source map
	 */
	public static Map<String, Serializable> cloneProperties(Map<String, Serializable> original) {
		return PropertiesUtil.cloneProperties(original);
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
	public static Map<String, Serializable> cloneProperties(Map<String, Serializable> original, String... skipKeys) {
		return PropertiesUtil.cloneProperties(original, skipKeys);
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
	public static Map<String, Serializable> cloneProperties(Map<String, Serializable> original, Set<String> skipKeys) {
		return PropertiesUtil.cloneProperties(original, skipKeys);
	}

	/**
	 * Populates the given {@link PropertyModel} instance with default values evaluated from the given collection of
	 * property definitions. The method provides option to override or not the existing values in the
	 * {@link PropertyModel}. The methods builds complex properties also.
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
	 * @param idManager
	 *            the id manager
	 */
	public static <E extends PropertyDefinition> void populateProperties(PropertyModel model, Collection<E> fields,
			ExpressionsManager evaluatorManager, boolean overrideExisting, DatabaseIdManager idManager) {
		Map<String, Serializable> properties = model.getProperties();
		if (properties == null) {
			properties = new LinkedHashMap<>();
			model.setProperties(properties);
		}

		ExpressionContext context = evaluatorManager.createDefaultContext((Instance) model, null, null);
		for (PropertyDefinition fieldDefinition : fields) {
			// does not override filled properties, but we want to repopulate
			// with the default
			Serializable currentValue = properties.get(fieldDefinition.getName());
			if (!overrideExisting && currentValue != null) {
				continue;
			}
			Serializable value = evaluateValue(evaluatorManager, overrideExisting, context, fieldDefinition, idManager);
			if (value == null && PropertyDefinition.hasType(DataTypeDefinition.BOOLEAN).test(fieldDefinition)) {
				properties.put(fieldDefinition.getName(), Boolean.FALSE);
			} else if (value != null || Boolean.TRUE.equals(fieldDefinition.isPreviewEnabled())) {
				properties.put(fieldDefinition.getName(), value);
			}
		}
	}

	/**
	 * Evaluate value.
	 *
	 * @param evaluatorManager
	 *            the evaluator manager
	 * @param overrideExisting
	 *            the override existing
	 * @param context
	 *            the context
	 * @param fieldDefinition
	 *            the field definition
	 * @param idManager
	 *            the id manager
	 * @return the serializable
	 */
	private static Serializable evaluateValue(ExpressionsManager evaluatorManager,
			boolean overrideExisting, ExpressionContext context, PropertyDefinition fieldDefinition,
			DatabaseIdManager idManager) {
		if (DataTypeDefinition.INSTANCE.equals(fieldDefinition.getType())) {
			ControlDefinition controlDefinition = fieldDefinition.getControlDefinition();
			CommonInstance instance = createCommonInstanceFromControl(controlDefinition, fieldDefinition, idManager);
			populateProperties(instance, controlDefinition != null ? controlDefinition.getFields() : Collections.emptyList() , evaluatorManager, overrideExisting, idManager);
			return instance;
		} else {
			return evaluateFieldExpression(evaluatorManager, context, fieldDefinition);
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
	 * @param idManager
	 *            the id manager
	 * @return the map
	 */
	public static Map<String, Serializable> evaluateDefaultPropertiesForModel(DefinitionModel model,
			Instance currentInstance, ExpressionsManager manager, DatabaseIdManager idManager) {
		if (model == null || currentInstance == null) {
			return new HashMap<>();
		}
		ExpressionContext context = manager.createDefaultContext(currentInstance, null, null);
		Map<String, Serializable> map = new LinkedHashMap<>();
		map.putAll(evaluateDefaultValues(model.getFields(), manager, context, idManager));
		if (model instanceof RegionDefinitionModel) {
			for (RegionDefinition definition : ((RegionDefinitionModel) model).getRegions()) {
				map.putAll(evaluateDefaultValues(definition.getFields(), manager, context, idManager));
			}
		}
		return map;
	}

	/**
	 * Evaluates all default values for the given collection of property definitions and returns a mapping of evaluated
	 * values. If default value is missing then entry in the result will be missing for that field.
	 *
	 * @param fields
	 *            the collection of fields to evaluate.
	 * @param evaluatorManager
	 *            the evaluator manager to use for evaluations
	 * @param context
	 *            the context to use for the evaluations. Note the current instance is recommended to be passed in the
	 *            context.
	 * @param idManager
	 *            the id manager
	 * @return the map of evaluated properties.
	 */
	public static Map<String, Serializable> evaluateDefaultValues(Collection<PropertyDefinition> fields, ExpressionsManager evaluatorManager,
			ExpressionContext context, DatabaseIdManager idManager) {
		Map<String, Serializable> result = CollectionUtils.createLinkedHashMap(fields.size());

		evaluateDefaultValuesInternal(fields, evaluatorManager, result, context, idManager);

		return result;
	}

	/**
	 * Evaluate default values internal.
	 *
	 * @param <E>
	 *            the element type
	 * @param fields
	 *            the fields
	 * @param evaluatorManager
	 *            the evaluator manager
	 * @param properties
	 *            the properties
	 * @param context
	 *            the context to use when evaluating expressions
	 * @param idManager
	 *            the id manager
	 */
	private static <E extends PropertyDefinition> void evaluateDefaultValuesInternal(Collection<E> fields,
			ExpressionsManager evaluatorManager, Map<String, Serializable> properties, ExpressionContext context,
			DatabaseIdManager idManager) {

		for (E fieldDefinition : fields) {
			// does not override filled properties, but we want to repopulate
			// with the default
			Serializable value = null;
			if (DataTypeDefinition.INSTANCE.equals(fieldDefinition.getType())) {
				ControlDefinition controlDefinition = fieldDefinition.getControlDefinition();
				if (controlDefinition != null) {
					// if not null instance won't be null too
					CommonInstance instance = createCommonInstanceFromControl(controlDefinition, fieldDefinition, idManager);
					// populate properties of the newly created common instance
					evaluateDefaultValuesInternal(controlDefinition.getFields(), evaluatorManager,instance.getProperties(), context, idManager);
					value = instance;
				}
			} else {
				value = evaluateFieldExpression(evaluatorManager, context, fieldDefinition);
			}
			if (value != null) {
				properties.put(fieldDefinition.getName(), value);
			}
		}
	}

	/**
	 * Creates the common instance from control.
	 *
	 * @param <E>
	 *            the element type
	 * @param controlDefinition
	 *            the control definition
	 * @param fieldDefinition
	 *            the field definition
	 * @param idManager
	 *            the id manager
	 * @return the common instance
	 */
	private static <E extends PropertyDefinition> CommonInstance createCommonInstanceFromControl(
			ControlDefinition controlDefinition, E fieldDefinition, DatabaseIdManager idManager) {
		// Instance types must have a control definition!
		if (controlDefinition == null) {
			LOGGER.warn("Instance type fields must have a control definition to be instantiated: "
					+ fieldDefinition.getIdentifier());
			return null;
		}
		String name = fieldDefinition.getName();
		String path = PathHelper.getPath(controlDefinition);
		CommonInstance instance = new CommonInstance(name, path);
		idManager.generateStringId(instance, true);
		return instance;
	}

	/**
	 * Evaluate field expression.
	 *
	 * @param <E>
	 *            the element type
	 * @param evaluatorManager
	 *            the evaluator manager
	 * @param context
	 *            the context
	 * @param fieldDefinition
	 *            the field definition
	 * @return the serializable
	 */
	private static <E extends PropertyDefinition> Serializable evaluateFieldExpression(
			ExpressionsManager evaluatorManager, ExpressionContext context, E fieldDefinition) {
		try {
			context.put(ExpressionContextProperties.TARGET_FIELD, (Serializable) fieldDefinition);
			return evaluatorManager.evaluate(fieldDefinition, context);
		} catch (TypeConversionException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(FAILED_TO_CONVERT_TO, fieldDefinition.getDefaultValue(),
						fieldDefinition.getDataType().getJavaClassName(), e);
			} else {
				LOGGER.warn(FAILED_TO_CONVERT_TO, fieldDefinition.getDefaultValue(),
						fieldDefinition.getDataType().getJavaClassName());
			}
		}
		return null;
	}

}
