package com.sirma.itt.objects.services.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.dao.AllowedChildrenProvider;
import com.sirma.itt.emf.definition.dao.AllowedChildrenTypeProvider;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstanceDeleteEvent;
import com.sirma.itt.emf.event.instance.AfterInstancePersistEvent;
import com.sirma.itt.emf.event.instance.BeforeInstanceDeleteEvent;
import com.sirma.itt.emf.event.instance.BeforeInstancePersistEvent;
import com.sirma.itt.emf.event.instance.InstanceAttachedEvent;
import com.sirma.itt.emf.event.instance.InstanceDetachedEvent;
import com.sirma.itt.emf.instance.dao.AllowedChildrenHelper;
import com.sirma.itt.emf.instance.dao.BaseAllowedChildrenProvider;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceEventProvider;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.DMSInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.rendition.RenditionService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.state.operation.event.OperationExecutedEvent;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.objects.constants.ObjectProperties;
import com.sirma.itt.objects.exceptions.DmsObjectException;
import com.sirma.itt.objects.security.ObjectActionTypeConstants;
import com.sirma.itt.objects.services.adapters.CMFObjectInstanceAdapterService;

/**
 * Default common implementation for all instances that are handled by semantic db. The
 * implementation will work for instances that are similar to
 * {@link com.sirma.itt.objects.domain.model.ObjectInstance}.
 * 
 * @author BBonev
 * @param <I>
 *            the generic instance type
 * @param <D>
 *            the generic definition type
 */
