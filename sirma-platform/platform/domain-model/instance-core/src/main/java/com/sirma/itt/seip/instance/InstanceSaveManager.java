package com.sirma.itt.seip.instance;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.time.TimeTracker;

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
@ApplicationScoped
public class InstanceSaveManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	@ExtensionPoint(value = InstanceSaveStep.NAME)
	private Plugins<InstanceSaveStep> steps;

	@Inject
	private InstanceService instanceService;

	/**
	 * Executes {@link InstanceSaveStep} extensions in order. First invokes the before phase of the steps, then executes
	 * the actual instance save. After that invokes the after phases of the steps. If any error occurs during step
	 * execution or the actual save, rollback is invoked for already executed steps.
	 *
	 * @param context
	 *            the {@link InstanceSaveContext} used to store and share instance data between the different steps
	 * @return the saved instance
	 */
	@Transactional(TxType.REQUIRED)
	public Instance saveInstance(InstanceSaveContext context) {
		Objects.requireNonNull(context, "Context is required!");
		TimeTracker timeTracker = TimeTracker.createAndStart();
		String id = context.getInstanceId();

		// successfully executed steps in order of execution to be used for rollback, if any error occurs
		List<InstanceSaveStep> beforeSaveExecutedSteps = new ArrayList<>(steps.count());
		List<InstanceSaveStep> afterSaveExecutedSteps = null;
		try {
			callSteps(step -> step.beforeSave(context), beforeSaveExecutedSteps::add);

			instanceService.save(context.getInstance(), context.getOperation());

			afterSaveExecutedSteps = new ArrayList<>(steps.count());
			callSteps(step -> step.afterSave(context), afterSaveExecutedSteps::add);

			LOGGER.debug("Instance with id - {}, was saved for {} ms.", id, timeTracker.stop());
			return context.getInstance();
		} catch (RuntimeException e) {
			timeTracker.restart();
			LOGGER.error("Error occurred while saving instance - {}.", id, e);
			LOGGER.info("Commencing rollback procedure for instance - {}", id);

			invokeRollback(afterSaveExecutedSteps, step -> step.rollbackAfterSave(context, e));
			invokeRollback(beforeSaveExecutedSteps, step -> step.rollbackBeforeSave(context, e));

			LOGGER.info("Rollback completed in {} ms, for instance - {}", timeTracker.stop(), id);
			throw e;
		}
	}

	private void callSteps(Consumer<InstanceSaveStep> invokeStep, Consumer<InstanceSaveStep> onSuccess) {
		steps.forEach(invokeStep.andThen(onSuccess));
	}

	private static void invokeRollback(List<InstanceSaveStep> steps, Consumer<InstanceSaveStep> invokeStep) {
		if (isEmpty(steps)) {
			return;
		}

		Collections.reverse(steps);
		for (InstanceSaveStep step : steps) {
			try {
				invokeStep.accept(step);
			} catch (RuntimeException e) {
				LOGGER.error("Failed to rollback step: {}", step.getName(), e);
			}
		}
	}

}
