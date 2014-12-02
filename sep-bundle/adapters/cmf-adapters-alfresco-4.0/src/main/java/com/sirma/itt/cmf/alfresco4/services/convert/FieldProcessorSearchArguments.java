package com.sirma.itt.cmf.alfresco4.services.convert;

import java.io.Serializable;
import java.util.Date;

import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.time.DateRange;

/**
 * The Class FieldProcessorSearchArguments allaows not writable element + properties mapping from
 * file.
 */
public class FieldProcessorSearchArguments extends FieldProcessor {

	/** The cahched pair. */
	private Pair<String, Serializable> CAHCHED_PAIR = new Pair<String, Serializable>(null, null);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pair<String, Serializable> processToDMSSkipped(PropertyDefinition definition,
			Serializable value, DMSTypeConverter convertor) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pair<String, Serializable> processToDMSPassed(PropertyDefinition definition,
			Serializable value, DMSTypeConverter convertor) {
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
		if (value instanceof DateRange) {
			CAHCHED_PAIR.setFirst(definition.getDmsType().substring(indexOfSkippable));
			CAHCHED_PAIR.setSecond(TypeConverterUtil.getConverter().convert(String.class, value));
			return CAHCHED_PAIR;
		}
		return convertor.doConvertInternalToDMS(definition, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pair<String, Serializable> processToDmsNoDefinition(String key, Serializable value,
			DMSTypeConverter dmsTypeConvertor) {
		String fromMap = dmsTypeConvertor.getCMFtoDMSMapping().getProperty(key);
		if (fromMap == null) {
			fromMap = key;
		}
		Serializable s = value;
		if ((value instanceof Date) || (value instanceof DateRange)) {
			s = TypeConverterUtil.getConverter().convert(String.class, value);
		}
		return new Pair<String, Serializable>(fromMap, s);
	}

}
