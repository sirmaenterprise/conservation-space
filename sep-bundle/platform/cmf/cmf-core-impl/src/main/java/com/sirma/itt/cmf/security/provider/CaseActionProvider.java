package com.sirma.itt.cmf.security.provider;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.security.ActionProvider;
import com.sirma.itt.emf.security.provider.BaseDefinitionActionProvider;

/**
 * Action provider for case operations.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ActionProvider.TARGET_NAME, order = 10)
public class CaseActionProvider extends BaseDefinitionActionProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<?> getInstanceClass() {
		return CaseInstance.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<? extends DefinitionModel> getDefinitionClass() {
		return CaseDefinition.class;
	}

}
