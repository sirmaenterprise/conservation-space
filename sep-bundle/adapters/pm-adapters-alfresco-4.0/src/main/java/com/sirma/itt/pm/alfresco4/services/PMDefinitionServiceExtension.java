package com.sirma.itt.pm.alfresco4.services;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.adapter.DMSDefintionAdapterServiceExtension;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.pm.alfresco4.ServiceURIRegistry;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;

/**
 * Adapter service for working with project definitions. Extends the base cmf definition adapter.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = DMSDefintionAdapterServiceExtension.TARGET_NAME, order = 20)
public class PMDefinitionServiceExtension implements DMSDefintionAdapterServiceExtension {

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
		return ServiceURIRegistry.PM_PROJECT_DEFINITIONS_SEARCH;
	}

}
