package com.sirma.itt.seip.instance.archive;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.ArchivedInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.actions.InstanceOperations;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.instance.dao.ServiceRegistry;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.tasks.RunAs;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;

/**
 * The default implementation of the {@link ArchiveService}. The implementation works with
 * {@link DeleteInstanceSchedulerExecutor} to process all children.
 *
 * @author BBonev
 */
@ApplicationScoped
@Transactional(TxType.REQUIRED)
public class ArchiveServiceImpl implements ArchiveService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveServiceImpl.class);

	@Inject
	private TransactionIdHolder transactionIdHolder;

	@Inject
	private EventService eventService;

	@Inject
	private SchedulerService schedulerService;

	@Inject
	@InstanceType(type = ObjectTypes.ARCHIVED)
	private InstanceDao archivedInstanceDao;

	@Inject
	private InstanceOperations operationInvoker;

	@Inject
	private ServiceRegistry serviceRegistry;

	@Inject
	private TypeConverter typeConverter;

	/** Specifies if the delete operation should be synchronous or not. */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "operations.delete.synchronous", type = Boolean.class, defaultValue = "false", sensitive = true, label = "Specifies if the delete operation should be synchronous or not.")
	private ConfigurationProperty<Boolean> syncDelete;

	/**
	 * Specifies the number of retries the service to try to delete the instance and it's children
	 */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "operations.delete.maxRetries", type = Integer.class, defaultValue = "5", sensitive = true, label = "Specifies the number of retries the service to try to delete the instance and it's children")
	private ConfigurationProperty<Integer> deleteRetries;

	/** The time between retries in seconds. */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "operations.delete.retryTimeout", type = Long.class, defaultValue = "120", sensitive = true, label = "The time between retries in seconds")
	private ConfigurationProperty<Long> retriesTimeout;

	@Override
	public void scheduleDelete(Instance instance, Operation operation) {
		if (instance == null || instance.getId() == null) {
			return;
		}
		scheduleInternal(instance, operation, false);
	}

	@Override
	public void scheduleDelete(Instance instance, Operation operation, boolean permanent) {
		if (instance == null || instance.getId() == null) {
			return;
		}
		scheduleInternal(instance, operation, permanent);
	}

	/**
	 * Schedule internal.
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @param permanent
	 *            the permanent
	 */
	private void scheduleInternal(Instance instance, Operation operation, boolean permanent) {
		// if we have active transaction means the method is called for scheduled instance for
		// deletion and we need to perform the actual deletion now and not some other time
		if (transactionIdHolder.isTransactionActive()) {
			LOGGER.debug("Scheduling {} with id={} for deletion", instance.getClass().getSimpleName(),
					instance.getId());
			archiveAndDelete(instance, operation, !permanent);
		} else {
			LOGGER.debug("Deleting {} with id={}", instance.getClass().getSimpleName(), instance.getId());
			scheduleForDeleteInternal(instance, operation, permanent);
		}
	}

	/**
	 * Schedule instance for deletion.
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @param permanent
	 *            the permanent
	 */
	private void scheduleForDeleteInternal(Instance instance, Operation operation, boolean permanent) {

		String transactionId = transactionIdHolder.createTransactionId();

		notifyForDeletingInstanceGraph(instance, transactionId);

		SchedulerConfiguration configuration = createConfiguration(instance, operation);

		SchedulerContext context = createExecutorContext(instance, operation, transactionId, permanent);

		schedulerService.schedule(DeleteInstanceSchedulerExecutor.BEAN_ID, configuration, context);
	}

	/**
	 * Creates the executor context.
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @param transactionId
	 *            the transaction id
	 * @param permanent
	 *            the permanent
	 * @return the scheduler context
	 */
	private static SchedulerContext createExecutorContext(Instance instance, Operation operation, String transactionId,
			boolean permanent) {
		SchedulerContext context = new SchedulerContext(3);
		context.put(DeleteInstanceSchedulerExecutor.TRANSACTION_ID, transactionId);
		context.put(DeleteInstanceSchedulerExecutor.INSTANCE_REFERENCE, instance.toReference());
		context.put(DeleteInstanceSchedulerExecutor.OPERATION, operation.getOperation());
		context.put(DeleteInstanceSchedulerExecutor.IS_PERMANENT, permanent);
		return context;
	}

	/**
	 * Notify for deleting instance graph.
	 *
	 * @param instance
	 *            the instance
	 * @param transactionId
	 *            the transaction id
	 */
	private void notifyForDeletingInstanceGraph(Instance instance, String transactionId) {
		eventService.fire(new ArchivedInstanceAddedEvent(instance, transactionId));
	}

	/**
	 * Creates the configuration.
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @return the scheduler configuration
	 */
	private SchedulerConfiguration createConfiguration(Instance instance, Operation operation) {
		boolean isSyncDelete = syncDelete.get().booleanValue();
		SchedulerConfiguration configuration = schedulerService
				.buildEmptyConfiguration(isSyncDelete ? SchedulerEntryType.IMMEDIATE : SchedulerEntryType.TIMED);
		String operationId = "archive";
		if (operation != null && operation.getOperation() != null) {
			operationId = operation.getOperation();
		}
		configuration.setIdentifier(operationId + "_" + instance.getId());
		// we disable transaction on the operation because it could take some time
		// the operation implementation will handle the transactions
		return configuration
				.setScheduleTime(new Date())
					.setRemoveOnSuccess(true)
					.setSynchronous(isSyncDelete)
					.setPersistent(!isSyncDelete)
					.setMaxRetryCount(deleteRetries.get().intValue())
					.setRetryDelay(retriesTimeout.get())
					.setTransactionMode(isSyncDelete ? TransactionMode.REQUIRED : TransactionMode.NOT_SUPPORTED)
					.setRunAs(RunAs.USER);
	}

	@Override
	public void delete(Instance instance, Operation operation) {
		archiveAndDelete(instance, operation, true);
	}

	@Override
	public void delete(Instance instance, Operation operation, boolean archive) {
		archiveAndDelete(instance, operation, archive);
	}

	/**
	 * Archive the given instance and delete.
	 *
	 * @param instance
	 *            the instance the instance to archive and delete
	 * @param operation
	 *            the operation the operation that triggered the deletion
	 * @param archive
	 *            if <code>true</code> the instance will be archived before deletion otherwise will be deleted without
	 *            option for restore
	 */
	private void archiveAndDelete(Instance instance, Operation operation, boolean archive) {
		if (instance == null) {
			// we can't continue
			LOGGER.warn("Tried to delete null instance. Nothing is deleted!");
			return;
		}
		// convert instance to deleted instance
		// save to archive store
		// delete the actual instance

		// if archive is needed do it otherwise only instance delete will occur
		if (archive) {
			LOGGER.trace("Archiving {} with id={}", instance.getClass().getSimpleName(), instance.getId());
			// create a copy of the instance just before deletion
			ArchivedInstance archivedInstance = typeConverter.convert(ArchivedInstance.class, instance);

			// invoke lifecycle operation events to notify that the instance has been deleted
			// the changes done to the instance in here will be saved in the semantic database
			operationInvoker.invokeDelete(instance, operation);

			// store the archived instance
			archivedInstanceDao.persistChanges(archivedInstance);
			LOGGER.trace("Completed archival of {} with id={}", instance.getClass().getSimpleName(), instance.getId());
		} else {
			LOGGER.info("Going to remove {} with id={} without option for restore.",
					instance.getClass().getSimpleName(), instance.getId());
			// invoke lifecycle operation events to notify that the instance has been deleted
			// the changes done to the instance in here will be saved in the semantic database
			operationInvoker.invokeDelete(instance, operation);
		}
		InstanceDao instanceDao = serviceRegistry.getInstanceDao(instance);
		// if there is a instance dao perform the actual delete from the database depending on the
		// implementation and update the caches
		if (instanceDao != null) {
			// after this call the instance is no longer in the general relational tables
			instanceDao.delete(instance);
		} else {
			LOGGER.warn("Could not find a handle to delete {} with id={}. The instance will not be removed!",
					instance.getClass().getSimpleName(), instance.getId());
		}
	}

	@Override
	public <S extends Serializable> Collection<ArchivedInstance> loadByDbId(List<S> ids) {
		List<ArchivedInstance> loaded = archivedInstanceDao.loadInstancesByDbKey(ids);
		setIsDeletedProperty(loaded);
		return loaded;
	}

	@Override
	public <S extends Serializable> Collection<ArchivedInstance> loadByDbId(List<S> ids, boolean loadProperties) {
		List<ArchivedInstance> loaded = archivedInstanceDao.loadInstancesByDbKey(ids, loadProperties);
		setIsDeletedProperty(loaded);
		return loaded;
	}

	// CMF-18334 - only for deleted instances
	private static void setIsDeletedProperty(List<ArchivedInstance> loaded) {
		for (Instance instance : loaded) {
			instance.add(DefaultProperties.IS_DELETED, true);
		}
	}

}
