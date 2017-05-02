package com.sirma.itt.seip.permissions;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.security.Action;

/**
 * The Class is responsible to provide action for externally managed actions.
 */
@ApplicationScoped
public class ExternalModelActionProvider extends BaseDefinitionActionProvider {

	@Override
	protected Class<?> getInstanceClass() {
		return null;
	}

	@Override
	protected Class<? extends DefinitionModel> getDefinitionClass() {
		return null;
	}

	@Override
	public Action createAction(String actionId, String purpose, String labelId) {// NOSONAR
		return super.createAction(actionId, purpose, labelId);
	}

}
