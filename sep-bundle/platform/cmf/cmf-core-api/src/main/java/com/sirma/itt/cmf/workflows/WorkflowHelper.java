package com.sirma.itt.cmf.workflows;

import java.util.List;
import java.util.regex.Pattern;

import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.definition.model.Transitional;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.exceptions.DefinitionValidationException;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.PathHelper;

/**
 * Helper class for working with workflow definitions.
 *
 * @author BBonev
 */
public class WorkflowHelper {

	/** The Constant ALPHA_ONLY. */
	private static final Pattern ALPHA_ONLY = Pattern.compile("\\D+");

	/**
	 * Strip workflow engine Id from the given key.
	 *
	 * @param key
	 *            the key
	 * @return the key value without the engine ID
	 */
	public static String stripEngineId(String key) {
		int index = key.indexOf("$");
		if (index == -1) {
			return key;
		}
		return key.substring(index + 1);
	}

	/**
	 * Gets the instance id by stripping and engine id and also removes any
	 * other prefixes from the id.
	 * <p>
	 * <b>NOTE</b>The returned string contains only digits
	 *
	 * @param key
	 *            the key
	 * @return the instance id
	 */
	public static String getInstanceId(String key) {
		String id = stripEngineId(key);
		return ALPHA_ONLY.matcher(id).replaceAll("");
	}

	/**
	 * Gets task by his ID and transition from that task. If task is not found
	 * then <code>null</code> is returned but if the transition is not found
	 * then only the second element of the pair will be <code>null</code>
	 *
	 * @param definition
	 *            the definition
	 * @param taskId
	 *            the task id
	 * @param transitionId
	 *            the transition id
	 * @return the pair of task definition and a transition definition
	 */
	public static Pair<TaskDefinitionRef, TransitionDefinition> getTaskAndTransition(
			WorkflowDefinition definition, String taskId, String transitionId) {
		TaskDefinitionRef taskDefinitionRef = getTaskById(definition, taskId);
		TransitionDefinition transitionDefinition = null;
		if (taskDefinitionRef != null) {
			transitionDefinition = getTransitionById(taskDefinitionRef,
					transitionId);
		} else {
			return null;
		}
		return new Pair<TaskDefinitionRef, TransitionDefinition>(
				taskDefinitionRef, transitionDefinition);
	}

	/**
	 * Gets the transition by id if found.
	 * 
	 * @param taskDefinition
	 *            the task definition ref
	 * @return the found transition or <code>null</code> if not found
	 */
	public static TransitionDefinition getDefaultTransition(Transitional taskDefinition) {
		if (taskDefinition == null) {
			return null;
		}
		List<TransitionDefinition> transitions = taskDefinition.getTransitions();
		for (TransitionDefinition transitionDefinition : transitions) {
			if (transitionDefinition.getDefaultTransition()) {
				return transitionDefinition;
			}
		}
		return null;
	}

	/**
	 * Gets the transition by id if found.
	 * 
	 * @param taskDefinition
	 *            the task definition ref
	 * @param transitionId
	 *            the transition id
	 * @return the found transition or <code>null</code> if not found
	 */
	public static TransitionDefinition getTransitionById(Transitional taskDefinition,
			String transitionId) {
		if (taskDefinition == null) {
			return null;
		}
		List<TransitionDefinition> transitions = taskDefinition.getTransitions();
		for (TransitionDefinition transitionDefinition : transitions) {
			if (EqualsHelper.nullSafeEquals(transitionId, transitionDefinition.getIdentifier(),
					true)) {
				return transitionDefinition;
			}
		}
		return null;
	}

	/**
	 * Gets the start task from the given workflow definitions.
	 *
	 * @param workflowDefinition
	 *            the workflow definition
	 * @return the start task
	 * @throws DefinitionValidationException
	 *             if the start task is not found
	 */
	public static TaskDefinitionRef getStartTask(
			WorkflowDefinition workflowDefinition)
			throws DefinitionValidationException {
		TaskDefinitionRef definitionRef = getTaskByPurpose(workflowDefinition,
				TaskProperties.PURPOSE_START_TASK);
		if (definitionRef == null) {
			throw new DefinitionValidationException(
					"The workflow definition "
							+ workflowDefinition.getIdentifier()
							+ " does not have a start task.");
		}
		return definitionRef;
	}

	/**
	 * Gets the preview task from the given workflow definitions.
	 *
	 * @param workflowDefinition
	 *            the workflow definition
	 * @return the preview task if found
	 * @throws DefinitionValidationException
	 *             if the preview task is not found
	 */
	public static TaskDefinitionRef getWorkflowPreviewTask(
			WorkflowDefinition workflowDefinition)
			throws DefinitionValidationException {
		TaskDefinitionRef definitionRef = getTaskByPurpose(workflowDefinition,
				TaskProperties.PURPOSE_WORKFLOW_PREVIEW);
		return definitionRef;
	}

	/**
	 * Gets the task by purpose.
	 *
	 * @param workflowDefinition
	 *            the workflow definition
	 * @param taskPurpose
	 *            the task purpose
	 * @return the task definition or <code>null</code> if not found
	 */
	public static TaskDefinitionRef getTaskByPurpose(
			WorkflowDefinition workflowDefinition, String taskPurpose) {
		for (TaskDefinitionRef ref : workflowDefinition.getTasks()) {
			if (EqualsHelper
					.nullSafeEquals(ref.getPurpose(), taskPurpose, true)) {
				return ref;
			}
		}
		return null;
	}

	/**
	 * Gets the task by task definition id.
	 *
	 * @param workflowDefinition
	 *            the workflow definition
	 * @param taskId
	 *            the task definition id
	 * @return the task definition or <code>null</code> if not found
	 */
	public static TaskDefinitionRef getTaskById(
			WorkflowDefinition workflowDefinition, String taskId) {
		if (workflowDefinition == null) {
			return null;
		}
		return PathHelper.find(workflowDefinition.getTasks(), taskId);
	}
}
