package com.sirma.itt.pm.security.provider;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.security.ActionProvider;
import com.sirma.itt.emf.security.provider.BaseDefinitionActionProvider;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Producer implementation for project definition operations.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ActionProvider.TARGET_NAME, order = 100)
public class PmActionProvider extends BaseDefinitionActionProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<?> getInstanceClass() {
		return ProjectInstance.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<? extends DefinitionModel> getDefinitionClass() {
		return ProjectDefinition.class;
	}
}
