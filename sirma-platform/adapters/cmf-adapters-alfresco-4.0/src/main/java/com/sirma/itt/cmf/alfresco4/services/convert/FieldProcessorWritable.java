package com.sirma.itt.cmf.alfresco4.services.convert;

import java.io.Serializable;
import java.util.Date;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;

/**
 * The Class FieldProcessorWritable.
 */
public class FieldProcessorWritable extends FieldProcessor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasRequiredLevel(PropertyDefinition definition) {
		if (definition == null || definition.getDmsType().indexOf("-") == 0
				|| FORBIDDEN.equals(definition.getDmsType())) {
			return false;
		}
		return true;
		// return (definition.getDisplayType() == DisplayType.EDITABLE ||
		// definition.isMandatory());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pair<String, Serializable> processToDMSSkipped(PropertyDefinition definition, Serializable value,
			DMSTypeConverter convertor) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pair<String, Serializable> processToDMSPassed(PropertyDefinition definition, Serializable value,
			DMSTypeConverterImpl convertor) {
		if (definition.getDmsType() == null || FORBIDDEN.equals(definition.getDmsType())
				|| definition.getDmsType().indexOf("-") == 0) {
			return Pair.NULL_PAIR;
		}

		// keep the property and converted it
		if (value instanceof Date) {
			return new Pair<String, Serializable>(definition.getDmsType(), ((Date) value).getTime());
		}
		return convertor.doConvertInternalToDMS(definition, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String prepareDMSKey(String key) {
		String baseKey = key.replace("_", ":");
		if (baseKey.indexOf('-') == 0) {
			return baseKey.substring(1);
		}
		return baseKey;
	}
}
