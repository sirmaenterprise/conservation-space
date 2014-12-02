package com.sirma.itt.cmf.security.provider;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.security.ActionProvider;
import com.sirma.itt.emf.security.provider.BaseDefinitionActionProvider;

/**
 * Action provider for workflow task operations.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ActionProvider.TARGET_NAME, order = 31)
public class WorkflowInstanceActionProvider extends BaseDefinitionActionProvider {

	@Override
	protected List<? extends DefinitionModel> getDefinitions() {
		// we should collect all workflows in order to find the operations
		List<WorkflowDefinition> allDefinitions = dictionaryService
				.getAllDefinitions(WorkflowDefinition.class);
		return allDefinitions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<?> getInstanceClass() {
		return WorkflowInstanceContext.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<? extends DefinitionModel> getDefinitionClass() {
		return WorkflowDefinition.class;
	}

}
