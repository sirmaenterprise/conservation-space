package com.sirma.itt.cmf.services.impl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.cmf.beans.definitions.TaskDefinition;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.event.task.standalone.BeforeStandaloneTaskCancelEvent;
import com.sirma.itt.cmf.event.task.standalone.BeforeStandaloneTaskDeleteEvent;
import com.sirma.itt.cmf.event.task.standalone.BeforeStandaloneTaskPersistEvent;
import com.sirma.itt.cmf.event.task.standalone.BeforeStandaloneTaskStartEvent;
import com.sirma.itt.cmf.event.task.standalone.BeforeStandaloneTaskTransitionEvent;
import com.sirma.itt.cmf.event.task.standalone.StandaloneTaskChangeEvent;
import com.sirma.itt.cmf.event.task.standalone.StandaloneTaskCompletedEvent;
import com.sirma.itt.cmf.event.task.standalone.StandaloneTaskCreateEvent;
import com.sirma.itt.cmf.event.task.standalone.StandaloneTaskPersistedEvent;
import com.sirma.itt.cmf.services.StandaloneTaskService;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.cmf.services.adapter.CMFWorkflowAdapterService;
import com.sirma.itt.cmf.workflows.TaskTransitionBinding;
import com.sirma.itt.cmf.workflows.TaskTypeBinding;
import com.sirma.itt.cmf.workflows.WorkflowHelper;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.dao.AllowedChildrenProvider;
import com.sirma.itt.emf.definition.dao.AllowedChildrenTypeProvider;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.definition.model.Transitional;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.dao.AllowedChildrenHelper;
import com.sirma.itt.emf.instance.dao.BaseAllowedChildrenProvider;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.DmsAware;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceContext;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.ActionRegistry;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.state.operation.event.OperationExecutedEvent;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Default implementation for {@link TaskService}.
 *
 * @author BBonev
 */
@Stateless
public class StandaloneTaskServiceImpl implements StandaloneTaskService {

	private static final Logger LOGGER = Logger.getLogger(StandaloneTaskServiceImpl.class);
	/** The instance dao. */
	@Inject
	@InstanceType(type = ObjectTypesCmf.STANDALONE_TASK)
	private InstanceDao<StandaloneTaskInstance> instanceDao;

	@Inject
	private DictionaryService dictionaryService;

	/** The event service. */
	@Inject
	private EventService eventService;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/** The adapter service. */
	@Inject
	private CMFWorkflowAdapterService adapterService;

	@Inject
	private AllowedChildrenTypeProvider allowedChildrenTypeProvider;

	@Inject
	private javax.enterprise.inject.Instance<TaskService> taskService;

	@Inject
	private ActionRegistry actionRegistry;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public StandaloneTaskInstance createInstance(TaskDefinition definition, Instance parent) {
		return createInstance(definition, parent, new Operation(ActionTypeConstants.CREATE_TASK));
	}

