package com.sirma.itt.cmf.services.impl;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.LinkConstantsCmf;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.constants.WorkflowProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.db.DbQueryTemplates;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.event.workflow.BeforeWorkflowCompleteEvent;
import com.sirma.itt.cmf.event.workflow.BeforeWorkflowPersistEvent;
import com.sirma.itt.cmf.event.workflow.BeforeWorkflowStartEvent;
import com.sirma.itt.cmf.event.workflow.BeforeWorkflowTransitionEvent;
import com.sirma.itt.cmf.event.workflow.WorkflowChangeEvent;
import com.sirma.itt.cmf.event.workflow.WorkflowCreateEvent;
import com.sirma.itt.cmf.event.workflow.WorkflowPersistedEvent;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.cmf.services.WorkflowService;
import com.sirma.itt.cmf.services.adapter.CMFWorkflowAdapterService;
import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.cmf.workflows.TaskTransitionBinding;
import com.sirma.itt.cmf.workflows.TaskTypeBinding;
import com.sirma.itt.cmf.workflows.WorkflowHelper;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.dao.AllowedChildrenProvider;
import com.sirma.itt.emf.definition.dao.AllowedChildrenTypeProvider;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.definition.model.Transitional;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.InstanceEventType;
import com.sirma.itt.emf.exceptions.DefinitionValidationException;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.PropertiesUtil;
import com.sirma.itt.emf.instance.dao.AllowedChildrenHelper;
import com.sirma.itt.emf.instance.dao.BaseAllowedChildrenProvider;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceEventProvider;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.state.operation.event.OperationExecutedEvent;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.PropertyModelComparator;

/**
 * Default implementation for the {@link WorkflowService}.
 * 
 * @author BBonev
 */
@Stateless
public class WorkflowServiceImpl implements WorkflowService {

