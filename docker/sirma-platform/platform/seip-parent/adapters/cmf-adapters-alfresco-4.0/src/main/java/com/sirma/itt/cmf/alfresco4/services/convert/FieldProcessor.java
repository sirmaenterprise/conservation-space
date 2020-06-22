package com.sirma.itt.cmf.alfresco4.services.convert;

import java.io.Serializable;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;

/**
 * The {@link FieldProcessor} is abstraction used as delegate for converting in specific situations.
 */
public abstract class FieldProcessor {

	/**
	 * Instantiates a new field processor.
	 */
	public FieldProcessor() {
	}

	/** The Constant FORBIDDEN. */
	protected static final String FORBIDDEN = DefaultProperties.NOT_USED_PROPERTY_VALUE;

	/**
	 * Checks for required level.
	 *
	 * @param definition
	 *            the definition
	 * @return true, if successful
	 */
	public boolean hasRequiredLevel(PropertyDefinition definition) {
		if (definition == null) {
			return false;
		}
		return true;
	}

	/**
	 * Checks for required level.
	 *
	 * @param definition
	 *            the definition
	 * @return true, if successful
	 */
	public boolean hasRequiredReadLevel(PropertyDefinition definition) {
		if (definition == null) {
			return false;
		}
		return true;
	}

	/**
	 * Process passed.
	 *
	 * @param definitionn
	 *            the definitionn
	 * @param value
	 *            the value
	 * @param convertor
	 *            the convertor
	 * @return the pair
	 */
	public abstract Pair<String, Serializable> processToDMSPassed(PropertyDefinition definitionn, Serializable value,
			DMSTypeConverterImpl convertor);

	/**
	 * Process skipped.
	 *
	 * @param definition
	 *            the definition
	 * @param value
	 *            the value
	 * @param convertor
	 *            the convertor
	 * @return the pair
	 */
	public Pair<String, Serializable> processToDMSSkipped(PropertyDefinition definition, Serializable value,
			DMSTypeConverter convertor) {
		return Pair.NULL_PAIR;
	}

	/**
	 * Process to cmf passed.
	 *
	 * @param definition
	 *            the definition
	 * @param jsonValue
	 *            the json value
	 * @param dmsTypeConvertor
	 *            the dms type convertor
	 * @return the pair
	 */
	public Pair<String, Serializable> processToCMFPassed(PropertyDefinition definition, Serializable jsonValue,
			DMSTypeConverterImpl dmsTypeConvertor) {
		return dmsTypeConvertor.convertDMSToCMFPropertyInernal(definition, jsonValue);
	}

	/**
	 * Process to cmf skipped.
	 *
	 * @param definition
	 *            the definition
	 * @param jsonValue
	 *            the json value
	 * @param dmsTypeConvertor
	 *            the dms type convertor
	 * @return the pair
	 */
	public Pair<String, Serializable> processToCMFSkipped(PropertyDefinition definition, Serializable jsonValue,
			DMSTypeConverter dmsTypeConvertor) {

		// return dmsTypeConvertor.convertDMSToCMFProperty(definition,
		// jsonValue);
		return Pair.NULL_PAIR;
	}

	/**
	 * Process no definition.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param dmsTypeConvertor
	 *            the dms type convertor
	 * @return the pair
	 */
	public Pair<String, Serializable> processToDmsNoDefinition(String key, Serializable value,
			DMSTypeConverterImpl dmsTypeConvertor) {
		return Pair.NULL_PAIR;
	}

	/**
	 * Process dms to cmf no definition.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param dmsTypeConvertor
	 *            the dms type convertor
	 * @return the pair
	 */
	public Pair<String, Serializable> processToCmfNoDefinition(String key, Serializable value,
			DMSTypeConverterImpl dmsTypeConvertor) {
		return Pair.NULL_PAIR;
	}

	/**
	 * Prepare dms key.
	 *
	 * @param key
	 *            the key
	 * @return the processed key
	 */
	public String prepareDMSKey(String key) {
		return key;
	}

	/**
	 * Prepare cmf key.
	 *
	 * @param key
	 *            the key
	 * @return the processed key
	 */
	public String prepareCMFKey(String key) {
		return key;
	}
}
