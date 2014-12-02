package com.sirma.itt.emf.instance.dao;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.dao.AllowedChildrenProvider;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.event.instance.BeforeInstancePersistEvent;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.model.DmsAware;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.state.operation.event.OperationExecutedEvent;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Base implementation for instance service. The implementation realizes base save algorithm and
 * uses provided instance dao for load methods.
 * 
 * @param <I>
 *            the instance type
 * @param <D>
 *            the instance definition
 * @author BBonev
 */
public abstract class BaseInstanceService<I extends Instance, D extends DefinitionModel> implements
		InstanceService<I, D> {
	/** The dictionary service. */
	@Inject
	protected DictionaryService dictionaryService;

	/** The event service. */
	@Inject
	protected EventService eventService;

	/** The type converter. */
	@Inject
	protected TypeConverter typeConverter;

	/** The service register. */
	@Inject
	protected ServiceRegister serviceRegister;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public I createInstance(D definition, Instance parent, Operation operation) {
		I instance = getInstanceDao().createInstance(definition, true);
		populateNewInstance(instance, definition);
		getInstanceDao().setCurrentUserTo(instance, DefaultProperties.CREATED_BY);

		if ((parent != null) && (instance instanceof OwnedModel)) {
			OwnedModel model = (OwnedModel) instance;
			// set parent reference if any
			InstanceReference reference = typeConverter.convert(InstanceReference.class, parent);
			model.setOwningReference(reference);
			model.setOwningInstance(parent);
		}

		notifyForStateChange(operation, instance);

		// fixes not set revision
		getInstanceDao().synchRevisions(instance, instance.getRevision());

		InstanceEventProvider<Instance> provider = serviceRegister
				.getEventProvider(getInstanceClass());
		if (provider != null) {
			eventService.fire(provider.createCreateEvent(instance));
		}
		return instance;
	}

	/**
	 * Called on new instance creation if any custom logic is needed to be executed when creating
	 * new instance. The method is called just after the instance dao created the instance.
	 * 
	 * @param instance
	 *            the instance
	 * @param definition
	 *            the definition
	 */
	protected abstract void populateNewInstance(I instance, D definition);

	/**
	 * Saves and updates the given instance. Should be called when a new instance is created,
	 * properties are changed or a new instance was added to the child or its children. Uses
	 * persistence and DMS layers for saving
	 * 
	 * @param instance
	 *            the instance to save/update
	 * @param operation
	 *            the performed operation
	 * @return the updated instance
	 */
	protected I saveInternal(I instance, Operation operation) {
		if (operation != null) {
			RuntimeConfiguration.setConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION,
					operation.getOperation());
		}
		// 1. call the adapter 2. Convert to caseEntity 3.persist data, 4.
		// persist properties
		TimeTracker tracker = new TimeTracker().begin();
		StringBuilder debugMessage = new StringBuilder();
		boolean debug = getLogger().isDebugEnabled();
		if (debug) {
			tracker.begin();
		}
		try {
			// update case instance state if needed
			notifyForStateChange(operation, instance);

			boolean onCreate = false;
			// we have an option to disable DMS call on case save
			// but if we have a files to be uploaded and the case is not created in DMS then the
			// upload
			// of the files will fail
			boolean callDms = !RuntimeConfiguration
					.isConfigurationSet(RuntimeConfigurationProperties.DO_NO_CALL_DMS);

			InstanceEventProvider<Instance> provider = serviceRegister
					.getEventProvider(getInstanceClass());

			// set the properties that relates to modifications but does not save it
			getInstanceDao().instanceUpdated(instance, false);

			BeforeInstancePersistEvent<Instance, ?> event = null;
			if (callDms && (instance instanceof DmsAware)) {
				DmsAware dmsInstance = (DmsAware) instance;
				if (dmsInstance.getDmsId() == null) {
					onCreate = true;
					if (provider != null) {
						event = provider.createBeforeInstancePersistEvent(instance);
					}
					eventService.fire(event);

					if (provider != null) {
						eventService.fire(provider.createChangeEvent(instance));
					}
					// create the case instance in the DMS and update the fields related
					// to creating a case
					String dmsId = createInstanceInDms(instance);
					dmsInstance.setDmsId(dmsId);
				} else {
					if (provider != null) {
						eventService.fire(provider.createChangeEvent(instance));
					}
					// set that the case has been updated and save to DMS
					updateInstanceInDms(instance);
				}
				if (debug) {
					debugMessage.append("\nDMS call for ").append(getClass().getSimpleName())
							.append(onCreate ? " create" : " update").append(" took ")
							.append(tracker.stopInSeconds()).append(" sec");
					tracker.begin();
				}
			}
			try {
				// persist entity and properties
				I old = getInstanceDao().persistChanges(instance);
				if (debug) {
					debugMessage.append("\nCMF persist ").append(getClass().getSimpleName())
							.append(" tool ").append(tracker.stopInSeconds()).append(" sec");
				}
				if (callDms) {
					tracker.begin();
					debugMessage.append("\nDMS call for updating document permissions took ")
							.append(tracker.stopInSeconds()).append(" sec");
				}
				if (debug) {
					debugMessage.append("\nTotal save took ").append(tracker.stopInSeconds())
							.append(" sec");
					getLogger().debug(debugMessage.toString());
				}
				if (event != null) {
					eventService.fireNextPhase(event);
				}
				if (!RuntimeConfiguration
						.isConfigurationSet(RuntimeConfigurationProperties.DO_NOT_FIRE_PERSIST_EVENT)) {
					if (provider != null) {
						eventService.fire(provider.createPersistedEvent(instance, old,
								getOperationId(operation)));
					}
				}
			} catch (RuntimeException e) {
				// delete the instance from DMS site
				if (onCreate) {
					try {
						if (SequenceEntityGenerator.isPersisted(instance)) {
							getInstanceDao().delete(instance);
						}
						deleteInstanceFromDms(instance, true);
					} catch (DMSException e1) {
						getLogger().trace("Failed to delete instance", e1);
						throw new EmfRuntimeException(
								"Failed to delete case instance from DMS on rollback", e);
					}
				}
				throw e;
			}
			return instance;
		} finally {
			if (operation != null) {
				RuntimeConfiguration
						.clearConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION);
			}
		}
	}

	/**
	 * Called when an instance should be deleted from DMS if needed.
	 * 
	 * @param instance
	 *            the instance
	 * @param permanent
	 *            the permanent
	 * @throws DMSException
	 *             the dMS exception
	 */
	protected void deleteInstanceFromDms(I instance, boolean permanent) throws DMSException {

	}

	/**
	 * On instance updated. Could be used for DMS calls.
	 * 
	 * @param instance
	 *            the instance
	 */
	protected void updateInstanceInDms(I instance) {

	}

	/**
	 * On instance created. Could be used for DMS calls.
	 * 
	 * @param instance
	 *            the instance
	 * @return the DMS id if any
	 */
	protected String createInstanceInDms(I instance) {
		return null;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<I> loadInstances(Instance owner) {
		return getInstanceDao().loadInstances(owner, true);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public I loadByDbId(Serializable id) {
		return getInstanceDao().loadInstance(id, null, true);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public I load(Serializable instanceId) {
		return getInstanceDao().loadInstance(null, instanceId, true);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<I> load(List<S> ids) {
		return load(ids, true);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<I> loadByDbId(List<S> ids) {
		return loadByDbId(ids, true);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<I> load(List<S> ids, boolean allProperties) {
		if ((ids == null) || ids.isEmpty()) {
			return CollectionUtils.emptyList();
		}
		return getInstanceDao().loadInstances(ids, allProperties);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<I> loadByDbId(List<S> ids, boolean allProperties) {
		if ((ids == null) || ids.isEmpty()) {
			return CollectionUtils.emptyList();
		}
		return getInstanceDao().loadInstancesByDbKey(ids, allProperties);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Map<String, List<DefinitionModel>> getAllowedChildren(I owner) {
		AllowedChildrenProvider<I> calculator = getAllowChildrenProvider();
		if (calculator == null) {
			return Collections.emptyMap();
		}
		return AllowedChildrenHelper.getAllowedChildren(owner, calculator, dictionaryService);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<DefinitionModel> getAllowedChildren(I owner, String type) {
		AllowedChildrenProvider<I> calculator = getAllowChildrenProvider();
		if (calculator == null) {
			return Collections.emptyList();
		}
		return AllowedChildrenHelper.getAllowedChildren(owner, calculator, dictionaryService, type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isChildAllowed(I owner, String type) {
		AllowedChildrenProvider<I> calculator = getAllowChildrenProvider();
		if (calculator == null) {
			return false;
		}
		return AllowedChildrenHelper.isChildAllowed(owner, calculator, dictionaryService, type);
	}

	/**
	 * Gets the allowed children calculator if supported. If not <code>null</code> could be
	 * returned. Used by methods {@link #getAllowedChildren(Instance)},
	 * {@link #getAllowedChildren(Instance, String)} and {@link #isChildAllowed(Instance, String)}
	 * 
	 * @return the calculator to use for allowed children calculation
	 */
	protected abstract AllowedChildrenProvider<I> getAllowChildrenProvider();

	/**
	 * Change state.
	 * 
	 * @param operation
	 *            the operation
	 * @param instance
	 *            the instance
	 */
	protected void notifyForStateChange(Operation operation, I instance) {
		eventService.fire(new OperationExecutedEvent(operation, instance));
	}

	/**
	 * Gets the instance dao implementation for the current service implementation. Required!.
	 * 
	 * @return the instance dao
	 */
	protected abstract InstanceDao<I> getInstanceDao();

	/**
	 * Gets the instance class that is represented by the current service. Will be used for
	 * {@link ServiceRegister}.Required!
	 * 
	 * @return the instance class
	 */
	protected abstract Class<I> getInstanceClass();

	/**
	 * Gets the logger for logging. Required!
	 * 
	 * @return the logger
	 */
	protected abstract Logger getLogger();

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