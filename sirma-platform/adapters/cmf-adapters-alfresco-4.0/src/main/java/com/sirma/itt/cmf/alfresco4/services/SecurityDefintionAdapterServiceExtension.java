package com.sirma.itt.cmf.alfresco4.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.DefintionAdapterServiceExtension;
import com.sirma.itt.seip.permissions.model.RoleInstance;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Security module's adapter that provides the means of loading the definitions for the module.
 *
 * @author BBonev
 */
@Extension(target = DefintionAdapterServiceExtension.TARGET_NAME, order = 5)
public class SecurityDefintionAdapterServiceExtension implements DefintionAdapterServiceExtension {

	/** The Constant mapping. */
	private static final Map<Class<?>, String> MAPPING = CollectionUtils.createHashMap(5);

	static {
		MAPPING.put(RoleInstance.class, ServiceURIRegistry.CMF_PERMISSION_DEFINITIONS_SEARCH);
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
		return MAPPING.get(definitionClass);
	}

}
