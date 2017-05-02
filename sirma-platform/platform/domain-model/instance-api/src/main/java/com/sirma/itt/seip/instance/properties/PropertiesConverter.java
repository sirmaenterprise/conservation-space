package com.sirma.itt.seip.instance.properties;

import java.io.Serializable;
import java.util.Map;

import javax.json.JsonObject;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.PropertyModel;

/**
 * Properties converter that provides means for transforming properties from external model (REST) to internal model and
 * vice versa.
 *
 * @author BBonev
 * @deprecated This class does not produce valid model any more and should not be used. It's not removed because it's
 *             used in old classes used in UI1 and partially in some rests for UI2 that should be refactored/removed.
 */
@Deprecated
public interface PropertiesConverter {

	/**
	 * Converts the given property model for REST compatibility.
	 *
	 * @param model
	 *            the source model to convert.
	 * @param definitionModel
	 *            the definition model to use for conversion
	 * @return the result converted mapping.
	 */
	Map<String, ?> convertToExternalModel(PropertyModel model, DefinitionModel definitionModel);

	/**
	 * Converts the given properties that are from REST request for internal use. The method should not allow
	 * transferring properties that are not defined in the model
	 *
	 * @param source
	 *            the source properties to convert
	 * @param definitionModel
	 *            the definition model to use for conversion
	 * @return the result converted mapping.
	 */
	default Map<String, Serializable> convertToInternalModel(Map<String, ?> source, DefinitionModel definitionModel) {
		return convertToInternalModel(source, definitionModel, false);
	}

	/**
	 * Converts the given properties that are from REST request for internal use. The caller may choose if properties
	 * that are not defined in the model to be included in the result. For sync properties basic conversion will be done
	 * and no value keys will be changed.
	 *
	 * @param source
	 *            the source properties to convert
	 * @param definitionModel
	 *            the definition model to use for conversion
	 * @param allowNonModelProperties
	 *            if non model properties should be copied or not
	 * @return the result converted mapping.
	 */
	Map<String, Serializable> convertToInternalModel(Map<String, ?> source, DefinitionModel definitionModel,
			boolean allowNonModelProperties);

	/**
	 * Convert instance properties from json to the specified definition model. The method does not allow non model
	 * properties
	 *
	 * @param properties
	 *            json properties.
	 * @param definition
	 *            Instance definition containing the properties model.
	 * @return The converted properties.
	 * @see #fromJson(JsonObject, DefinitionModel, boolean)
	 */
	default Map<String, Serializable> fromJson(JsonObject properties, DefinitionModel definition) {
		return fromJson(properties, definition, false);
	}

	/**
	 * Convert instance properties from json to the specified definition model.
	 *
	 * @param properties
	 *            json properties.
	 * @param definition
	 *            Instance definition containing the properties model.
	 * @param allowNonModelProperties
	 *            if non model properties should be copied or not
	 * @return The converted properties.
	 */
	Map<String, Serializable> fromJson(JsonObject properties, DefinitionModel definition,
			boolean allowNonModelProperties);

	/**
	 * Convert instance properties to json.
	 * @param model Instance to convert.
	 * @param definition instance definition containing the properties model.
	 * @return A {@link JsonObject} containing the converted properties.
	 */
	JsonObject toJson(PropertyModel model, DefinitionModel definition);
}