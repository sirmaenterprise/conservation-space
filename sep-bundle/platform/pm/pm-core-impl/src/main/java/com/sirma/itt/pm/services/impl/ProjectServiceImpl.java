package com.sirma.itt.pm.services.impl;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.services.impl.LinkReferencesDeletionService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.dao.AllowedChildrenProvider;
import com.sirma.itt.emf.definition.dao.AllowedChildrenTypeProvider;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.InstanceAttachedEvent;
import com.sirma.itt.emf.event.instance.InstanceDetachedEvent;
import com.sirma.itt.emf.instance.dao.AllowedChildrenHelper;
import com.sirma.itt.emf.instance.dao.BaseAllowedChildrenProvider;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceEventProvider;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.scheduler.SchedulerConfiguration;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.scheduler.SchedulerEntryType;
import com.sirma.itt.emf.scheduler.SchedulerService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.state.operation.event.OperationExecutedEvent;
import com.sirma.itt.pm.domain.ObjectTypesPm;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.event.BeforeProjectCancelEvent;
import com.sirma.itt.pm.event.BeforeProjectDeleteEvent;
import com.sirma.itt.pm.event.BeforeProjectPersistEvent;
import com.sirma.itt.pm.event.ProjectChangeEvent;
import com.sirma.itt.pm.event.ProjectCreateEvent;
import com.sirma.itt.pm.event.ProjectCreatedEvent;
import com.sirma.itt.pm.event.ProjectPersistedEvent;
import com.sirma.itt.pm.event.ProjectSaveEvent;
import com.sirma.itt.pm.exceptions.DmsProjectException;
import com.sirma.itt.pm.security.PmActionTypeConstants;
import com.sirma.itt.pm.services.ProjectService;
import com.sirma.itt.pm.services.adapter.CMFProjectInstanceAdapterService;

/**
 * Default implementation of project service.
 *
 * @author BBonev
 */
@Stateless
public class ProjectServiceImpl implements ProjectService {

	/** The adapter service. */
	@Inject
	private CMFProjectInstanceAdapterService adapterService;

	/** The instance dao. */
	@Inject
	@InstanceType(type = ObjectTypesPm.PROJECT)
	private InstanceDao<ProjectInstance> instanceDao;

	/** The event service. */
	@Inject
	private EventService eventService;

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The type provider. */
	@Inject
	private AllowedChildrenTypeProvider typeProvider;

	/** The service register. */
	@Inject
	private ServiceRegister serviceRegister;
	@Inject
	private LinkService linkService;
	@Inject
	private SchedulerService schedulerService;

	/**
	 * Creates the instance.
	 *
	 * @param definition
	 *            the definition
	 * @param operation
	 *            the operation
	 * @return the project instance
	 */
	private ProjectInstance createInstance(ProjectDefinition definition, Operation operation) {
		ProjectInstance instance = instanceDao.createInstance(definition.getIdentifier(),
				definition.getClass(), true);

		instanceDao.instanceUpdated(instance, false);

		// set the initial state
		eventService.fire(new OperationExecutedEvent(operation, instance));
		eventService.fire(new ProjectCreateEvent(instance));
		return instance;
	}

