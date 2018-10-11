package com.sirma.itt.cmf.alfresco4.services.convert;

import java.io.Serializable;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.Resettable;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * The Class DMSTypeConverter is custom converter for specific models. <br>
 * REVIEW: why not separate the converter into 2 one for CMF->DMS and one DMS->CMF so we can have more clear
 * implementation with common interface/abstract class
 *
 * @author BBonev
 * @author BBanchev
 */
public interface DMSTypeConverter extends Resettable {

	/** The Constant ALLOW_ALL. */
	FieldProcessor ALLOW_ALL = new FieldProcessorAllowedAll();
	/** The Constant ALLOW_ALL. */
	FieldProcessor PROPERTIES_MAPPING = new FieldProcessorFromMapping();
	/** The Constant EDITABLE_OR_MANDATORY_LEVEL. */
	FieldProcessor EDITABLE_HIDDEN_MANDATORY_LEVEL = new FieldProcessorWritable();
	/** The Constant EDITABLE_OR_MANDATORY_LEVEL. */
	FieldProcessor WORKFLOW_TASK_LEVEL = new FieldProcessorBPMWritable();
	/** The Constant ALLOW_WITH_PREFIX. */
	FieldProcessor ALLOW_WITH_PREFIX = new FieldProcessorAllowedAllWithPrefix();
	/** document version level. */
	FieldProcessor DOCUMENT_LEVEL = new FieldProcessorFromMapping() {
			@Override
			public String prepareDMSKey(String key) {
				// return the original key
				if (key.indexOf('-') == 0) {
					return key.substring(1);
				}
				return key;
			}
		};

	/**
	 * Filter properties by using json and provided model. Only model elements with requiered read level passes. Default
	 * Model is used to filter
	 *
	 * @param properties
	 *            the properties
	 * @param level
	 *            the level
	 * @return the map
	 */
	Map<String, Serializable> filterCMFProperties(Map<String, Serializable> properties, FieldProcessor level);

	/**
	 * Filter properties by using json and provided model. Only model elements with requiered read level passes.
	 *
	 * @param defModel
	 *            the model
	 * @param properties
	 *            the properties
	 * @param level
	 *            the level
	 * @return the map
	 */
	Map<String, Serializable> filterCMFProperties(DefinitionModel defModel, Map<String, Serializable> properties,
			FieldProcessor level);

	/**
	 * Filter properties by using map and provided model. Only model elements with requiered read level passes.
	 *
	 * @param defModel
	 *            the model
	 * @param properties
	 *            the properties
	 * @param level
	 *            the level
	 * @return the map
	 * @throws JSONException
	 *             the jSON exception
	 */
	Map<String, Serializable> filterDMSProperties(DefinitionModel defModel, Map<String, Serializable> properties,
			FieldProcessor level) throws JSONException;

	/**
	 * Filter properties.
	 *
	 * @param defModel
	 *            the model
	 * @param properties
	 *            the properties
	 * @param level
	 *            the level
	 * @return the map
	 * @throws JSONException
	 *             the jSON exception
	 */

	Map<String, Serializable> filterDMSProperties(DefinitionModel defModel, JSONObject properties, FieldProcessor level)
			throws JSONException;

	/**
	 * Convert cmf to dms property.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param processor
	 *            the processor
	 * @return the pair of converted key->value
	 */
	Pair<String, Serializable> convertCMFtoDMSProperty(String key, Serializable value, FieldProcessor processor);

	/**
	 * Convert cmf to dms property.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param instance
	 *            the instance to use as model base
	 * @param processor
	 *            the processor to use
	 * @return the pair of converted key->value
	 */
	Pair<String, Serializable> convertCMFtoDMSProperty(String key, Serializable value, Instance instance,
			FieldProcessor processor);

	/**
	 * Convert cmf to dms properties.
	 *
	 * @param properties
	 *            the sort args
	 * @param processor
	 *            the filter model
	 * @return the map
	 */
	Map<String, Serializable> convertCMFtoDMSProperties(Map<String, Serializable> properties, FieldProcessor processor);

	/**
	 * Convert cmf to dms properties.
	 *
	 * @param properties
	 *            the properties
	 * @param instance
	 *            the current instance element
	 * @param level
	 *            the level of processing
	 * @return the map of processed properties
	 */
	Map<String, Serializable> convertCMFtoDMSProperties(Map<String, Serializable> properties, Instance instance,
			FieldProcessor level);

	/**
	 * Convert cmf to dms properties.
	 *
	 * @param properties
	 *            the properties
	 * @param instance
	 *            the current instance element
	 * @param definition
	 *            the definition
	 * @param level
	 *            the level of processing
	 * @return the map of processed properties
	 */
	Map<String, Serializable> convertCMFtoDMSProperties(Map<String, Serializable> properties, Instance instance,
			DefinitionModel definition, FieldProcessor level);

	/**
	 * Convert dms to cmf properties.
	 *
	 * @param jsonObject
	 *            the json object
	 * @param instance
	 *            the task path element
	 * @param level
	 *            the level
	 * @return the map
	 * @throws JSONException
	 *             the jSON exception
	 */
	Map<String, Serializable> convertDMSToCMFProperties(JSONObject jsonObject, Instance instance, FieldProcessor level) throws JSONException;

	/**
	 * Convert dms to cmf properties.
	 *
	 * @param props
	 *            the props
	 * @param instance
	 *            the task path element
	 * @param level
	 *            the level
	 * @return the map
	 * @throws JSONException
	 *             the jSON exception
	 */
	Map<String, Serializable> convertDMSToCMFProperties(Map<String, Serializable> props, Instance instance,
			FieldProcessor level) throws JSONException;

	/**
	 * Convert dms to cmf properties.
	 *
	 * @param jsonObject
	 *            the json object
	 * @param processor
	 *            the processor
	 * @return the map
	 */
	Map<String, Serializable> convertDMSToCMFProperties(JSONObject jsonObject, FieldProcessor processor);

	/**
	 * Convert dms to cmf single property.
	 *
	 * @param dmsKey
	 *            is the dms key
	 * @param objectValue
	 *            is the value to set to the key
	 * @param level
	 *            is the required level processor. Most probably {@link #PROPERTIES_MAPPING}
	 * @return the pair of converted key, value or null if no mapping is available
	 */
	Pair<String, Serializable> convertDMSToCMFProperty(String dmsKey, Serializable objectValue, FieldProcessor level);

	/**
	 * Convert cmf to dms properties by value.<br>
	 * REVIEW: the name of the method should be changed to reflect that we a using the base model for conversion
	 *
	 * @param properties
	 *            the properties
	 * @param processor
	 *            the processor
	 * @return the map
	 */
	Map<String, Serializable> convertCMFtoDMSPropertiesByValue(Map<String, Serializable> properties,
			FieldProcessor processor);

	/**
	 * Convert dms to cmf properties by value - iteration is done over the value not the model itself
	 *
	 * @param properties
	 *            the properties
	 * @param instance
	 *            the instance
	 * @param level
	 *            the level
	 * @return the map
	 * @throws JSONException
	 *             the jSON exception
	 */
	Map<String, Serializable> convertDMStoCMFPropertiesByValue(JSONObject properties, Instance instance, FieldProcessor level) throws JSONException;

	/**
	 * Gets the model prefix.
	 *
	 * @return the model prefix
	 */
	String getModelPrefix();

}