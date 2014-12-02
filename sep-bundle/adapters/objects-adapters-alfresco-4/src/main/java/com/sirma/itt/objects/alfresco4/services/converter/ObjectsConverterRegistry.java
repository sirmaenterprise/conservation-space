package com.sirma.itt.objects.alfresco4.services.converter;

import com.sirma.itt.cmf.alfresco4.services.convert.ConverterRegistry;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * Loads the converter properties for Objects. Extension of base loader.
 * 
 * @author BBonev
 */
@Extension(target = ConverterRegistry.TARGET_NAME, order = 20)
public class ObjectsConverterRegistry implements ConverterRegistry {

	@Override
	public String getNameForSupportedType(Class<? extends Instance> type) {
		if (type == ObjectInstance.class) {
			return "object";
		}
		return null;
	}

}