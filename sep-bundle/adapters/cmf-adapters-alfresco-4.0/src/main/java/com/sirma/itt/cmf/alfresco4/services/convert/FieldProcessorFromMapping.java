package com.sirma.itt.cmf.alfresco4.services.convert;

import java.io.Serializable;
import java.util.Set;

import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.Pair;

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
			DMSTypeConverter dmsTypeConvertor) {
		String fromMap = dmsTypeConvertor.getCMFtoDMSMapping().getProperty(key);
		if (fromMap == null) {
			fromMap = key;
		}
		return new Pair<String, Serializable>(fromMap, value);
	}

	@Override
	public Pair<String, Serializable> processToCmfNoDefinition(String key, Serializable value,
			DMSTypeConverter dmsTypeConvertor) {
		Set<String> set = dmsTypeConvertor.getDMStoCMFMapping().get(key);
		if (set != null && set.size() > 0) {
			return new Pair<String, Serializable>(set.iterator().next(), value);
		}
		return super.processToCmfNoDefinition(key, value, dmsTypeConvertor);
	}

}
