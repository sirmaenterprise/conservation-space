package com.sirma.itt.seip.eai.service.model.transform;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.Resettable;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.eai.exception.EAIModelException;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty.EntityPropertyMapping;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * {@link EAIModelConverter} has methods for two-way conversion of data SEIP<->external. How mappings are obtained is
 * left on the implementing classes. Each converter could be accessed by its name {@link #getName()}. <br>
 * The responsibilities of implementation is to convert key and value including the codelist mapping.
 * 
 * @author bbanchev
 */
public interface EAIModelConverter extends Resettable, Plugin, Named {
	/** The plugin name. */
	String PLUGIN_ID = "EAIModelConverter";

	/**
	 * Convert SEIP property to external property using {@link EntityPropertyMapping#AS_DATA} mapping.
	 *
	 * @param key
	 *            the key is the property name. Key should be a valid uri
	 * @param value
	 *            the value is the property value
	 * @param definitionId
	 *            is the definition model to use
	 * @return the converted pair list that may contain 0 to * mappings for that key
	 * @throws EAIModelException
	 *             on any model conversion error
	 */
	List<Pair<String, Serializable>> convertSEIPtoExternalProperty(String key, Serializable value, String definitionId)
			throws EAIModelException;

	/**
	 * Convert external to SEIP properties compatible with the provided {@link Instance}.
	 *
	 * @param properties
	 *            the properties to convert. If value is not serialized a conversion to string is triggered
	 * @param instance
	 *            the instance that will consume the properties - might be null
	 * @return the map of converted properties with the seip field id
	 * @throws EAIModelException
	 *             on any model conversion error
	 */
	Map<String, Serializable> convertExternaltoSEIPProperties(Map<String, Object> properties, Instance instance)
			throws EAIModelException;

	/**
	 * Convert single external property and value to its internal representation
	 * 
	 * @param externalName
	 *            the external property name
	 * @param rawValue
	 *            the external value as received
	 * @param definitionId
	 *            the internal SEIP model definition id
	 * @return the converted property and value as pair
	 * @throws EAIModelException
	 *             on conversion error
	 */
	Pair<String, Serializable> convertExternaltoSEIPProperty(String externalName, Serializable rawValue,
			String definitionId) throws EAIModelException;

	/**
	 * Finds a field filtered by specified predicated for given instance definition
	 * 
	 * @param definitionId
	 *            is the definition id to check in
	 * @param predicate
	 *            is the filter predicate
	 * @return the first element in the stream that is filtered out
	 */
	Optional<PropertyDefinition> findInternalFieldForType(String definitionId, Predicate<PropertyDefinition> predicate);
}