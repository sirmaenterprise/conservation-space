package com.sirma.itt.cmf.instance;

import java.util.List;

import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.beans.model.TaskType;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.cmf.services.WorkflowService;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.dao.AllowedChildrenTypeProvider;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.dao.BaseAllowedChildrenProvider;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.RootInstanceContext;

/**
 * Specific provider for {@link DocumentInstance} children when the document is part of a case or
 * project.
 *
 * @author BBonev
 */
public class DocumentAllowedChildrenProvider extends BaseAllowedChildrenProvider<DocumentInstance> {

	/** The task service. */
	@Inject
	private javax.enterprise.inject.Instance<TaskService> taskService;

	/** The workflow service. */
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
	public DocumentAllowedChildrenProvider(DictionaryService dictionaryService,
			AllowedChildrenTypeProvider allowedChildrenTypeProvider,
			javax.enterprise.inject.Instance<WorkflowService> workflowService) {
		super(dictionaryService, allowedChildrenTypeProvider);
		this.workflowService = workflowService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean calculateActive(DocumentInstance instance, String type) {
		Instance context = InstanceUtil.getContext(instance, true);
		// can can calculate only when the current document is part of case or project
		if (((context instanceof CaseInstance) || (context instanceof RootInstanceContext))
				&& ObjectTypesCmf.WORKFLOW.equals(type)) {
			List<String> ownedTask = taskService.get().getOwnedTaskInstances(context,
					TaskState.IN_PROGRESS, TaskType.WORKFLOW_TASK);
			return (ownedTask != null) && (!ownedTask.isEmpty());
		}
		return super.calculateActive(instance, type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <A extends Instance> List<A> getActive(DocumentInstance instance, String type) {
		if (ObjectTypesCmf.WORKFLOW.equals(type)) {
			Instance context = InstanceUtil.getContext(instance, true);
			// can can calculate only when the current document is part of case or project
			if ((context instanceof CaseInstance) || (context instanceof RootInstanceContext)) {
				return (List<A>) workflowService.get().getCurrentWorkflow(context);
			}
		}
		return super.getActive(instance, type);
	}

}
