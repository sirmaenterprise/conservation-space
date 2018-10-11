package com.sirma.itt.cmf.alfresco4.services.convert;

import java.io.Serializable;

import com.sirma.itt.seip.Pair;

/**
 * The Class FieldProcessorAllowedAllWithPrefix.
 */
public class FieldProcessorAllowedAllWithPrefix extends FieldProcessorAllowedAll {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pair<String, Serializable> processToDmsNoDefinition(String key, Serializable value,
			DMSTypeConverterImpl dmsTypeConvertor) {
		if (key.contains(":")) {
			return new Pair<>(key, value);
		}
		return new Pair<>(dmsTypeConvertor.getModelPrefix() + key, value);
	}

}