	/**
	 * Save instance.
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @param phaseEvent
	 *            event to fire before saving
	 * @return the project instance
	 */
	private ProjectInstance saveInstance(ProjectInstance instance, Operation operation,
			TwoPhaseEvent phaseEvent) {
		// 1. call the adapter 2. Convert to caseEntity 3.persist data, 4.
		// persist properties
		if (operation != null) {
			RuntimeConfiguration.setConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION,
					operation.getOperation());
		}
		try {
			// update project instance state if needed
			eventService.fire(new OperationExecutedEvent(operation, instance));

			boolean onCreate = false;

			// set the properties that relates to modifications but does not
			// save it
			instanceDao.instanceUpdated(instance, false);

			// fire the first part of the event
			eventService.fire(phaseEvent);

			if (instance.getDmsId() == null) {
				onCreate = true;

				eventService.fire(new ProjectChangeEvent(instance));
				// create the case instance in the DMS and update the fields related
				// to creating a case
				onCreateProject(instance);
			} else {
				eventService.fire(new ProjectChangeEvent(instance));
				// set that the case has been updated and save to DMS
				onUpdateProject(instance);
			}
			try {
				eventService.fire(new ProjectSaveEvent(instance));

				// persist entity and properties
				ProjectInstance old = instanceDao.persistChanges(instance);

				ProjectCreatedEvent event = null;
				if (onCreate) {
					event = new ProjectCreatedEvent(instance);
					eventService.fire(event);
				}
				// fire the next phase of the event before the second save
				eventService.fireNextPhase(phaseEvent);
				if (((event != null) && event.isHandled()) || (phaseEvent != null)) {
					instanceDao.persistChanges(instance);
				}

				eventService.fire(new ProjectPersistedEvent(instance, old,
						getOperationId(operation)));
			} catch (RuntimeException e) {
				// delete the instance from DMS site
				if (onCreate) {
					try {
						adapterService.deleteProjectInstance(instance, true);
					} catch (DMSException e1) {
						throw new DmsProjectException(
								"Failed to delete project instance from DMS on rollback", e);
					}
				}
				throw e;
			}
		} finally {
			if (operation != null) {
				RuntimeConfiguration
						.clearConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION);
			}
		}
		return instance;
	}

	/**
	 * On update project.
	 *
	 * @param instance
	 *            the instance
	 */
	@Secure
	private void onUpdateProject(ProjectInstance instance) {
		if (RuntimeConfiguration.isConfigurationSet(RuntimeConfigurationProperties.DO_NO_CALL_DMS)) {
			return;
		}
		try {
			adapterService.updateProjectInstance(instance);
		} catch (Exception e) {
			throw new DmsProjectException("Error updating project instance in DMS", e);
		}
	}

	/**
	 * On create project.
	 *
	 * @param instance
	 *            the instance
	 */
	private void onCreateProject(ProjectInstance instance) {
		if (RuntimeConfiguration.isConfigurationSet(RuntimeConfigurationProperties.DO_NO_CALL_DMS)) {
			return;
		}
		try {
			String dmsId = adapterService.createProjectInstance(instance);
			instance.setDmsId(dmsId);
		} catch (Exception e) {
			throw new DmsProjectException("Error creating project instance in DMS", e);
		}
	}

	/**
	 * Load instance.
	 *
	 * @param id
	 *            the id
	 * @return the project instance
	 */
	private ProjectInstance loadInstance(Serializable id) {
		return instanceDao.loadInstance(id, null, true);
	}

	/**
	 * Batch load instances.
	 *
	 * @param <S>
	 *            the generic type
	 * @param dmsIds
	 *            the dms ids
	 * @param loadAllProperties
	 *            the load all properties
	 * @return the list
	 */
	private <S extends Serializable> List<ProjectInstance> batchLoadInstances(List<S> dmsIds,
			boolean loadAllProperties) {
		if ((dmsIds == null) || dmsIds.isEmpty()) {
			return Collections.emptyList();
		}
		return instanceDao.loadInstances(dmsIds, loadAllProperties);
	}

	/**
	 * Batch load instances by id.
	 *
	 * @param <S>
	 *            the generic type
	 * @param ids
	 *            the ids
	 * @param loadAllProperties
	 *            the load all properties
	 * @return the list
	 */
	private <S extends Serializable> List<ProjectInstance> batchLoadInstancesById(List<S> ids,
			boolean loadAllProperties) {
		if ((ids == null) || ids.isEmpty()) {
			return Collections.emptyList();
		}
		return instanceDao.loadInstancesByDbKey(ids, loadAllProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public ProjectInstance createInstance(ProjectDefinition definition,
			com.sirma.itt.emf.instance.model.Instance parent) {
		return createInstance(definition, new Operation(PmActionTypeConstants.CREATE_PROJECT));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public ProjectInstance createInstance(ProjectDefinition definition,
			com.sirma.itt.emf.instance.model.Instance parent, Operation operation) {
		return createInstance(definition, operation);
	}

	/**
	 * {@inheritDoc}
	 */
	@Secure
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public ProjectInstance save(ProjectInstance instance, Operation operation) {
		TwoPhaseEvent event = null;
		if ((instance.getDmsId() == null)
				|| ((operation != null)
				&& (operation.getOperation() != null)
				&& operation.getOperation().contains("import"))) {
			event = new BeforeProjectPersistEvent(instance);
		}
		return saveInstance(instance, operation, event);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<ProjectInstance> loadInstances(com.sirma.itt.emf.instance.model.Instance owner) {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public ProjectInstance loadByDbId(Serializable id) {
		return loadInstance(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public ProjectInstance load(Serializable instanceId) {
		return loadInstance(instanceId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<ProjectInstance> load(List<S> ids) {
		return batchLoadInstances(ids, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<ProjectInstance> loadByDbId(List<S> ids) {
		return batchLoadInstancesById(ids, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<ProjectInstance> load(List<S> ids, boolean allProperties) {
		return batchLoadInstances(ids, allProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<ProjectInstance> loadByDbId(List<S> ids,
			boolean allProperties) {
		return batchLoadInstancesById(ids, allProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Map<String, List<DefinitionModel>> getAllowedChildren(ProjectInstance owner) {
		// TODO: implement more specific provider
		AllowedChildrenProvider<ProjectInstance> calculator = new BaseAllowedChildrenProvider<>(
				dictionaryService, typeProvider);
		return AllowedChildrenHelper.getAllowedChildren(owner, calculator, dictionaryService);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<DefinitionModel> getAllowedChildren(ProjectInstance owner, String type) {
		// TODO: implement more specific provider
		AllowedChildrenProvider<ProjectInstance> calculator = new BaseAllowedChildrenProvider<>(
				dictionaryService, typeProvider);
		return AllowedChildrenHelper.getAllowedChildren(owner, calculator, dictionaryService, type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isChildAllowed(ProjectInstance owner, String type) {
		AllowedChildrenProvider<ProjectInstance> calculator = new BaseAllowedChildrenProvider<>(
				dictionaryService, typeProvider);
		return AllowedChildrenHelper.isChildAllowed(owner, calculator, dictionaryService, type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Class<ProjectDefinition> getInstanceDefinitionClass() {
		return ProjectDefinition.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void refresh(ProjectInstance instance) {
		instanceDao.loadProperties(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Secure
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public ProjectInstance cancel(ProjectInstance instance) {
		cancelInternal(instance, new Operation(ActionTypeConstants.STOP), false);
		return instance;
	}

	@Override
	public ProjectInstance clone(ProjectInstance instance, Operation operation) {
		return null;
	}

	@Override
	@Secure
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void delete(ProjectInstance instance, Operation operation, boolean permanent) {
		cancelInternal(instance, operation, permanent);
	}

	/**
	 * Internal cancel of project
	 *
	 * @param instance
	 *            is the project to cancel/stop
	 * @param operation
	 *            is the executed operation stop/delete
	 * @param permanent
	 *            whether this is permanent
	 */
	private void cancelInternal(ProjectInstance instance, Operation operation, boolean permanent) {
		String configId = "_" + instance.getIdentifier();
		boolean isCancel = false;
		if ((operation != null) && (operation.getOperation() != null)) {
			isCancel = ActionTypeConstants.STOP.equals(operation.getOperation());
			configId = operation.getOperation() + configId;
		}
		TwoPhaseEvent event;
		if (isCancel) {
			event = new BeforeProjectCancelEvent(instance);
		} else {
			event = new BeforeProjectDeleteEvent(instance);
		}

		List<LinkReference> links = linkService.getLinks(instance.toReference(),
				LinkConstants.PARENT_TO_CHILD);

		SchedulerConfiguration configuration = null;
		SchedulerContext context = null;
		// we should create configuration and schedule something when there is nothing to delete
		if (!links.isEmpty()) {
			configuration = schedulerService.buildEmptyConfiguration(SchedulerEntryType.TIMED);
			configuration.setIdentifier(configId);
			// 2h delay TODO this probably should be config - currently no R
			configuration.setRetryDelay(2L * 60L * 60L);
			configuration.setMaxRetryCount(10);
			// populate context
			context = new SchedulerContext();
			// create a copy - could be kryo unknown impl
			context.put(LinkReferencesDeletionService.DELEATABLE, new LinkedList<>(links));
			context.put(LinkReferencesDeletionService.OPERATION, operation.getOperation());
			context.put(LinkReferencesDeletionService.PERMANENT, Boolean.valueOf(permanent));
			context.put(LinkReferencesDeletionService.CUSTOM_ERROR,
					"There are problems during deletion of project's (" + instance.getIdentifier()
							+ ") children! Action is rescheduled for execution in 2 hours");
		}

		saveInstance(instance, operation, event);

		if (!links.isEmpty()) {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.SECOND, 5);
			configuration.setScheduleTime(calendar.getTime());
			// schedule the bean execution
			schedulerService
					.schedule(LinkReferencesDeletionService.BEAN_ID, configuration, context);
		}
	}

	@Override
	public void attach(ProjectInstance targetInstance, Operation operation,
			com.sirma.itt.emf.instance.model.Instance... children) {
		List<Instance> list = instanceDao.attach(targetInstance, operation, children);
		InstanceEventProvider<Instance> eventProvider = serviceRegister
				.getEventProvider(targetInstance);
		for (Instance instance : list) {
			InstanceAttachedEvent<Instance> event = eventProvider.createAttachEvent(targetInstance,
					instance);
			eventService.fire(event);
		}
	}

	@Override
	public void detach(ProjectInstance sourceInstance, Operation operation,
			com.sirma.itt.emf.instance.model.Instance... instances) {
		List<Instance> list = instanceDao.detach(sourceInstance, operation, instances);
		InstanceEventProvider<Instance> eventProvider = serviceRegister
				.getEventProvider(sourceInstance);
		for (Instance instance : list) {
			InstanceDetachedEvent<Instance> event = eventProvider.createDetachEvent(sourceInstance,
					instance);
			eventService.fire(event);
		}
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
