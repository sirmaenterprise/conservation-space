package com.sirma.itt.cmf.security.provider;

import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.model.TaskInstance;
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
@Extension(target = ActionProvider.TARGET_NAME, order = 30)
public class WorkflowTaskActionProvider extends BaseDefinitionActionProvider {

	@Override
	protected List<? extends DefinitionModel> getDefinitions() {
		// we should collect all tasks from all workflows in order to find the operations
		List<TaskDefinitionRef> result = new LinkedList<TaskDefinitionRef>();
		List<WorkflowDefinition> allDefinitions = dictionaryService.getAllDefinitions(WorkflowDefinition.class);
		for (WorkflowDefinition workflowDefinition : allDefinitions) {
			result.addAll(workflowDefinition.getTasks());
		}
		return result;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<?> getInstanceClass() {
		return TaskInstance.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<? extends DefinitionModel> getDefinitionClass() {
		return TaskDefinitionRef.class;
	}

}