	@Override
	public StandaloneTaskInstance createInstance(TaskDefinition definition, Instance parent,
			Operation operation) {
		StandaloneTaskInstance instance = instanceDao.createInstance(definition, true);
		InstanceReference reference = typeConverter.convert(InstanceReference.class, parent);
		instance.setOwningReference(reference);
		instance.setOwningInstance(parent);
		Instance context = parent;
		if (!(parent instanceof InstanceContext)) {
			context = InstanceUtil.getContext(parent, true);
		}
		if (context instanceof DmsAware) {
			instance.setParentContextId(((DmsAware) context).getDmsId());
		}

		eventService.fire(new OperationExecutedEvent(operation, instance));
		// fire create event
		eventService.fire(new StandaloneTaskCreateEvent(instance, parent));
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Secure
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public StandaloneTaskInstance start(StandaloneTaskInstance instance, Operation operation) {
		if (operation != null) {
			RuntimeConfiguration.setConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION,
					operation.getOperation());
		}
		try {
			TaskDefinition model = (TaskDefinition) dictionaryService
					.getInstanceDefinition(instance);
			String startTransition = TaskProperties.TRANSITION_START_WORKFLOW;
			TransitionDefinition transition = null;
			if ((operation != null) && StringUtils.isNotNullOrEmpty(operation.getOperation())) {
				transition = WorkflowHelper.getDefaultTransition(model);
				if (transition != null) {
					startTransition = transition.getIdentifier();
				} else {
					startTransition = operation.getOperation();
				}
			}

			eventService.fire(new OperationExecutedEvent(operation, instance));

			instance.getProperties().put(TaskProperties.ACTUAL_START_DATE, new Date());
			instanceDao.setCurrentUserTo(instance, TaskProperties.START_BY);
			TaskService taskServiceLocal = taskService.get();
			if (!taskServiceLocal.isPooledTask(instance)) {
				CollectionUtils.copyValue(instance, TaskProperties.TASK_ASSIGNEE, instance,
						TaskProperties.TASK_OWNER);
			}
			// fire event for before task start
			instanceDao.instanceUpdated(instance, false);

			if (transition == null) {
				transition = WorkflowHelper.getTransitionById(model, startTransition);
			}

			BeforeStandaloneTaskStartEvent event = new BeforeStandaloneTaskStartEvent(instance,
					operation);
			TaskTypeBinding typeBinding = new TaskTypeBinding(instance.getIdentifier());
			TaskTransitionBinding transitionBinding = transition == null ? null
					: new TaskTransitionBinding(transition.getEventId());
			eventService.fire(event, typeBinding, transitionBinding);

			BeforeStandaloneTaskPersistEvent persistEvent = new BeforeStandaloneTaskPersistEvent(
					instance);
			eventService.fire(persistEvent);

			StandaloneTaskInstance updatedTask = adapterService.startTask(instance);
			// the list should be empty
			if (updatedTask != null) {
				instance.setTaskInstanceId(updatedTask.getTaskInstanceId());
				instance.getProperties().putAll(updatedTask.getProperties());
				instance.setState(TaskState.IN_PROGRESS);
				instance.setDmsId(updatedTask.getDmsId());
			}

			// fire event after task start
			eventService.fireNextPhase(event, typeBinding, transitionBinding);

			// update the parent path after the DB ID is generated
			String treePath = InstanceUtil.buildPath(instance, InstanceContext.class);
			instance.setTreePath(treePath);

			eventService.fire(new StandaloneTaskChangeEvent(instance));

			StandaloneTaskInstance old = instanceDao.persistChanges(instance);

			Instance context = InstanceUtil.getParentContext(instance);
			// pooled tasks are added as non active
			taskServiceLocal.attachTaskToInstance(context, Arrays.asList(instance),
					InstanceUtil.getDirectParent(instance),
					TaskState.IN_PROGRESS == instance.getState());
			eventService.fireNextPhase(persistEvent);
			eventService.fire(new StandaloneTaskPersistedEvent(instance, old,
					getOperationId(operation)));
		} catch (DMSException e) {
			throw new EmfRuntimeException(e);
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
	@Secure
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public StandaloneTaskInstance complete(StandaloneTaskInstance instance, Operation operation) {
		if (operation == null) {
			LOGGER.warn("Cannot complete Standalone task instance with undefined operation!");
			return instance;
		}
		RuntimeConfiguration.setConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION,
				operation.getOperation());
		try {
			DefinitionModel model = dictionaryService.getInstanceDefinition(instance);

			TransitionDefinition transitionDefinition = WorkflowHelper.getTransitionById(
					(Transitional) model, operation.getOperation());

			BeforeStandaloneTaskTransitionEvent beforeTransitionEvent = new BeforeStandaloneTaskTransitionEvent(
					instance, transitionDefinition);
			eventService.fire(beforeTransitionEvent);

			String transition = operation.getOperation();
			instance.setState(TaskState.COMPLETED);
			adapterService.transition(transition, instance);

			instanceDao.instanceUpdated(instance, false);

			eventService.fireNextPhase(beforeTransitionEvent);

			eventService.fire(new StandaloneTaskChangeEvent(instance));

			eventService.fire(new StandaloneTaskCompletedEvent(instance, operation,
					transitionDefinition));

			StandaloneTaskInstance old = instanceDao.persistChanges(instance);

			taskService.get().updateTaskStateAndAssignment(InstanceUtil.getContext(instance),
					InstanceUtil.getDirectParent(instance), instance, Boolean.FALSE);
			eventService.fire(new StandaloneTaskPersistedEvent(instance, old,
					getOperationId(operation)));
		} catch (DMSException e) {
			throw new EmfRuntimeException(e);
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
	@Secure
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public StandaloneTaskInstance cancel(StandaloneTaskInstance instance) {
		return cancelInternal(instance, true);
	}

	/**
	 * Cancel internal.
	 *
	 * @param instance
	 *            the instance
	 * @param cancelSubTasks
	 *            the cancel sub tasks
	 * @return the standalone task instance
	 */
	private StandaloneTaskInstance cancelInternal(StandaloneTaskInstance instance,
			boolean cancelSubTasks) {
		RuntimeConfiguration.setConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION,
				ActionTypeConstants.STOP);
		try {
			BeforeStandaloneTaskCancelEvent cancelEvent = new BeforeStandaloneTaskCancelEvent(
					instance);
			eventService.fire(cancelEvent);

			instance.setState(TaskState.COMPLETED);
			if (!RuntimeConfiguration
					.isConfigurationSet(RuntimeConfigurationProperties.DO_NO_CALL_DMS)) {
				// task cancellation in DMS
				adapterService.cancelTask(instance);
			}

			eventService.fireNextPhase(cancelEvent);

			// eventService.fire(new OperationExecutedEvent(operation, instance));
			instanceDao.instanceUpdated(instance, false);

			eventService.fire(new StandaloneTaskChangeEvent(instance));

			if (cancelSubTasks) {
				// get all sub tasks and cancel them also
				List<StandaloneTaskInstance> subTasks = taskService.get().getSubTasks(instance,
						TaskState.IN_PROGRESS, false);
				for (StandaloneTaskInstance abstractTaskInstance : subTasks) {
					cancelInternal(abstractTaskInstance, false);
				}
			}

			StandaloneTaskInstance old = instanceDao.persistChanges(instance);

			taskService.get().updateTaskStateAndAssignment(InstanceUtil.getContext(instance),
					InstanceUtil.getDirectParent(instance), instance, Boolean.FALSE);
			eventService.fire(new StandaloneTaskPersistedEvent(instance, old,
					ActionTypeConstants.STOP));

			return instance;
		} catch (DMSException e) {
			throw new EmfRuntimeException(e);
		} finally {
			RuntimeConfiguration
					.clearConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Secure
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public StandaloneTaskInstance save(StandaloneTaskInstance instance, Operation operation) {
		if (operation != null) {
			RuntimeConfiguration.setConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION,
					operation.getOperation());
		}
		try {
			boolean complete = (operation == null) || (operation.getOperation() == null);
			if ((operation != null) && (operation.getOperation() != null)) {
				// check if the given action is an operation
				Action action = actionRegistry.find(new Pair<Class<?>, String>(
						StandaloneTaskInstance.class, operation.getOperation()));
				complete = action == null;
				if ((action != null) && action.getActionId().equals(ActionTypeConstants.STOP)) {
					return cancel(instance);
				}
			}

			if (complete) {
				return complete(instance, new Operation(ActionTypeConstants.COMPLETE));
			}
			// else if not complete
			eventService.fire(new OperationExecutedEvent(operation, instance));

			instanceDao.instanceUpdated(instance, false);
			eventService.fire(new StandaloneTaskChangeEvent(instance));

			BeforeStandaloneTaskPersistEvent event = null;
			if (!SequenceEntityGenerator.isPersisted(instance)) {
				event = new BeforeStandaloneTaskPersistEvent(instance);
				eventService.fire(event);
			}

			try {
				adapterService.updateTask(instance, null);

				StandaloneTaskInstance old = instanceDao.persistChanges(instance);

				taskService.get().updateTaskStateAndAssignment(
						InstanceUtil.getDirectParent(instance, true),
						InstanceUtil.getContext(instance, true), instance,
						TaskState.IN_PROGRESS.equals(instance.getState()));

				if (event != null) {
					eventService.fireNextPhase(event);
				}
				eventService.fire(new StandaloneTaskPersistedEvent(instance, old,
						getOperationId(operation)));
			} catch (DMSException e) {
				LOGGER.warn("Failed to update task in DMS: ", e);
			}
			// add permissions update
			// add possible task reassing
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
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<StandaloneTaskInstance> loadInstances(Instance owner) {
		return instanceDao.loadInstances(owner, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public StandaloneTaskInstance loadByDbId(Serializable id) {
		return instanceDao.loadInstance(id, null, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public StandaloneTaskInstance load(Serializable instanceId) {
		return instanceDao.loadInstance(null, instanceId, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<StandaloneTaskInstance> load(List<S> ids) {
		return instanceDao.loadInstances(ids, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<StandaloneTaskInstance> loadByDbId(List<S> ids) {
		return instanceDao.loadInstancesByDbKey(ids, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Map<String, List<DefinitionModel>> getAllowedChildren(StandaloneTaskInstance owner) {
		// no allowed children on a task
		AllowedChildrenProvider<StandaloneTaskInstance> calculator = new BaseAllowedChildrenProvider<StandaloneTaskInstance>(
				dictionaryService, allowedChildrenTypeProvider);
		return AllowedChildrenHelper.getAllowedChildren(owner, calculator, dictionaryService);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<DefinitionModel> getAllowedChildren(StandaloneTaskInstance owner, String type) {
		AllowedChildrenProvider<StandaloneTaskInstance> calculator = new BaseAllowedChildrenProvider<StandaloneTaskInstance>(
				dictionaryService, allowedChildrenTypeProvider);
		return AllowedChildrenHelper.getAllowedChildren(owner, calculator, dictionaryService, type);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isChildAllowed(StandaloneTaskInstance owner, String type) {
		AllowedChildrenProvider<StandaloneTaskInstance> calculator = new BaseAllowedChildrenProvider<StandaloneTaskInstance>(
				dictionaryService, allowedChildrenTypeProvider);
		return AllowedChildrenHelper.isChildAllowed(owner, calculator, dictionaryService, type);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<StandaloneTaskInstance> load(List<S> ids,
			boolean allProperties) {
		return instanceDao.loadInstances(ids, allProperties);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<StandaloneTaskInstance> loadByDbId(List<S> ids,
			boolean allProperties) {
		return instanceDao.loadInstancesByDbKey(ids, allProperties);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Class<TaskDefinition> getInstanceDefinitionClass() {
		return TaskDefinition.class;
	}

	@Override
	public void refresh(StandaloneTaskInstance instance) {
		instanceDao.loadProperties(instance);
	}

	@Override
	public StandaloneTaskInstance clone(StandaloneTaskInstance instance, Operation operation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(StandaloneTaskInstance instance, Operation operation, boolean permanent) {
		deleteInternal(instance, operation, true);
	}

	/**
	 * Delete internal.
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @param deleteSubtasks
	 *            the delete subtasks
	 */
	private void deleteInternal(StandaloneTaskInstance instance, Operation operation,
			boolean deleteSubtasks) {
		RuntimeConfiguration.setConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION,
				ActionTypeConstants.DELETE);
		try {
			BeforeStandaloneTaskDeleteEvent beforeDeleteEvent = new BeforeStandaloneTaskDeleteEvent(
					instance);
			eventService.fire(beforeDeleteEvent);

			instance.setState(TaskState.COMPLETED);

			if (!RuntimeConfiguration
					.isConfigurationSet(RuntimeConfigurationProperties.DO_NO_CALL_DMS)) {
				// task cancellation in DMS
				adapterService.deleteTask(instance);
			}

			eventService.fire(new OperationExecutedEvent(operation, instance));
			// make changes visible
			instanceDao.instanceUpdated(instance, false);

			eventService.fire(new StandaloneTaskChangeEvent(instance));

			// should delete the subtasks
			if (deleteSubtasks) {
				List<StandaloneTaskInstance> subTasks = taskService.get().getSubTasks(instance,
						TaskState.ALL, false);
				for (StandaloneTaskInstance taskInstance : subTasks) {
					deleteInternal(taskInstance, operation, false);
				}
			}

			// save actually
			StandaloneTaskInstance old = instanceDao.persistChanges(instance);
			// update the task owner and state in mapping table
			taskService.get().updateTaskStateAndAssignment(InstanceUtil.getContext(instance),
					InstanceUtil.getDirectParent(instance), instance, Boolean.FALSE);
			eventService.fire(new StandaloneTaskPersistedEvent(instance, old,
					getOperationId(operation)));

			eventService.fireNextPhase(beforeDeleteEvent);
		} catch (DMSException e) {
			throw new EmfRuntimeException(e);
		} finally {
			RuntimeConfiguration
					.clearConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION);
		}
	}

	@Override
	public void attach(StandaloneTaskInstance targetInstance, Operation operation,
			Instance... children) {
		// TODO Auto-generated method stub

	}

	@Override
	public void detach(StandaloneTaskInstance sourceInstance, Operation operation,
			Instance... instances) {
		// TODO Auto-generated method stub

	}

	/**
	 * Gets the operation id.
	 *
	 * @param operation
	 *            the operation
	 * @return the operation id
	 */
	protected String getOperationId(Operation operation) {
		String operationId = null;
		if (operation != null) {
			operationId = operation.getOperation();
		}
		return operationId;
	}

}
