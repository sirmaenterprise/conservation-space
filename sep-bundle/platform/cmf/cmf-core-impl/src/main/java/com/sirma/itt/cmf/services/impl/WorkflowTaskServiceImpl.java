package com.sirma.itt.cmf.services.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.LinkConstantsCmf;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.event.task.workflow.BeforeTaskPersistEvent;
import com.sirma.itt.cmf.event.task.workflow.TaskChangeEvent;
import com.sirma.itt.cmf.event.task.workflow.TaskCreateEvent;
import com.sirma.itt.cmf.event.task.workflow.TaskPersistedEvent;
import com.sirma.itt.cmf.services.WorkflowTaskService;
import com.sirma.itt.cmf.services.adapter.CMFWorkflowAdapterService;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Default implementation for a {@link TaskInstance} of the
 * {@link com.sirma.itt.emf.instance.dao.InstanceService}.
 *
 * @author BBonev
 */
@Stateless
public class WorkflowTaskServiceImpl implements WorkflowTaskService {

	private static final Operation OPERATION = new Operation();
	private static final Logger LOGGER = Logger.getLogger(WorkflowTaskServiceImpl.class);
	private static boolean debug = LOGGER.isDebugEnabled();
	/** The instance dao. */
	@Inject
	@InstanceType(type = ObjectTypesCmf.WORKFLOW_TASK)
	private InstanceDao<TaskInstance> instanceDao;

	@Inject
	@InstanceType(type = ObjectTypesCmf.WORKFLOW)
	private InstanceDao<WorkflowInstanceContext> workflowInstanceDao;

	@Inject
	private EventService eventService;

	/** The adapter service. */
	@Inject
	private CMFWorkflowAdapterService adapterService;

	@Inject
	@Proxy
	private InstanceService<Instance, DefinitionModel> proxyService;

