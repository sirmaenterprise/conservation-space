package com.sirma.itt.cmf.alfresco4.services.convert;

import java.io.Serializable;
import java.util.Set;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;

/**
 * The Class FieldProcessorFromMapping.
 */
public class FieldProcessorFromMapping extends FieldProcessorBPMWritable {

	@Override
	public boolean hasRequiredLevel(PropertyDefinition definition) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pair<String, Serializable> processToDmsNoDefinition(String key, Serializable value,
			DMSTypeConverterImpl dmsTypeConvertor) {
		String fromMap = dmsTypeConvertor.getCMFtoDMSMapping().getProperty(key);
		if (fromMap == null) {
			fromMap = key;
		}
		return new Pair<>(fromMap, value);
	}

	@Override
	public Pair<String, Serializable> processToCmfNoDefinition(String key, Serializable value,
			DMSTypeConverterImpl dmsTypeConvertor) {
		Set<String> set = dmsTypeConvertor.getDMStoCMFMapping().get(key);
		if (set != null && !set.isEmpty()) {
			return new Pair<>(set.iterator().next(), value);
		}
		return super.processToCmfNoDefinition(key, value, dmsTypeConvertor);
	}

}
