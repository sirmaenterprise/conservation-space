package com.sirma.itt.pm.service.adapter;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.sirma.itt.emf.adapter.DMSDefintionAdapterServiceExtension;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;

/**
 * Adapter service mock for project for project definitions.
 */
@Extension(target = DMSDefintionAdapterServiceExtension.TARGET_NAME, order = 20, priority = 1)
public class PMDefinitionServiceExtensionMock implements DMSDefintionAdapterServiceExtension {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class<?>> getSupportedObjects() {
		List<Class<?>> list = new ArrayList<Class<?>>(1);
		list.add(ProjectDefinition.class);
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSearchPath(Class<?> definitionClass) {
		URL resource = PMDefinitionServiceExtensionMock.class.getResource("dummy");
		return resource.toString();
	}

}
