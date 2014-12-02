package com.sirma.itt.objects.alfresco4.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sirma.itt.emf.adapter.DMSDefintionAdapterServiceExtension;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.objects.alfresco4.ObjectsServiceURIRegistry;
import com.sirma.itt.objects.domain.definitions.ObjectDefinition;

/**
 * CMF module's adapter that provides the means of loading the definitions for the module.
 *
 * @author BBonev
 */
@Extension(target = DMSDefintionAdapterServiceExtension.TARGET_NAME, order = 30)
public class ObjectsDefintionAdapterServiceExtension implements DMSDefintionAdapterServiceExtension {

	/** The Constant mapping. */
	private static final Map<Class<?>, String> MAPPING = CollectionUtils.createHashMap(5);
	static {
		MAPPING.put(ObjectDefinition.class, ObjectsServiceURIRegistry.OBJECT_DEFINITIONS_SEARCH);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class<?>> getSupportedObjects() {
		return new ArrayList<Class<?>>(MAPPING.keySet());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSearchPath(Class<?> definitionClass) {
		return MAPPING.get(definitionClass);
	}

}
