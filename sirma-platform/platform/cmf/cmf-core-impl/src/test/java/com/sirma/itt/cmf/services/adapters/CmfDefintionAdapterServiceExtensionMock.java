package com.sirma.itt.cmf.services.adapters;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.DefintionAdapterServiceExtension;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.template.TemplateDefinition;

/**
 * Mock for DefintionAdapterServiceExtension that returs internal uri to definitions location
 *
 * @author bbanchev
 */
@Extension(target = DefintionAdapterServiceExtension.TARGET_NAME, order = 10, priority = 1)
public class CmfDefintionAdapterServiceExtensionMock implements DefintionAdapterServiceExtension {

	/** The Constant mapping. */
	private static final Map<Class<?>, String> MAPPING = CollectionUtils.createHashMap(5);

	static {
		MAPPING.put(TemplateDefinition.class, "");
		MAPPING.put(GenericDefinition.class, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class> getSupportedObjects() {
		return new ArrayList<>(MAPPING.keySet());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSearchPath(Class<?> definitionClass) {
		URL resource = CmfDefintionAdapterServiceExtensionMock.class.getResource("dummy");
		return resource.toString();
	}

}
