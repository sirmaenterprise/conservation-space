package com.sirma.itt.seip.instance;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Handles instance saving by executing all phases needed for successful instance saving. The different phases are
 * represented by steps, which are executed in defined order.
 * <p>
 * Instance save flow: <br />
 * 1. It is executed <b>before</b> phase for all of the steps. <br />
 * 2. After all of the before steps are executed successfully, the actual instance save is executed. <br />
 * 3. If all of the phases are executed without any errors, the <b>after</b> phase of the steps is executed finally.
 * <br />
 * 4. The saved instance is returned.
 * <p>
 * If any error occurs while the saving process is executed, steps rollback is invoked. The rollback procedure is also
 * executed on phases: <br />
 * 1. For every <b>successfully executed</b> step is invoked <b>after</b> rollback phase. <br />
 * 2. For every <b>successfully executed</b> step is invoked <b>before</b> rollback phase. <br />
 * Note that the rollback phase is executed in reversed order from the original, which means that the last successfully
 * executed step will be the first in the rollback phase. Also, if any error occurs, while rollback procedure is
 * executed, the error is just logged and the procedure is continued.
 *
 * @author A. Kunchev
 * @see InstanceSaveStep
 */
class InstanceSaveManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	@ExtensionPoint(value = InstanceSaveStep.NAME)
	private Plugins<InstanceSaveStep> steps;

	@Inject
	private InstanceService instanceService;

	@Inject
	private TransactionSupport transactionSupport;

	/**
	 * Executes {@link InstanceSaveStep} extensions in order. First invokes the before phase of the steps, then executes
	 * the actual instance save. After that invokes the after phases of the steps. If any error occurs during step
	 * execution or the actual save, rollback is invoked for already executed steps.
	 *
	 * @param context the {@link InstanceSaveContext} used to store and share instance data between the different steps
	 * @return the saved instance
	 */
	Instance saveInstance(InstanceSaveContext context) {
		Objects.requireNonNull(context, "Context is required!");
		TimeTracker timeTracker = TimeTracker.createAndStart();
		String id = context.getInstanceId();

		// successfully executed steps in order of execution to be used for rollback, if any error occurs
		final List<InstanceSaveStep> beforeSaveExecutedSteps = new ArrayList<>(steps.count());
		final List<InstanceSaveStep> afterSaveExecutedSteps = new ArrayList<>(steps.count());

		// no mather when transaction fails we will rollback the operation
		// this should be before the actual save as if the transaction is marked for rollback during the save operation
		// this cannot be registered
		// if the transaction is already marked for rollback we should not continue anyway
		transactionSupport.invokeOnFailedTransactionInTx(
				() -> doRollback(context, beforeSaveExecutedSteps, afterSaveExecutedSteps));

		callSteps(step -> step.beforeSave(context), beforeSaveExecutedSteps::add);

		instanceService.save(context.getInstance(), context.getOperation());

		callSteps(step -> step.afterSave(context), afterSaveExecutedSteps::add);

		LOGGER.debug("Instance with id - {}, was saved for {} ms.", id, timeTracker.stop());
		return context.getInstance();
	}

	private static void doRollback(InstanceSaveContext context, List<InstanceSaveStep> beforeSaveExecutedSteps,
			List<InstanceSaveStep> afterSaveExecutedSteps) {
		TimeTracker timeTracker = TimeTracker.createAndStart();
		String id = context.getInstanceId();
		LOGGER.info("Commencing rollback procedure for instance - {}", id);
		invokeRollback(afterSaveExecutedSteps, step -> step.rollbackAfterSave(context));
		invokeRollback(beforeSaveExecutedSteps, step -> step.rollbackBeforeSave(context));
		LOGGER.info("Rollback completed in {} ms, for instance - {}", timeTracker.stop(), id);
	}

	private static void invokeRollback(List<InstanceSaveStep> saveSteps, Consumer<InstanceSaveStep> invokeStep) {
		if (isEmpty(saveSteps)) {
			return;
		}

		Collections.reverse(saveSteps);
		for (InstanceSaveStep step : saveSteps) {
			try {
				invokeStep.accept(step);
			} catch (RuntimeException e) {
				LOGGER.error("Failed to rollback step: {}", step.getName(), e);
			}
		}
	}

	private void callSteps(Consumer<InstanceSaveStep> invokeStep, Consumer<InstanceSaveStep> onSuccess) {
		steps.forEach(invokeStep.andThen(onSuccess));
	}
}