public abstract class BaseSemanticInstanceServiceImpl<I extends Instance, D extends DefinitionModel>
		implements InstanceService<I, D> {

	/** The adapter service. */
	@Inject
	private CMFObjectInstanceAdapterService adapterService;
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

	/** The instance service. */
	@Inject
	@Proxy
	protected InstanceService<Instance, DefinitionModel> instanceService;
	@Inject
	private RenditionService renditionService;

	/**
	 * Creates the instance.
	 *
	 * @param definition
	 *            the definition
	 * @param operation
	 *            the operation
	 * @return the project instance
	 */
	private I createInstance(D definition, Operation operation) {
		I instance = getInstanceDao().createInstance(definition, true);

		getInstanceDao().instanceUpdated(instance, false);
		// set the initial state
		eventService.fire(new OperationExecutedEvent(operation, instance));
		InstanceEventProvider<Instance> eventProvider = serviceRegister.getEventProvider(instance);
		if (eventProvider != null) {
			eventService.fire(eventProvider.createCreateEvent(instance));
		}

		// fixes not set revision
		getInstanceDao().synchRevisions(instance, instance.getRevision());
		return instance;
	}

	/**
	 * Save instance.
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @return the project instance
	 */
	private I saveInstance(I instance, Operation operation) {
		// 1. call the adapter 2. Convert to caseEntity 3.persist data, 4.
		// persist properties
		if (operation != null) {
			RuntimeConfiguration.setConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION,
					operation.getOperation());
		}
		TimeTracker tracker = TimeTracker.createAndStart();
		try {
			InstanceEventProvider<Instance> eventProvider = serviceRegister
					.getEventProvider(instance);
			// update project instance state if needed
			eventService.fire(new OperationExecutedEvent(operation, instance));

			boolean onCreate = false;
			BeforeInstancePersistEvent<Instance, ? extends AfterInstancePersistEvent<Instance, TwoPhaseEvent>> persistEvent = null;

			// set the properties that relates to modifications but does not
			// save it
			getInstanceDao().instanceUpdated(instance, false);

			Serializable serializable = instance.getProperties().remove(
					ObjectProperties.DEFAULT_VIEW);
			DocumentInstance view = null;
			if (serializable instanceof DocumentInstance) {
				view = (DocumentInstance) serializable;
			}

			if (instance instanceof DMSInstance) {
				if (((DMSInstance) instance).getDmsId() == null) {
					onCreate = true;
					if (eventProvider != null) {
						eventService.fire(eventProvider.createChangeEvent(instance));
						persistEvent = eventProvider.createBeforeInstancePersistEvent(instance);
					}

					eventService.fire(persistEvent);
					// create the case instance in the DMS and update the fields related
					// to creating a case
					onCreateObject((DMSInstance) instance);
				} else {
					if (eventProvider != null) {
						eventService.fire(eventProvider.createChangeEvent(instance));
					}
					// set that the case has been updated and save to DMS
					onUpdateObject((DMSInstance) instance);
				}
			}

			try {
				if (view != null) {
					instance.getProperties().put(ObjectProperties.DEFAULT_VIEW, view);
				}

				// persist entity and properties
				I old = getInstanceDao().persistChanges(instance);

				if (view != null) {
					instance.getProperties().put(ObjectProperties.DEFAULT_VIEW, view);
				}
				if (onCreate) {
					if (persistEvent != null) {
						eventService.fireNextPhase(persistEvent);
					}
				}
				if (!RuntimeConfiguration
						.isConfigurationSet(RuntimeConfigurationProperties.DO_NOT_FIRE_PERSIST_EVENT)) {
					if (eventProvider!=null) {
						eventService.fire(eventProvider.createPersistedEvent(instance, old,
								getOperationId(operation)));
					}
				}
			} catch (RuntimeException e) {
				// delete the instance from DMS site
				if (onCreate && (instance instanceof DMSInstance)) {
					try {
						adapterService.deleteInstance((DMSInstance) instance, true);
					} catch (DMSException e1) {
						throw new DmsObjectException(
								"Failed to delete object instance from DMS on rollback", e);
					}
				}
				throw e;
			}
		} finally {
			getLogger().debug("Total semantic instance {}  save took {} s", instance.getId(),
					tracker.stopInSeconds());
			if (operation != null) {
				RuntimeConfiguration
						.clearConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION);
			}
			instance.getProperties().remove(ObjectProperties.DEFAULT_VIEW);
		}
		return instance;
	}

	/**
	 * On update project.
	 *
	 * @param instance
	 *            the instance
	 */
	private void onUpdateObject(DMSInstance instance) {
		try {
			adapterService.updateInstance(instance);
		} catch (Exception e) {
			throw new DmsObjectException("Error updating object instance in DMS", e);
		}
	}

	/**
	 * On create project.
	 *
	 * @param instance
	 *            the instance
	 */
	private void onCreateObject(DMSInstance instance) {
		try {
			String dmsId = adapterService.createInstance(instance);
			instance.setDmsId(dmsId);
		} catch (Exception e) {
			throw new DmsObjectException("Error creating object instance in DMS", e);
		}
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
	private <S extends Serializable> List<I> batchLoadInstances(List<S> dmsIds,
			boolean loadAllProperties) {
		if ((dmsIds == null) || dmsIds.isEmpty()) {
			return Collections.emptyList();
		}
		return getInstanceDao().loadInstances(dmsIds, loadAllProperties);
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
	private <S extends Serializable> List<I> batchLoadInstancesById(List<S> ids,
			boolean loadAllProperties) {
		if ((ids == null) || ids.isEmpty()) {
			return Collections.emptyList();
		}

		return getInstanceDao().loadInstancesByDbKey(ids, loadAllProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public I createInstance(D definition,
			com.sirma.itt.emf.instance.model.Instance parent) {
		return createInstance(definition, new Operation(ObjectActionTypeConstants.CREATE_OBJECT));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public I createInstance(D definition,
			com.sirma.itt.emf.instance.model.Instance parent, Operation operation) {
		return createInstance(definition, operation);
	}

	/**
	 * {@inheritDoc}
	 */
	@Secure
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public I save(I instance, Operation operation) {
		return saveInstance(instance, operation);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<I> loadInstances(com.sirma.itt.emf.instance.model.Instance owner) {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public I loadByDbId(Serializable id) {
		I instance = getInstanceDao().loadInstance(id, null, true);
		return renditionService.loadThumbnail(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public I load(Serializable instanceId) {
		I instance = getInstanceDao().loadInstance(null, instanceId, true);
		renditionService.loadThumbnail(instance);
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<I> load(List<S> ids) {
		return batchLoadInstances(ids, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<I> loadByDbId(List<S> ids) {
		return batchLoadInstancesById(ids, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<I> load(List<S> ids, boolean allProperties) {
		return batchLoadInstances(ids, allProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<I> loadByDbId(List<S> ids,
			boolean allProperties) {
		return batchLoadInstancesById(ids, allProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Map<String, List<DefinitionModel>> getAllowedChildren(I owner) {
		// TODO: implement more specific provider
		AllowedChildrenProvider<I> calculator = new BaseAllowedChildrenProvider<I>(
				dictionaryService, typeProvider);
		return AllowedChildrenHelper.getAllowedChildren(owner, calculator, dictionaryService);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<DefinitionModel> getAllowedChildren(I owner, String type) {
		// TODO: implement more specific provider
		AllowedChildrenProvider<I> calculator = new BaseAllowedChildrenProvider<I>(
				dictionaryService, typeProvider);
		return AllowedChildrenHelper.getAllowedChildren(owner, calculator, dictionaryService, type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isChildAllowed(I owner, String type) {
		AllowedChildrenProvider<I> calculator = new BaseAllowedChildrenProvider<I>(
				dictionaryService, typeProvider);
		return AllowedChildrenHelper.isChildAllowed(owner, calculator, dictionaryService, type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void refresh(I instance) {
		getInstanceDao().loadProperties(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Secure
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public I cancel(I instance) {
		return save(instance, new Operation(ActionTypeConstants.STOP));
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public I clone(I instanceToClone, Operation operation) {
		D definition = dictionaryService.getDefinition(getInstanceDefinitionClass(),
				instanceToClone.getIdentifier());
		// Should throw an event and what should be the target of the event?
		eventService.fire(new OperationExecutedEvent(operation, instanceToClone));

		I instance = createInstance(definition, null, operation);

		for (Entry<String, Serializable> propertyEntry : instanceToClone.getProperties().entrySet()) {
			// Checks if property is not in the NOT_CLONABLE list and that it exists in object
			// definition (to avoid idoc custom properties)
			if (!DefaultProperties.NOT_CLONABLE_PROPERTIES.contains(propertyEntry.getKey())) {
				/*
				 * boolean existsInDef = false; for (int i = 0; (i < fields.size()) && !existsInDef;
				 * i++) { PropertyDefinition fieldDefinition = fields.get(i); existsInDef =
				 * fieldDefinition.getName().equals(propertyEntry.getKey()); } if (existsInDef) {
				 */
				instance.getProperties().put(propertyEntry.getKey(), propertyEntry.getValue());
				// }
			}
		}
		return instance;
	}

	@Secure
	@Override
	public void delete(I instance, Operation operation, boolean permanent) {
		InstanceEventProvider<Instance> eventProvider = serviceRegister.getEventProvider(instance);
		BeforeInstanceDeleteEvent<Instance, ? extends AfterInstanceDeleteEvent<Instance, TwoPhaseEvent>> event = null;
		if (eventProvider!=null) {
			event = eventProvider.createBeforeInstanceDeleteEvent(instance);
		}

		eventService.fire(event);
		// change the state of the object
		saveInstance(instance, operation);
		// delete the instance
		getInstanceDao().delete(instance);
		eventService.fireNextPhase(event);
	}

	@Override
	public void attach(I targetInstance, Operation operation, Instance... children) {
		List<Instance> list = getInstanceDao().attach(targetInstance, operation, children);
		InstanceEventProvider<Instance> eventProvider = serviceRegister
				.getEventProvider(targetInstance);
		for (Instance instance : list) {
			InstanceAttachedEvent<Instance> event = eventProvider.createAttachEvent(targetInstance,
					instance);
			eventService.fire(event);
		}
	}

	@Override
	public void detach(I sourceInstance, Operation operation, Instance... instances) {
		List<Instance> list = getInstanceDao().detach(sourceInstance, operation, instances);
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

	/**
	 * Getter method for instanceDao.
	 * 
	 * @return the instanceDao
	 */
	protected abstract InstanceDao<I> getInstanceDao();

	/**
	 * Getter method for lOGGER.
	 * 
	 * @return the lOGGER
	 */
	protected abstract Logger getLogger();
}
