package com.sirma.itt.seip.instance.archive;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.util.DependencyResolver;
import com.sirma.itt.seip.domain.util.DependencyResolvers;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Scheduler operation for deletion of a instance and all it's dependencies. The implementation could be modified easily
 * to be executed in a parallel context.
 *
 * @author BBonev
 */
@ApplicationScoped
@Named(DeleteInstanceSchedulerExecutor.BEAN_ID)
public class DeleteInstanceSchedulerExecutor extends SchedulerActionAdapter {

	public static final String BEAN_ID = "deleteInstanceSchedulerExecutor";

	/** Parameter name for the operation object that triggered the deletion. Type {@link String}. */
	public static final String OPERATION = "operation";
	/** Parameter name for the reference of the instance that need to be deleted. Type {@link InstanceReference}. */
	public static final String INSTANCE_REFERENCE = "instanceReference";
	/** Parameter name for the transaction in which the instance is deleted. Type {@link String}. */
	public static final String TRANSACTION_ID = "transactionId";
	/**
	 * Parameter name for the flag if the instance and it's children should be deleted permanently. Type {@link Boolean}
	 * .
	 */
	public static final String IS_PERMANENT = "isPermanent";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final List<Pair<String, Class<?>>> ARGUMENTS = Arrays.asList(
			new Pair<>(TRANSACTION_ID, String.class), new Pair<>(INSTANCE_REFERENCE, InstanceReference.class),
			new Pair<>(OPERATION, String.class), new Pair<>(IS_PERMANENT, Boolean.class));

	private int dependencyDeleteBatchSize;

	@Inject
	private TransactionIdHolder idHolder;
	@Inject
	private ArchiveService archiveService;
	@Inject
	private DependencyResolvers resolvers;
	@Inject
	private TransactionSupport transactionSupport;

	@Override
	public void execute(SchedulerContext context) throws Exception {
		try {
			String transactionId = context.getIfSameType(TRANSACTION_ID, String.class);
			idHolder.setTransactionId(transactionId);

			final Instance instance = loadTargetInstance(context);
			if (instance == null) {
				LOGGER.error("Could not load instance for deletion with reference {}",
						context.getIfSameType(INSTANCE_REFERENCE, InstanceReference.class));
				// we throw exception to retry the operation
				throw new EmfRuntimeException("Could not load instance for deletion");
			}
			final Operation operation = new Operation(context.getIfSameType(OPERATION, String.class), true);
			final boolean shouldArchive = !context.getIfSameType(IS_PERMANENT, Boolean.class, Boolean.FALSE);

			LOGGER.info(
					"Started asynchronous deletion of instance with {} and id={} deletion with transaction id={}.",
					instance.getClass().getName(), instance.getId(), transactionId);

			performDeletion(instance, operation, shouldArchive);
		} finally {
			idHolder.clearCurrentId();
		}
	}

	/**
	 * Perform deletion.
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @param shouldArchive
	 *            the should archive
	 */
	private void performDeletion(final Instance instance, final Operation operation, final boolean shouldArchive) {
		TimeTracker tracker = TimeTracker.createAndStart();
		// delete all dependencies and delete them in batches
		// this is because if there a lot of instances this could take some time so we
		// do not want the transaction to timeout
		final Iterator<Instance> iterator = getInstanceIterator(instance);
		int total = 0;
		Set<Serializable> processedInstances = new HashSet<>(64);
		while (iterator.hasNext()) {
			// NOTE: this could be changed to be executed in a parallel context
			total += deleteDependenciesInNewTx(operation, iterator, processedInstances, shouldArchive);
			LOGGER.debug("Deleted {} instance children", total);
		}

		// and now we will delete the primary instance
		// this is last because if something fails a retry could continue to the end
		transactionSupport.invokeInNewTx(() -> archiveInstance(instance, operation, shouldArchive));
		LOGGER.info("Finished deletion of {} with id={}. The operation deleted {} child instanes and took {} ms.",
				instance.getClass().getSimpleName(), instance.getId(), total, tracker.stop());
	}

	/**
	 * Load target instance.
	 *
	 * @param context
	 *            the context
	 * @return the instance
	 */
	private Instance loadTargetInstance(SchedulerContext context) {
		InstanceReference reference = context.getIfSameType(INSTANCE_REFERENCE, InstanceReference.class);
		Options.ALLOW_LOADING_OF_DELETED_INSTANCES.enable();
		try {
			return transactionSupport.invokeInTx(reference::toInstance);
		} finally {
			Options.ALLOW_LOADING_OF_DELETED_INSTANCES.disable();
		}
	}

	/**
	 * Delete dependencies in new transaction.
	 *
	 * @param operation
	 *            the operation
	 * @param iterator
	 *            the iterator
	 * @param processedInstances
	 *            the processed instances
	 * @param shouldArchive
	 *            the should archive
	 * @return the processed instances
	 */
	private Integer deleteDependenciesInNewTx(final Operation operation, final Iterator<Instance> iterator,
			Set<Serializable> processedInstances, final boolean shouldArchive) {
		return transactionSupport
				.invokeInNewTx(() -> deleteDependentInstances(iterator, operation, processedInstances, shouldArchive));
	}

	/**
	 * Delete dependent instances.
	 *
	 * @param iterator
	 *            the iterator
	 * @param operation
	 *            the operation
	 * @param processedInstances
	 *            the processed instances
	 * @param shouldArchive
	 *            the should archive
	 * @return the processed instances
	 */
	private Integer deleteDependentInstances(Iterator<Instance> iterator, Operation operation,
			Set<Serializable> processedInstances, boolean shouldArchive) {
		int processed = 0;
		while (iterator.hasNext()) {
			Instance next = iterator.next();
			if (next != null && !processedInstances.contains(next.getId())) {
				processedInstances.add(next.getId());

				archiveInstance(next, operation, shouldArchive);
			}
			if (++processed >= dependencyDeleteBatchSize) {
				break;
			}
		}
		return processed;
	}

	/**
	 * Gets the instance iterator.
	 *
	 * @param instance
	 *            the instance
	 * @return the instance iterator
	 */
	private Iterator<Instance> getInstanceIterator(Instance instance) {
		DependencyResolver dependencyResolver = resolvers.getResolver(instance);

		if (dependencyResolver == null) {
			LOGGER.warn("No dependency resolver found for instance of type " + instance.getClass());
			return Collections.emptyIterator();
		}

		dependencyDeleteBatchSize = dependencyResolver.currentBatchSize();
		dependencyDeleteBatchSize = dependencyDeleteBatchSize < 0 ? 50 : dependencyDeleteBatchSize;

		Iterator<Instance> iterator;
		if (dependencyResolver.isLazyLoadingSupported()) {
			iterator = dependencyResolver.resolveDependenciesLazily(instance);
		} else {
			iterator = dependencyResolver.resolveDependencies(instance).iterator();
		}
		return iterator;
	}

	/**
	 * Archive instance.
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @param shouldArchive
	 *            the should archive
	 */
	private void archiveInstance(Instance instance, Operation operation, boolean shouldArchive) {
		archiveService.delete(instance, operation, shouldArchive);
	}

	@Override
	protected List<Pair<String, Class<?>>> validateInput() {
		return ARGUMENTS;
	}

}
