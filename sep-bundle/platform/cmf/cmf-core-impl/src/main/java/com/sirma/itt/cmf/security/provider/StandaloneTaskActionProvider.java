package com.sirma.itt.cmf.security.provider;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.definitions.TaskDefinition;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.security.ActionProvider;
import com.sirma.itt.emf.security.provider.BaseDefinitionActionProvider;

/**
 * Action provider for standalone task operations.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ActionProvider.TARGET_NAME, order = 40)
public class StandaloneTaskActionProvider extends BaseDefinitionActionProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<?> getInstanceClass() {
		return StandaloneTaskInstance.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<? extends DefinitionModel> getDefinitionClass() {
		return TaskDefinition.class;
	}

}