	@Inject
	private LinkService linkService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public TaskInstance createInstance(TaskDefinitionRef definition, Instance parent) {
		return createInstance(definition, parent, OPERATION);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public TaskInstance createInstance(TaskDefinitionRef definition, Instance parent,
			Operation operation) {
		TaskInstance instance = instanceDao.createInstance(definition, true);
		if (parent instanceof WorkflowInstanceContext) {
			instance.setOwningInstance(parent);
			instance.setOwningReference(parent.toReference());
			instance.setContext((WorkflowInstanceContext) parent);
			// transfer links from workflow to task
			List<LinkReference> links = linkService.getLinks(parent.toReference(),
					LinkConstantsCmf.PROCESSES);
			for (LinkReference linkReference : links) {
				linkService.link(instance.toReference(), linkReference.getTo(),
						LinkConstantsCmf.PROCESSES, null,
						LinkConstantsCmf.DEFAULT_SYSTEM_PROPERTIES);
			}
		}
		instanceDao.instanceUpdated(instance, false);
		eventService.fire(new TaskCreateEvent(instance));
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public TaskInstance save(TaskInstance taskInstance, Operation operation) {
		if (operation != null) {
			RuntimeConfiguration.setConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION,
					operation.getOperation());
		}
		try {
			WorkflowInstanceContext context = taskInstance.getContext();

			if (debug) {
				LOGGER.debug("Firing event (type=" + taskInstance.getIdentifier()
						+ ", taskOperation=create) for " + context.getIdentifier() + ":"
						+ context.getWorkflowInstanceId());
			}
			boolean updateHandled = true;
			BeforeTaskPersistEvent beforeTaskPersistEvent = null;
			if (!SequenceEntityGenerator.isPersisted(taskInstance)) {
				updateHandled = false;
				if (StringUtils.isNullOrEmpty(taskInstance.getContentManagementId())) {
					taskInstance.setContentManagementId(taskInstance.getTaskInstanceId());
				}
				beforeTaskPersistEvent = new BeforeTaskPersistEvent(taskInstance,
						taskInstance.getParentTask());
				eventService.fire(beforeTaskPersistEvent);
				updateHandled |= beforeTaskPersistEvent.isHandled();
			}

			boolean callDms = !RuntimeConfiguration
					.isConfigurationSet(RuntimeConfigurationProperties.DO_NO_CALL_DMS);
			// if handled then we need to persist the changes of the task
			if (callDms && updateHandled) {
				try {
					adapterService.updateTask(taskInstance, null);
				} catch (DMSException e) {
					throw new EmfRuntimeException("Failed to update task instance "
							+ taskInstance.getTaskInstanceId() + " due to: " + e.getMessage(), e);
				}
			}
			instanceDao.instanceUpdated(taskInstance, false);
			eventService.fire(new TaskChangeEvent(taskInstance));
			// add the parent
			TaskInstance old = instanceDao.persistChanges(taskInstance);
			if (beforeTaskPersistEvent != null) {
				eventService.fireNextPhase(beforeTaskPersistEvent);
			}
			eventService.fire(new TaskPersistedEvent(taskInstance, old, getOperationId(operation)));
		} finally {
			if (operation != null) {
				RuntimeConfiguration
						.clearConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION);
			}
		}
		return taskInstance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<TaskInstance> loadInstances(Instance owner) {
		List<TaskInstance> instances = instanceDao.loadInstances(owner, true);
		populateWorkflowInstances(instances, true);
		return instances;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public TaskInstance loadByDbId(Serializable id) {
		TaskInstance instance = instanceDao.loadInstance(id, null, true);
		if (instance != null) {
			instance.setContext(workflowInstanceDao.loadInstance(null,
					instance.getWorkflowInstanceId(), true));
		}
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public TaskInstance load(Serializable instanceId) {
		TaskInstance instance = instanceDao.loadInstance(null, instanceId, true);
		if (instance != null) {
			instance.setContext(workflowInstanceDao.loadInstance(null,
					instance.getWorkflowInstanceId(), true));
		}
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<TaskInstance> load(List<S> ids) {
		return load(ids, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<TaskInstance> loadByDbId(List<S> ids) {
		return loadByDbId(ids, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<TaskInstance> load(List<S> ids, boolean allProperties) {
		List<TaskInstance> list = instanceDao.loadInstances(ids, allProperties);
		populateWorkflowInstances(list, allProperties);
		return list;
	}

	/**
	 * Populate workflow instances.
	 *
	 * @param list
	 *            the list
	 * @param allProperties
	 *            the all properties
	 */
	private void populateWorkflowInstances(List<TaskInstance> list, boolean allProperties) {
		Map<String, List<TaskInstance>> mapping = CollectionUtils.createLinkedHashMap(list.size());
		for (TaskInstance taskInstance : list) {
			CollectionUtils.addValueToMap(mapping, taskInstance.getWorkflowInstanceId(),
					taskInstance);
		}
		List<WorkflowInstanceContext> workflows = workflowInstanceDao.loadInstances(
				new ArrayList<>(mapping.keySet()), allProperties);
		for (WorkflowInstanceContext context : workflows) {
			List<TaskInstance> tasks = mapping.get(context.getWorkflowInstanceId());
			for (TaskInstance taskInstance : tasks) {
				taskInstance.setContext(context);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<TaskInstance> loadByDbId(List<S> ids, boolean allProperties) {
		List<TaskInstance> list = instanceDao.loadInstancesByDbKey(ids, allProperties);
		populateWorkflowInstances(list, allProperties);
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Map<String, List<DefinitionModel>> getAllowedChildren(TaskInstance owner) {
		return Collections.emptyMap();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<DefinitionModel> getAllowedChildren(TaskInstance owner, String type) {
		return Collections.emptyList();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isChildAllowed(TaskInstance owner, String type) {
		return false;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Class<TaskDefinitionRef> getInstanceDefinitionClass() {
		return TaskDefinitionRef.class;
	}

	@Override
	public void refresh(TaskInstance instance) {
		instanceDao.loadProperties(instance);
		if (instance.getContext() == null) {
			// changed to load the properties of the context and set them
			instance.setContext(workflowInstanceDao.loadInstance(null,
					instance.getWorkflowInstanceId(), true));
			// no need to continue
			return;
		}
		proxyService.refresh(instance.getContext());
		// update the merged properties after refresh -- the task refresh clears the WF specific
		// properties
		instance.setContext(instance.getContext());
	}

	@Secure
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public TaskInstance cancel(TaskInstance instance) {
		return save(instance, new Operation(ActionTypeConstants.STOP));
	}

	@Override
	public TaskInstance clone(TaskInstance instance, Operation operation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(TaskInstance instance, Operation operation, boolean permanent) {

	}

	@Override
	public void attach(TaskInstance targetInstance, Operation operation, Instance... children) {
		// TODO Auto-generated method stub

	}

	@Override
	public void detach(TaskInstance sourceInstance, Operation operation, Instance... instances) {
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
