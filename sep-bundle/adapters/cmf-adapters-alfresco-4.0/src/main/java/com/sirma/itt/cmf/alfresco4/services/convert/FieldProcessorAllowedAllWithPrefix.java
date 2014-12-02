package com.sirma.itt.cmf.alfresco4.services.convert;

import java.io.Serializable;

import com.sirma.itt.emf.domain.Pair;

/**
 * The Class FieldProcessorAllowedAllWithPrefix.
 */
public class FieldProcessorAllowedAllWithPrefix extends FieldProcessorAllowedAll {


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pair<String, Serializable> processToDmsNoDefinition(String key, Serializable value,
			DMSTypeConverter dmsTypeConvertor) {
		if (key.contains(":")) {
			return new Pair<String, Serializable>(key, value);
		}
		return new Pair<String, Serializable>(dmsTypeConvertor.getModelPrefix() + key, value);
	}

}
