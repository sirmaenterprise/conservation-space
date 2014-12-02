package com.sirma.itt.cmf.services.observers;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.event.workflow.AfterWorkflowTransitionEvent;
import com.sirma.itt.cmf.workflows.WorkflowHelper;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.evaluation.ExpressionsManager;

/**
 * Default handler for task completion event. For the executed transition we will check the
 * definition for fields that need to be outjected.
 *
 * @author BBonev
 */
@ApplicationScoped
public class DefaultOnTaskTransition {

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;
	/** The manager. */
	@Inject
	private ExpressionsManager manager;
	/** The logger. */
	@Inject
	private Logger logger;

	/**
	 * On completed task.
	 *
	 * @param event
	 *            the event
	 */
	public void onCompletedTask(@Observes AfterWorkflowTransitionEvent event) {
		WorkflowInstanceContext context = event.getInstance();
		WorkflowDefinition workflowDefinition = dictionaryService.getDefinition(
				WorkflowDefinition.class, context.getIdentifier(), context.getRevision());

		TaskInstance taskInstance = event.getTaskInstance();
		String operation = event.getOperationId();

		Pair<TaskDefinitionRef, TransitionDefinition> pair = WorkflowHelper.getTaskAndTransition(
				workflowDefinition, taskInstance.getIdentifier(), operation);

		if ((pair == null) || (pair.getSecond() == null)) {
			return;
		}
		TransitionDefinition transitionDefinition = pair.getSecond();
		if (transitionDefinition.getFields().isEmpty()) {
			return;
		}

		Map<String, Serializable> extractedFields = manager.evaluateRules(
				new LinkedHashSet<PropertyDefinition>(transitionDefinition.getFields()), false,
				taskInstance, context.getOwningInstance());

		if (logger.isDebugEnabled()) {
			logger.debug("Outjected fields from task instance " + taskInstance.getTaskInstanceId()
					+ " on opeartion " + operation + " " + extractedFields.keySet());
		}
	}
}
