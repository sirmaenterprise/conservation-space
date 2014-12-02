package com.sirma.itt.cmf.instance;

import java.util.List;

import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.beans.model.TaskType;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.cmf.services.WorkflowService;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.dao.AllowedChildrenTypeProvider;
import com.sirma.itt.emf.instance.dao.BaseAllowedChildrenProvider;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * Specific provider for {@link CaseInstance} children
 *
 * @author BBonev
 */
public class CaseAllowedChildrenProvider extends BaseAllowedChildrenProvider<CaseInstance> {
	@Inject
	private javax.enterprise.inject.Instance<TaskService> taskService;
	@Inject
	private javax.enterprise.inject.Instance<WorkflowService> workflowService;
	/**
	 * Instantiates a new case allowed children provider.
	 *
	 * @param dictionaryService
	 *            the dictionary service
	 * @param allowedChildrenTypeProvider
	 *            the allowed children type provider
	 * @param workflowService
	 *            the workflow service
	 */
	@Inject
	public CaseAllowedChildrenProvider(DictionaryService dictionaryService,
			AllowedChildrenTypeProvider allowedChildrenTypeProvider,
			javax.enterprise.inject.Instance<WorkflowService> workflowService) {
		super(dictionaryService, allowedChildrenTypeProvider);
		this.workflowService = workflowService;
	}

	@Override
	public boolean calculateActive(CaseInstance instance, String type) {
		if (ObjectTypesCmf.WORKFLOW.equals(type)) {
			List<String> ownedTask = taskService.get().getOwnedTaskInstances(instance, TaskState.IN_PROGRESS,
					TaskType.WORKFLOW_TASK);
			return (ownedTask != null) && !ownedTask.isEmpty();
		}
		return super.calculateActive(instance, type);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <A extends Instance> List<A> getActive(CaseInstance instance, String type) {
		if (ObjectTypesCmf.WORKFLOW.equals(type)) {
			return (List<A>) workflowService.get().getCurrentWorkflow(instance);
		}
		return super.getActive(instance, type);
	}

}
