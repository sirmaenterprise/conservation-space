package com.sirma.itt.cmf.alfresco4.services.convert;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;

/**
 * The Class FieldProcessorWritable.
 */
public class FieldProcessorBPMWritable extends FieldProcessorWritable {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasRequiredReadLevel(PropertyDefinition definition) {
		if (definition == null || FORBIDDEN.equals(definition.getDmsType())) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String prepareDMSKey(String key) {
		// REVIEW: pre compile pattern
		String baseKey = key.replace(":", "_");
		if (baseKey.indexOf('-') == 0) {
			return baseKey.substring(1);
		}
		return baseKey;
	}

}
