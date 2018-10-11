package com.sirma.itt.cmf.alfresco4.services.convert;

import java.io.Serializable;
import java.util.Date;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;

/**
 * The Class FieldProcessorAllowedAll.
 */
public class FieldProcessorAllowedAll extends FieldProcessor {

	/** The cahched pair. */
	private Pair<String, Serializable> CAHCHED_PAIR = new Pair<String, Serializable>(null, null);

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
		if (FORBIDDEN.equals(definition.getDmsType())) {
			return convertor.createDefaultProperty(definition.getName(), value);
		}
		int indexOfSkippable = definition.getDmsType().indexOf("-");
		if (indexOfSkippable == 0) {
			indexOfSkippable = 1;
		} else {
			indexOfSkippable = 0;
		}
		// keep the property and converted it
		if (value instanceof Date) {
			CAHCHED_PAIR.setFirst(definition.getDmsType().substring(indexOfSkippable));
			CAHCHED_PAIR.setSecond(((Date) value).getTime());
			return CAHCHED_PAIR;
		}
		return convertor.doConvertInternalToDMS(definition, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pair<String, Serializable> processToDmsNoDefinition(String key, Serializable value,
			DMSTypeConverterImpl dmsTypeConvertor) {
		return new Pair<String, Serializable>(key, value);
	}

}