	private static final Operation START_WORKFLOW = new Operation(
			ActionTypeConstants.START_WORKFLOW);
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowServiceImpl.class);
	/** The debug. */
	private boolean debug;
	/** The debug. */
	private boolean trace;
	/** The instance dao. */
	@Inject
	@InstanceType(type = ObjectTypesCmf.WORKFLOW)
	private InstanceDao<WorkflowInstanceContext> instanceDao;
	/** The task instance dao. */
	@Inject
	@InstanceType(type = ObjectTypesCmf.WORKFLOW_TASK)
	private InstanceDao<TaskInstance> taskInstanceDao;

	/** The adapter service. */
	@Inject
	private CMFWorkflowAdapterService adapterService;
	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;
	/** The transition event. */
	@Inject
	@Any
	private EventService eventService;
	/** The db dao. */
	@Inject
	private DbDao dbDao;

	/** The authentication service. */
	@Inject
	private javax.enterprise.inject.Instance<AuthenticationService> authenticationService;

	/** The instance service. */
	@Inject
	@Proxy
	private InstanceService<Instance, DefinitionModel> instanceService;

	/** The task service. */
	@Inject
	private TaskService taskService;

	/** The allowed children type provider. */
	@Inject
	private AllowedChildrenTypeProvider allowedChildrenTypeProvider;

	/** The allowed child calculator. */
	private AllowedChildrenProvider<WorkflowInstanceContext> allowedChildCalculator;

	@Inject
	private ServiceRegister register;

	@Inject
	private LinkService linkService;
	private final Operation editDetailsOperation = new Operation(ActionTypeConstants.EDIT_DETAILS);

	/**
	 * Inits the.
	 */
	@PostConstruct
	public void init() {
		debug = LOGGER.isDebugEnabled();
		trace = LOGGER.isTraceEnabled();
		allowedChildCalculator = new BaseAllowedChildrenProvider<WorkflowInstanceContext>(
				dictionaryService, allowedChildrenTypeProvider);
	}

	/**
	 * Creates a workflow instance from the given definition.
	 * 
	 * @param definition
	 *            the definition
	 * @param instance
	 *            the instance to set as owning
	 * @return the workflow instance context
	 */
	protected WorkflowInstanceContext createWorkflowInstance(WorkflowDefinition definition,
			Instance instance) {
		WorkflowInstanceContext context = createInstance(definition);
		Instance parent = InstanceUtil.getContext(instance, true);
		if (parent != null) {
			context.setOwningReference(parent.toReference());
			context.setOwningInstance(parent);
			context.getProperties().put(WorkflowProperties.CONTEXT_TYPE, parent.getIdentifier());
			if ((instance != null) && !parent.getClass().equals(instance.getClass())) {
				// create link for the processed target
				linkService.link(context.toReference(), instance.toReference(),
						LinkConstantsCmf.PROCESSES, LinkConstantsCmf.PROCESSED_BY,
						LinkConstantsCmf.DEFAULT_SYSTEM_PROPERTIES);
			}
		}

		instanceDao.setCurrentUserTo(context, WorkflowProperties.STARTED_BY);
		context.getProperties().put(WorkflowProperties.TYPE,
				WorkflowHelper.stripEngineId(definition.getIdentifier()));

		context.getProperties().put(WorkflowProperties.REVISION, context.getRevision());

		notifyForExecutedOperation(context, START_WORKFLOW);
		eventService.fire(new WorkflowCreateEvent(context, START_WORKFLOW.getOperation()));
		// eventually we can fire an event for created context
		return context;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Secure
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public List<TaskInstance> startWorkflow(WorkflowInstanceContext context,
			TaskInstance startTaskInstance) {
		boolean error = false;
		TimeTracker tracker = new TimeTracker();
		tracker.begin();
		StringBuilder traceMsg = null;
		if (trace) {
			traceMsg = new StringBuilder();
			traceMsg.append("Called startWorkflow(" + context + ")\n");
			tracker.begin();
		}
		RuntimeConfiguration.setConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION,
				TaskProperties.TRANSITION_START_WORKFLOW);
		try {
			Instance owningInstance = context.getOwningInstance();
			if (owningInstance == null) {
				error = true;
				String string = "Cannot start workflow for invalid or non existent parent!";
				traceMsg.append(string);
				throw new EmfRuntimeException(string);
			}
			// call instance update for state change
			instanceService.refresh(owningInstance);

			notifyForExecutedOperation(context, new Operation(
					TaskProperties.TRANSITION_START_WORKFLOW));

			context.setActive(Boolean.TRUE);

			instanceDao.setCurrentUserTo(context, WorkflowProperties.STARTED_BY);

			BeforeWorkflowStartEvent event = new BeforeWorkflowStartEvent(context,
					TaskProperties.TRANSITION_START_WORKFLOW);
			eventService.fire(event);
			if (trace) {
				traceMsg.append("WorkflowEventType.BEFORE_START took: " + tracker.stopInSeconds()
						+ " s\n");
				tracker.begin();
			}

			WorkflowDefinition definition = getDefinition(context);

			TaskDefinitionRef startTaskDef = WorkflowHelper.getStartTask(definition);
			TransitionDefinition startWorkflowTrans = WorkflowHelper.getTransitionById(
					startTaskDef, TaskProperties.TRANSITION_START_WORKFLOW);
			if (startWorkflowTrans == null) {
				throw new DefinitionValidationException("The workflow's "
						+ context.getIdentifier() + " start task is missing the "
						+ TaskProperties.TRANSITION_START_WORKFLOW + " transition!");
			}

			Map<String, Serializable> cloneProperties = PropertiesUtil
					.cloneProperties(startTaskInstance.getProperties());
			cloneProperties.remove(WorkflowProperties.STATUS);
			context.getProperties().putAll(cloneProperties);

			TaskInstance startTask = updateStartTask(context, startTaskDef, startTaskInstance);
			// notify for start workflow task handler
			BeforeWorkflowTransitionEvent transitionEvent = new BeforeWorkflowTransitionEvent(
					context, startTask, startWorkflowTrans);
			notifyForTransition(transitionEvent, startTaskDef, startWorkflowTrans, context,
					startTask, false);
			if (trace) {
				traceMsg.append("Transition handling took: " + tracker.stopInSeconds() + " s\n");
				tracker.begin();
			}

			BeforeWorkflowPersistEvent workflowPersistEvent = new BeforeWorkflowPersistEvent(
					context);
			eventService.fire(workflowPersistEvent);

			List<TaskInstance> activeTasks = adapterService.startWorkflow(startTask, context);

			if (trace) {
				traceMsg.append("DMS start took: " + tracker.stopInSeconds() + " s\n");
				tracker.begin();
			}

			context.getProperties().put(DefaultProperties.ACTUAL_START_DATE, new Date());

			CollectionUtils.copyValue(context, DefaultProperties.ACTUAL_START_DATE, startTask,
					DefaultProperties.ACTUAL_START_DATE);
			notifyForTransition(transitionEvent, startTaskDef, startWorkflowTrans, context,
					startTask, true);
			eventService.fireNextPhase(event);

			instanceDao.instanceUpdated(context, false);

			eventService.fire(new WorkflowChangeEvent(context));

			// persist the workflow
			WorkflowInstanceContext old = instanceDao.persistChanges(context);
			eventService.fireNextPhase(workflowPersistEvent);
			if (trace) {
				traceMsg.append("DB persist took: " + tracker.stopInSeconds() + " s");
				tracker.begin();
			}

			// insert a row for the workflow creator
			// here we should insert the task instance ID of the start task but we do not have
			// it until we query the DMS
			// update: the start task Id is the same as the WF instance id
			startTask.setTaskInstanceId(context.getWorkflowInstanceId());
			startTask.setOwningReference(context.toReference());

			// we does not want a update of the start task to DMS - it can't be updated
			persistTask(context, startTask, null, owningInstance, true, null);

			taskService.attachTaskToInstance(owningInstance, Arrays.asList(startTask), context,
					false);
			// add to the lookup table the list of tasks
			taskService.attachTaskToInstance(owningInstance, activeTasks, context, true);

			// notify for created tasks
			for (TaskInstance taskInstance : activeTasks) {
				persistTask(context, taskInstance, startTask, owningInstance, false, null);
			}

			if (trace) {
				traceMsg.append("WorkflowEventType.AFTER_START took: " + tracker.stopInSeconds()
						+ " s\n");
			}

			RuntimeConfiguration.enable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);

			instanceService.save(owningInstance,
					createOperation(ActionTypeConstants.START_WORKFLOW, startWorkflowTrans));
			eventService.fire(new WorkflowPersistedEvent(context, old,
					TaskProperties.TRANSITION_START_WORKFLOW));
			return activeTasks;
		} catch (DMSException e) {
			error = true;
			throw new EmfRuntimeException("Failed to start workflow due to " + e.getMessage(), e);
		} finally {
			RuntimeConfiguration.disable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
			RuntimeConfiguration.setConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION,
					TaskProperties.TRANSITION_START_WORKFLOW);
			// // clear the case instance at the end of the call
			if (debug) {
				LOGGER.debug("Workflow " + (error ? "failed to start" : "started successfully")
						+ " in " + tracker.stopInSeconds() + " s");
				if (trace) {
					LOGGER.trace(traceMsg.toString());
				}
			}
		}
	}

	/**
	 * Notify for executed operation.
	 * 
	 * @param context
	 *            the context
	 * @param operation
	 *            the operation
	 */
	private void notifyForExecutedOperation(Instance context, Operation operation) {
		eventService.fire(new OperationExecutedEvent(operation, context));
	}

	/**
	 * Updates the start task against the workflow context and returns the same instance.
	 * 
	 * @param context
	 *            the context
	 * @param startTaskDef
	 *            the start task def
	 * @param instance
	 *            the instance
	 * @return the task instance
	 */
	private TaskInstance updateStartTask(WorkflowInstanceContext context,
			TaskDefinitionRef startTaskDef, TaskInstance instance) {
		instance.getProperties().putAll(PropertiesUtil.cloneProperties(context.getProperties()));
		instance.setContext(context);
		taskInstanceDao.instanceUpdated(instance, false);
		instance.getProperties().remove(DefaultProperties.STATUS);
		instance.setRevision(context.getRevision());
		instance.setState(TaskState.COMPLETED);
		instance.setIdentifier(startTaskDef.getIdentifier());
		instance.getProperties().put(TaskProperties.TASK_OWNER,
				context.getProperties().get(WorkflowProperties.STARTED_BY));
		instance.getProperties().put(TaskProperties.TYPE, startTaskDef.getIdentifier());
		instance.getProperties().put(TaskProperties.TASK_COMMENT,
				instance.getProperties().get(WorkflowProperties.MESSAGE));
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Secure
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public List<TaskInstance> updateWorkflow(WorkflowInstanceContext context,
			TaskInstance taskInstance, String operation) {
		if ((context == null) || (taskInstance == null) || (operation == null)) {
			throw new IllegalArgumentException();
		}
		if (!context.isActive()) {
			LOGGER.warn("Trying to update completed/canceled workflow: " + context.getIdentifier()
					+ ":" + context.getWorkflowInstanceId());
			return CollectionUtils.emptyList();
		}
		RuntimeConfiguration.setConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION,
				operation);

		TimeTracker tracker = new TimeTracker().begin();

		taskInstance.getProperties().put(WorkflowProperties.TRANSITION, operation);

		Pair<TaskDefinitionRef, TransitionDefinition> pair = WorkflowHelper.getTaskAndTransition(
				getDefinition(context), taskInstance.getIdentifier(), operation);
		if (pair == null) {
			throw new EmfRuntimeException("No such task definition " + taskInstance.getIdentifier()
					+ " for workflow " + context.getIdentifier());
		} else if (pair.getSecond() == null) {
			throw new EmfRuntimeException("No such transition definition " + operation
					+ " for task " + taskInstance + " from workflow " + context.getIdentifier());
		}

		try {
			// initialize a case instance into the context so that all events can update a single
			// copy and not several
			Instance owningInstance = context.getOwningInstance();
			// refresh the instance before firing the events if was modified while the task was
			// opened
			instanceService.refresh(owningInstance);

			// notify we a going to execute a transition
			BeforeWorkflowTransitionEvent transitionEvent = new BeforeWorkflowTransitionEvent(
					context, taskInstance, pair.getSecond());
			// notify for transition
			notifyForTransition(transitionEvent, pair.getFirst(), pair.getSecond(), context,
					taskInstance, false);

			// execute the transition into the BPM engine
			List<TaskInstance> transition = adapterService.transition(operation, taskInstance);

			// remove all current tasks
			taskService.updateTaskStateAndAssignment(owningInstance, context, taskInstance,
					Boolean.FALSE);

			List<TaskInstance> newActiveTasks = afterTransitionTaskUpdate(context, taskInstance,
					owningInstance, transition);
			// and the new tasks
			taskService.attachTaskToInstance(owningInstance, newActiveTasks, context, true);

			notifyForTransition(transitionEvent, pair.getFirst(), pair.getSecond(), context,
					taskInstance, true);

			if (transition.isEmpty()) {
				// notify we a going to complete the workflow using the given operation
				BeforeWorkflowCompleteEvent event = new BeforeWorkflowCompleteEvent(context,
						taskInstance, ActionTypeConstants.COMPLETE);
				eventService.fire(event);

				notifyForExecutedOperation(context, new Operation(ActionTypeConstants.COMPLETE));
				// set the workflow as completed
				completeWorkflow(context, ActionTypeConstants.COMPLETE);

				// notify we have completed the workflow
				eventService.fireNextPhase(event);

				// set inactive
				updateOwningInstanceForWorkflowEnd(context, ActionTypeConstants.EDIT_DETAILS,
						pair.getSecond(), owningInstance);
			} else {
				// should notify the case that the workflow changed transition
				updateOwningInstanceForWorkflowTransition(context, pair.getSecond(), owningInstance);
			}
			taskInstance.setState(TaskState.COMPLETED);
			persistTask(context, taskInstance, null, owningInstance, true, editDetailsOperation);

			save(context, new Operation(operation));
			if (debug) {
				LOGGER.debug("Executed transition " + operation + " on workflow "
						+ context.getWorkflowInstanceId() + " for " + tracker.stopInSeconds()
						+ " s");
			}

			return newActiveTasks;
		} catch (DMSException e) {
			throw new EmfRuntimeException(
					"Failed to execute transition workflow for workfow due to " + e.getMessage(), e);
		} finally {
			RuntimeConfiguration
					.clearConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION);
		}
	}

	/**
	 * Post processing of workflow tasks, based on transition tasks. New tasks are persisted and old
	 * tasks are removed/completed if necessary.
	 * 
	 * @param context
	 *            is the workflow
	 * @param taskInstance
	 *            is the current instance, produced the transition
	 * @param owningInstance
	 *            is the workflow owning instance
	 * @param transition
	 *            are the created task after transition
	 * @return the list of current active tasks.
	 */
	private List<TaskInstance> afterTransitionTaskUpdate(WorkflowInstanceContext context,
			TaskInstance taskInstance, Instance owningInstance, List<TaskInstance> transition) {
		Set<String> currentActiveTaskIds = taskService.getContextTasks(context, Boolean.TRUE);
		List<TaskInstance> newActiveTasks = new ArrayList<>(transition.size());
		for (TaskInstance instance : transition) {
			if (!currentActiveTaskIds.contains(instance.getTaskInstanceId())) {
				newActiveTasks.add(instance);
				persistTask(context, instance, taskInstance, owningInstance, false,
						editDetailsOperation);
			}
		}
		Map<Serializable, TaskInstance> bpmnCurrentActiveTasks = new HashMap<Serializable, TaskInstance>(
				transition.size());
		for (TaskInstance instance : transition) {
			bpmnCurrentActiveTasks.put(instance.getTaskInstanceId(), instance);
		}
		// remove the current task
		currentActiveTaskIds.remove(taskInstance.getTaskInstanceId());
		List<AbstractTaskInstance> currentActiveTask = taskService
				.load((List<Serializable>) new ArrayList<Serializable>(currentActiveTaskIds));
		if (currentActiveTask.size() != currentActiveTaskIds.size()) {
			LOGGER.warn("Not all tasks could be loaded for processing: " + currentActiveTask);
		}
		for (AbstractTaskInstance nextActiveTask : currentActiveTask) {
			if ((nextActiveTask instanceof TaskInstance)
					&& !bpmnCurrentActiveTasks.containsKey(nextActiveTask.getTaskInstanceId())) {
				nextActiveTask.setState(TaskState.COMPLETED);
				nextActiveTask.getProperties().put(TaskProperties.STATUS,
						PrimaryStates.COMPLETED.getType());
				persistTask(context, (TaskInstance) nextActiveTask, taskInstance, owningInstance,
						true, editDetailsOperation);
				System.out.println(nextActiveTask);
			} else {
				LOGGER.warn("Task that is part of workflow is not expected type: " + nextActiveTask);
			}
		}
		return newActiveTasks;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public WorkflowInstanceContext loadContext(String instanceId) {
		return instanceDao.loadInstance(null, instanceId, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<WorkflowInstanceContext> loadContexts(List<S> instanceId) {
		return instanceDao.loadInstances(instanceId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<TaskInstance> getWorkflowTasks(WorkflowInstanceContext context, TaskState state) {
		if (context == null) {
			return CollectionUtils.emptyList();
		}
		TimeTracker tracker = new TimeTracker();
		tracker.begin();
		String query = DbQueryTemplates.QUERY_TASK_BY_STATE_KEY;
		List<Pair<String, Object>> params = new ArrayList<Pair<String, Object>>(2);
		params.add(new Pair<String, Object>("workflowInstanceId", context.getWorkflowInstanceId()));
		if ((TaskState.ALL == state) || (state == null)) {
			query = DbQueryTemplates.QUERY_ALL_TASKS_KEY;
		} else {
			params.add(new Pair<String, Object>("state", state));
		}
		List<Long> list = dbDao.fetchWithNamed(query, params);
		List<TaskInstance> tasks = taskInstanceDao.loadInstancesByDbKey(list, true);
		if (debug) {
			LOGGER.debug("Task search for " + context.getWorkflowInstanceId() + ":" + state
					+ " returned " + tasks.size() + " tasks and took " + tracker.stopInSeconds()
					+ " s");
		}
		// NOTE: this instance here is probably independent copy of the case
		// and should not be used for visualizations
		Instance instance = context.getOwningInstance();
		for (Iterator<TaskInstance> it = tasks.iterator(); it.hasNext();) {
			TaskInstance taskInstance = it.next();
			taskInstance.setContext(context);
			taskInstance.setOwningInstance(instance);
		}
		// context is filled in adapter during converting props

		// added task sorting
		Collections.sort(tasks, new PropertyModelComparator(false, TaskProperties.ACTUAL_END_DATE));
		return tasks;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<WorkflowInstanceContext> getCurrentWorkflow(Instance instance) {
		if (instance == null) {
			return null;
		}
		List<WorkflowInstanceContext> result = instanceDao.loadInstances(instance, true);
		Iterator<WorkflowInstanceContext> iterator = result.iterator();
		while (iterator.hasNext()) {
			WorkflowInstanceContext context = iterator.next();
			if (!context.isActive()) {
				iterator.remove();
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Secure
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public BufferedImage getWorkflowProcessDiagram(WorkflowInstanceContext context) {
		try {
			return adapterService.getProcessDiagram(context.getWorkflowInstanceId());
		} catch (DMSException e) {
			LOGGER.warn("Failed to retrieve the process diagram for workflow instance"
					+ context.getWorkflowInstanceId());
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<WorkflowInstanceContext> getWorkflowsHistory(Instance instance) {
		TimeTracker tracker = new TimeTracker();
		tracker.begin();
		StringBuilder debugMsg = new StringBuilder();

		List<Pair<String, Object>> params = new ArrayList<Pair<String, Object>>(1);
		InstanceReference reference = instance.toReference();
		params.add(new Pair<String, Object>("sourceId", reference.getIdentifier()));
		params.add(new Pair<String, Object>("sourceType", reference.getReferenceType().getId()));
		List<String> contextIds = dbDao.fetchWithNamed(DbQueryTemplates.QUERY_WORKFLOW_HISTORY_KEY,
				params);
		if (debug) {
			debugMsg.append("\nDB wf history search took: ").append(tracker.stopInSeconds())
					.append(" s and returned ").append(contextIds.size()).append(" results");
		}
		if (contextIds.isEmpty()) {
			if (debug) {
				LOGGER.debug(debugMsg.toString());
			}
			return CollectionUtils.emptyList();
		}
		tracker.begin();
		List<WorkflowInstanceContext> instances = instanceDao.loadInstances(contextIds);
		for (WorkflowInstanceContext context : instances) {
			context.setOwningInstance(instance);
		}
		Collections.sort(instances, new PropertyModelComparator(false,
				WorkflowProperties.ACTUAL_END_DATE));
		if (debug) {
			debugMsg.append("\nPost load took ").append(tracker.stopInSeconds()).append(" s");
			LOGGER.debug(debugMsg.toString());
		}
		return instances;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Secure
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void updateTaskInstance(TaskInstance instance) {
		Operation operation = new Operation(ActionTypeConstants.EDIT_DETAILS);

		taskService.save(instance, operation);
		if (instance.getContext() != null) {
			save(instance.getContext(), operation);

			Instance owninginstance = instance.getContext().getOwningInstance();
			instanceService.refresh(owninginstance);

			// removeTasksForWorkflow(instance.getContext(), false);
			// addTasksForUsers(instance.getContext(), tasks, instance.getId());
			// NOTE: if for some reason the method updateTask(TaskInstance, null) changes the
			// list of tasks - more changes are needed here
			// only update the assigner if changed
			taskService.updateTaskStateAndAssignment(owninginstance, instance.getContext(),
					instance, TaskState.IN_PROGRESS.equals(instance.getState()));
			RuntimeConfiguration.enable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
			try {
				instanceService.save(owninginstance, operation);
			} finally {
				RuntimeConfiguration.disable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
			}
		}
	}

	/**
	 * Creates the operation from the given transition definition. If the definition defines case
	 * state change then they will be added to the operation
	 * 
	 * @param operationType
	 *            the operation type
	 * @param transitionDefinition
	 *            the transition definition
	 * @return the operation
	 */
	private Operation createOperation(String operationType,
			TransitionDefinition transitionDefinition) {
		return new Operation(operationType, transitionDefinition.getNextPrimaryState(),
				transitionDefinition.getNextSecondaryState());
	}

	/**
	 * Update case for workflow transition.
	 * 
	 * @param context
	 *            the context
	 * @param transition
	 *            the transition
	 * @param owningInstance
	 *            the owning instance
	 */
	private void updateOwningInstanceForWorkflowTransition(WorkflowInstanceContext context,
			TransitionDefinition transition, Instance owningInstance) {
		if (context.getOwningReference() == null) {
			return;
		}
		RuntimeConfiguration.enable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
		try {
			instanceService.save(owningInstance, new Operation(
					ActionTypeConstants.WORKFLOW_TRANSITION, transition.getNextPrimaryState(),
					transition.getNextSecondaryState()));
		} finally {
			RuntimeConfiguration.disable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
		}
	}

	/**
	 * Update the case and workflow for workflow completion. Does not save anything
	 * 
	 * @param context
	 *            the context
	 * @param operationCode
	 *            the operation code
	 */
	private void completeWorkflow(WorkflowInstanceContext context, String operationCode) {
		if (context.getOwningReference() == null) {
			return;
		}
		// set inactive
		context.setActive(Boolean.FALSE);
		context.getProperties().put(WorkflowProperties.ACTUAL_END_DATE, new Date());
		instanceDao.setCurrentUserTo(context, WorkflowProperties.COMPLETED_BY);
	}

	/**
	 * Update case instance for workflow end.
	 * 
	 * @param context
	 *            the context
	 * @param operationCode
	 *            the operation
	 * @param definition
	 *            the definition
	 * @param instance
	 *            the owning instance
	 */
	private void updateOwningInstanceForWorkflowEnd(WorkflowInstanceContext context,
			String operationCode, TransitionDefinition definition, Instance instance) {
		boolean configurationSet = RuntimeConfiguration
				.isConfigurationSet(RuntimeConfigurationProperties.DO_NO_CALL_DMS);
		if (configurationSet) {
			RuntimeConfiguration.disable(RuntimeConfigurationProperties.DO_NO_CALL_DMS);
		}
		RuntimeConfiguration.enable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
		Operation operation = new Operation(operationCode);
		if (definition != null) {
			operation.setNextPrimaryState(definition.getNextPrimaryState());
			operation.setNextSecondaryState(definition.getNextSecondaryState());
		}
		try {
			instanceService.save(instance, operation);
		} finally {
			if (configurationSet) {
				RuntimeConfiguration.enable(RuntimeConfigurationProperties.DO_NO_CALL_DMS);
			}
			RuntimeConfiguration.disable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
		}
	}

	/**
	 * Gets the definition from the given instance.
	 * 
	 * @param context
	 *            the context
	 * @return the definition
	 */
	private WorkflowDefinition getDefinition(WorkflowInstanceContext context) {
		return (WorkflowDefinition) dictionaryService.getInstanceDefinition(context);
	}

	/**
	 * Creates WorkflowInstanceContext and populates the default properties.
	 * 
	 * @param definition
	 *            the definition
	 * @return the workflow instance context
	 */
	private WorkflowInstanceContext createInstance(WorkflowDefinition definition) {
		WorkflowInstanceContext instanceContext = instanceDao.createInstance(definition, true);

		Serializable container = SecurityContextManager.getCurrentContainer(authenticationService);
		instanceContext.getProperties().put(WorkflowProperties.PARENT_CONTAINER, container);
		return instanceContext;
	}

	/**
	 * Notify for transition.
	 * 
	 * @param <E>
	 *            the element type
	 * @param event
	 *            the event
	 * @param task
	 *            the task
	 * @param transition
	 *            the transition
	 * @param context
	 *            the context
	 * @param taskInstance
	 *            the task instance
	 * @param nextPhase
	 *            the next phase
	 */
	private <E extends TwoPhaseEvent> void notifyForTransition(E event, TaskDefinitionRef task,
			TransitionDefinition transition, WorkflowInstanceContext context,
			AbstractTaskInstance taskInstance, boolean nextPhase) {
		TaskTypeBinding typeBinding = new TaskTypeBinding(task.getIdentifier());
		TaskTransitionBinding transitionBinding = new TaskTransitionBinding(transition.getEventId());
		if (debug) {
			LOGGER.debug("Firing event (type=" + task.getIdentifier() + ", transition="
					+ transition.getEventId() + ") for " + context.getIdentifier() + ":"
					+ context.getWorkflowInstanceId());
		}
		if (nextPhase) {
			eventService.fireNextPhase(event, typeBinding, transitionBinding);
		} else {
			// notify for start workflow task handler
			eventService.fire(event, typeBinding, transitionBinding);
		}
	}

	/**
	 * Persist task.
	 * 
	 * @param context
	 *            the context that the task is part of
	 * @param peristable
	 *            the task instance to save
	 * @param previousTask
	 *            the previous task
	 * @param startedOn
	 *            is the instance wf is started on
	 * @param dontCallDms
	 *            if true, onlu emf db is updated
	 * @param operation
	 *            the operation to perform on persist
	 */
	private void persistTask(WorkflowInstanceContext context, TaskInstance peristable,
			TaskInstance previousTask, Instance startedOn, boolean dontCallDms, Operation operation) {
		try {
			if (dontCallDms) {
				RuntimeConfiguration.enable(RuntimeConfigurationProperties.DO_NO_CALL_DMS);
			}
			SequenceEntityGenerator.generateStringId(peristable, true);

			peristable.setParentTask(previousTask);
			peristable.setOwningInstance(context);

			if (peristable.getContext() == null) {
				peristable.setContext(context);
			}
			if ((peristable.getOwningReference() == null)
					|| StringUtils.isNullOrEmpty(peristable.getOwningReference().getIdentifier())) {
				peristable.setOwningReference(context.toReference());
			}
			taskService.save(peristable, operation);
			// update the instance info
			taskService.updateTaskStateAndAssignment(startedOn, context, peristable,
					peristable.getState() != TaskState.COMPLETED);
		} finally {
			peristable.setParentTask(null);
			RuntimeConfiguration.disable(RuntimeConfigurationProperties.DO_NO_CALL_DMS);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public WorkflowInstanceContext createInstance(WorkflowDefinition definition, Instance parent) {
		return createWorkflowInstance(definition, parent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public WorkflowInstanceContext createInstance(WorkflowDefinition definition, Instance parent,
			Operation operation) {
		return createWorkflowInstance(definition, parent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public WorkflowInstanceContext save(WorkflowInstanceContext instance, Operation operation) {
		String operationId = null;
		if (operation != null) {
			operationId = operation.getOperation();
			RuntimeConfiguration.setConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION,
					operationId);
		}
		try {
			notifyForExecutedOperation(instance, operation);

			BeforeWorkflowPersistEvent event = null;
			if (!SequenceEntityGenerator.isPersisted(instance)) {
				event = new BeforeWorkflowPersistEvent(instance);
				eventService.fire(event);
			}
			instanceDao.instanceUpdated(instance, false);

			eventService.fire(new WorkflowChangeEvent(instance));

			WorkflowInstanceContext old = instanceDao.persistChanges(instance);
			if (event != null) {
				eventService.fireNextPhase(event);
			}
			eventService.fire(new WorkflowPersistedEvent(instance, old, operationId));
		} finally {
			if (operation != null) {
				RuntimeConfiguration
						.clearConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION);
			}
		}
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void refresh(WorkflowInstanceContext instance) {
		WorkflowInstanceContext context = instanceDao.loadInstance(instance.getId(),
				instance.getDmsId(), false);
		instanceDao.loadProperties(instance);
		instance.setWorkflowInstanceId(context.getWorkflowInstanceId());
		instance.setContentManagementId(context.getContentManagementId());
		instance.setRevision(context.getRevision());
		instance.setActive(context.getActive());
		instance.setOwningReference(context.getOwningReference());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<WorkflowInstanceContext> loadInstances(Instance owner) {
		return getCurrentWorkflow(owner);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public WorkflowInstanceContext loadByDbId(Serializable id) {
		return instanceDao.loadInstance(id, null, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public WorkflowInstanceContext load(Serializable instanceId) {
		return instanceDao.loadInstance(null, instanceId, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<WorkflowInstanceContext> load(List<S> ids) {
		return loadContexts(ids);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<WorkflowInstanceContext> loadByDbId(List<S> ids) {
		return instanceDao.loadInstancesByDbKey(ids);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<WorkflowInstanceContext> load(List<S> ids,
			boolean allProperties) {
		return instanceDao.loadInstances(ids, allProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<WorkflowInstanceContext> loadByDbId(List<S> ids,
			boolean allProperties) {
		return instanceDao.loadInstancesByDbKey(ids, allProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Map<String, List<DefinitionModel>> getAllowedChildren(WorkflowInstanceContext owner) {
		return AllowedChildrenHelper.getAllowedChildren(owner, allowedChildCalculator,
				dictionaryService);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<DefinitionModel> getAllowedChildren(WorkflowInstanceContext owner, String type) {
		return AllowedChildrenHelper.getAllowedChildren(owner, allowedChildCalculator,
				dictionaryService, type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isChildAllowed(WorkflowInstanceContext owner, String type) {
		return AllowedChildrenHelper.isChildAllowed(owner, allowedChildCalculator,
				dictionaryService, type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Class<WorkflowDefinition> getInstanceDefinitionClass() {
		return WorkflowDefinition.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Secure
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public WorkflowInstanceContext cancel(WorkflowInstanceContext context) {
		return cancelInternal(context, new Operation(ActionTypeConstants.STOP), false, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Secure
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void delete(WorkflowInstanceContext instance, Operation operation, boolean permanent) {
		cancelInternal(instance, operation, true, permanent);
	}

	/**
	 * Cancel a workflow with given operation id. All related events are fired. DMS cancelation is
	 * processed if {@link RuntimeConfiguration} it not set for
	 * {@link RuntimeConfigurationProperties#DO_NO_CALL_DMS}
	 * 
	 * @param context
	 *            is the workflow
	 * @param operation
	 *            is the operation to execute
	 * @param isDelete
	 *            the is delete
	 * @param permanent
	 *            the permanent
	 * @return the context on success or null. Throws exception on dms failure
	 */
	private WorkflowInstanceContext cancelInternal(WorkflowInstanceContext context,
			Operation operation, boolean isDelete, boolean permanent) {
		if (context == null) {
			LOGGER.error("Called {} workflow for null instnace! Check client code.",
					isDelete ? "delete" : "cancel");
			return null;
		}
		if (debug) {
			LOGGER.debug("Called {} workflow for {}:{}", isDelete ? "delete" : "cancel",
					context.getIdentifier(), context.getWorkflowInstanceId());
		}
		if (!context.isActive()) {
			LOGGER.warn("Trying to {} completed/canceled workflow: {}:{}", isDelete ? "delete"
					: "cancel", context.getIdentifier(), context.getWorkflowInstanceId());
			return null;
		}
		boolean couldCallDms = StringUtils.isNotNullOrEmpty(context.getWorkflowInstanceId())
				&& !RuntimeConfiguration
						.isConfigurationSet(RuntimeConfigurationProperties.DO_NO_CALL_DMS);
		// if delete operation we could have a missing workflow instance id
		if (StringUtils.isNullOrEmpty(context.getWorkflowInstanceId()) && !isDelete) {
			LOGGER.warn("Called cancel workflow on not started workflow!");
			return null;
		}
		try {
			InstanceEventType eventType = isDelete ? InstanceEventType.DELETE
					: InstanceEventType.STOP;
			InstanceEventProvider<Instance> wfEventProvider = register.getEventProvider(context);
			InstanceEventProvider<Instance> taskEventProvider = register
					.getEventProvider(TaskInstance.class);

			notifyForExecutedOperation(context, operation);
			// initialize a case instance into the context so that all events can update a single
			// copy and not several
			Instance instance = context.getOwningInstance();

			// notify we a going to delete/cancel the given workflow
			EmfEvent event = wfEventProvider.createEvent(eventType, context);
			// fire the initial event before the tasks events
			eventService.fire(event);

			// collect all tasks that are going to be canceled
			// DONT remove the task (@Deprecated remove all tasks for the current workflow)
			// taskService.removeContextTasks(context, false);
			List<TaskInstance> activeTasks = getWorkflowTasks(context, TaskState.IN_PROGRESS);

			Map<Serializable, EmfEvent> taskCancelEvents = CollectionUtils
					.createHashMap(activeTasks.size());
			for (TaskInstance taskInstance : activeTasks) {
				EmfEvent taskEvent = taskEventProvider.createEvent(eventType, taskInstance);
				eventService.fire(taskEvent);
				// store the events to fire the next phase after cancellation before save
				taskCancelEvents.put(taskInstance.getId(), taskEvent);
			}

			// sometimes we does not want to call the DMS system (see close case operation)
			if (couldCallDms) {
				if (isDelete) {
					// workflow deletion in DMS
					adapterService.deleteWorkflow(context, permanent);
				} else {
					// workflow cancellation in DMS
					adapterService.cancelWorkflow(context);
				}
			}

			// set completed state, fire next event and persist
			for (TaskInstance taskInstance : activeTasks) {
				// process transition
				notifyForExecutedOperation(taskInstance, operation);
				taskInstance.setState(TaskState.COMPLETED);

				// fire the second part of the event before the save to persist any changes of the
				// task before the actual completion
				eventService.fireNextPhase((TwoPhaseEvent) taskCancelEvents.get(taskInstance
						.getId()));

				// store the information of the task in the local DB only, because there is not task
				// in DMS anymore and will throw an exception
				persistTask(context, taskInstance, taskInstance.getParentTask(), instance, true,
						operation);
			}

			// mark the workflow as canceled
			completeWorkflow(context, operation.getOperation());

			// we have cancelled the workflow, fire the second part of the event before the last
			// save of the workflow instance
			eventService.fireNextPhase((TwoPhaseEvent) event);

			WorkflowDefinition workflowDefinition = getDefinition(context);
			// this is for backward compatibility ONLY
			TaskDefinitionRef taskDefinitionRef = WorkflowHelper
					.getWorkflowPreviewTask(workflowDefinition);
			Transitional model = taskDefinitionRef;
			if (model == null) {
				model = workflowDefinition;
			}

			TransitionDefinition transitionDefinition = WorkflowHelper.getTransitionById(model,
					TaskProperties.TRANSITION_CANCEL);
			updateOwningInstanceForWorkflowEnd(context, ActionTypeConstants.EDIT_DETAILS,
					transitionDefinition, instance);
			// update and save the workflow
			save(context, operation);
			return context;
		} catch (DMSException e) {
			throw new EmfRuntimeException("Failed to cancel workflow "
					+ context.getWorkflowInstanceId() + " due to: " + e.getMessage(), e);
		} finally {
			// clear the case instance at the end of the call
			// context.setOwningInstance(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WorkflowInstanceContext clone(WorkflowInstanceContext instance, Operation operation) {
		return null;
	}

	@Override
	public void attach(WorkflowInstanceContext targetInstance, Operation operation,
			Instance... children) {
		// nothing to attach
	}

	@Override
	public void detach(WorkflowInstanceContext sourceInstance, Operation operation,
			Instance... instances) {
		// nothing to detach
	}

}
