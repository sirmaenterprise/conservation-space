package com.sirma.itt.pm.alfresco4.services.convert;

import com.sirma.itt.cmf.alfresco4.services.convert.ConverterRegistry;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Loads the converter properties for PM. Extension of base loader.
 *
 * @author bbanchev
 */
@Extension(target = ConverterRegistry.TARGET_NAME, order = 10)
public class PMConverterRegistry implements ConverterRegistry {

	@Override
	public String getNameForSupportedType(Class<? extends Instance> type) {
		if (type == ProjectInstance.class) {
			return "project";
		}
		return null;
	}

}