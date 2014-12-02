package com.sirma.itt.cmf.alfresco4.services.convert;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Loads the converter properties for cmf. Case, Documents are supported
 *
 * @author bbanchev
 */
@Extension(target = ConverterRegistry.TARGET_NAME, order = 0)
public class CMFConverterRegistry implements ConverterRegistry {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getNameForSupportedType(Class<? extends Instance> type) {
		if (type == CaseInstance.class) {
			return "case";
		}
		if (type == DocumentInstance.class) {
			return "document";
		}
		return null;
	}

}