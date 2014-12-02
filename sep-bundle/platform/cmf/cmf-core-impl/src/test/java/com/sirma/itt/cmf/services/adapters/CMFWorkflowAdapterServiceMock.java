package com.sirma.itt.cmf.services.adapters;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.services.adapter.CMFWorkflowAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.search.model.SearchArguments;

/**
 * The CMFWorkflowAdapterServiceMock is adapter mock for workflow service
 */
@ApplicationScoped
public class CMFWorkflowAdapterServiceMock implements CMFWorkflowAdapterService {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends AbstractTaskInstance> List<T> transition(String transition, T task)
			throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		// if wf return dummy next task
		if (task instanceof TaskInstance) {
			@SuppressWarnings("unchecked")
			T taskNext = (T) transintionTaskCreation();
			taskNext.setRevision(task.getRevision());
			String workflowId = ((TaskInstance) task).getWorkflowDefinitionId();
			String nextTaskId = getTaskIdForWorkflow(workflowId, transition);
			if (nextTaskId != null) {
				// simulate activiti
				if (workflowId.contains("WFTYPE995") || workflowId.contains("WFTYPE996")) {
					if (task.getProperties().containsKey(TaskProperties.TASK_MULTI_ASSIGNEES)) {
						taskNext.getProperties().put(TaskProperties.TASK_MULTI_ASSIGNEES,
								task.getProperties().get(TaskProperties.TASK_MULTI_ASSIGNEES));
					}
				}
				taskNext.setIdentifier(nextTaskId);
				((TaskInstance) taskNext).setWorkflowInstanceId(((TaskInstance) task)
						.getWorkflowInstanceId());
				((TaskInstance) taskNext).setWorkflowDefinitionId(workflowId);
				return Collections.singletonList(taskNext);
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Gets dummy next state task id for given wf. Should be valid for the definition of workflow
	 *
	 * @param workflowDefinitionId
	 *            the definition id
	 * @param transition
	 *            is the transition taken
	 * @return the task id for this process
	 */
	private String getTaskIdForWorkflow(String workflowDefinitionId, String transition) {
		if (workflowDefinitionId == null) {
			throw new RuntimeException("Update mock implementation. Definition id is null!");
		}
		if (workflowDefinitionId.contains("WFTYPE999")) {
			if ("started".equals(transition)) {
				return "TSTYPE98";
			} else if ("RT0097".equals(transition) || "RT0098".equals(transition)) {
				return "TSTYPE99";
			} else if ("RT0095".equals(transition)) {
				return "TSTYPE98";
			} else if ("RT0096".equals(transition)) {
				return null;
			}
		} else if (workflowDefinitionId.contains("WFTYPE996")) {
			if ("started".equals(transition)) {
				return "TSTYPE94";
			} else if ("RT0097".equals(transition) || "RT0098".equals(transition)) {
				return "TSTYPE99";
			} else if ("RT0095".equals(transition)) {
				return "TSTYPE94";
			} else if ("RT0096".equals(transition)) {
				return null;
			}
		} else if (workflowDefinitionId.contains("WFTYPE995")) {
			if ("started".equals(transition)) {
				return "TSPOOL01";
			} else if ("RT0097".equals(transition) || "RT0098".equals(transition)) {
				return "TSPOOL02";
			} else if ("RT0095".equals(transition)) {
				return "TSPOOL01";
			} else if ("RT0096".equals(transition)) {
				return null;
			}
		} else if (workflowDefinitionId.contains("WFTYPE9M1")) {
			if ("started".equals(transition)) {
				return "TSTYPE9M12";
			} else if ("RT0097".equals(transition) || "RT0098".equals(transition)) {
				return "TSTYPE9M13";
			}
		}
		throw new RuntimeException("Not implemented for " + workflowDefinitionId + " " + transition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends AbstractTaskInstance> List<T> updateTask(T task,
			Map<String, Serializable> toRemove) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return Collections.singletonList(task);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<TaskInstance> startWorkflow(TaskInstance startTask,
			WorkflowInstanceContext workflowContext) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		workflowContext.setDmsId(UUID.randomUUID().toString());
		workflowContext.setWorkflowInstanceId(workflowContext.getDmsId());
		if (workflowContext.getIdentifier().contains("WFTYPE9M1")) {
			@SuppressWarnings("unchecked")
			List<String> assignees = (List<String>) startTask.getProperties().get(
					TaskProperties.TASK_ASSIGNEES);
			List<TaskInstance> multiInstances = new ArrayList<>(assignees.size());
			for (String nextAssignee : assignees) {
				TaskInstance task = transintionTaskCreation();
				task.setRevision(workflowContext.getRevision());
				task.setIdentifier(getTaskIdForWorkflow(workflowContext.getIdentifier(), "started"));

				task.setWorkflowInstanceId(workflowContext.getWorkflowInstanceId());
				task.getProperties().put(TaskProperties.TASK_ASSIGNEE, nextAssignee);
				task.setWorkflowDefinitionId(workflowContext.getIdentifier());
				multiInstances.add(task);
			}
			return multiInstances;
		}
		TaskInstance task = transintionTaskCreation();
		task.setRevision(workflowContext.getRevision());
		task.setIdentifier(getTaskIdForWorkflow(workflowContext.getIdentifier(), "started"));
		task.setWorkflowInstanceId(workflowContext.getWorkflowInstanceId());
		task.setWorkflowDefinitionId(workflowContext.getIdentifier());

		return Collections.singletonList(task);
	}

	/**
	 * Transintion task creation.
	 *
	 * @return the task instance
	 */
	private TaskInstance transintionTaskCreation() {
		TaskInstance instance = new TaskInstance();
		// simply return a single task
		updateTask(instance);
		return instance;
	}

	/**
	 * Update task with basic information
	 *
	 * @param instance
	 *            the instance
	 */
	private void updateTask(AbstractTaskInstance instance) {
		instance.setDmsId(UUID.randomUUID().toString());
		instance.setTaskInstanceId(instance.getDmsId());
		SequenceEntityGenerator.generateStringId(instance, true);
		instance.setState(TaskState.IN_PROGRESS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StandaloneTaskInstance startTask(StandaloneTaskInstance task) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		// simply return a single task
		updateTask(task);
		return task;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cancelWorkflow(WorkflowInstanceContext workflowContext) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cancelTask(StandaloneTaskInstance taskInstance) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SearchArguments<TaskInstance> searchTasks(SearchArguments<TaskInstance> args)
			throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SearchArguments<Pair<String, String>> searchTasksLight(
			SearchArguments<Pair<String, String>> args) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SearchArguments<TaskInstance> searchWorkflowTasks(
			WorkflowInstanceContext workflowContext, SearchArguments<TaskInstance> args)
			throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<TaskInstance> getTasks(WorkflowInstanceContext context, TaskState state)
			throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferedImage getProcessDiagram(String workflowInstance) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Serializable> filterTaskProperties(AbstractTaskInstance currentTask) {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteTask(StandaloneTaskInstance taskInstance) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
	}

	@Override
	public void deleteWorkflow(WorkflowInstanceContext workflowContext, boolean permanent)
			throws DMSException {
		// TODO Auto-generated method stub

	}

}
