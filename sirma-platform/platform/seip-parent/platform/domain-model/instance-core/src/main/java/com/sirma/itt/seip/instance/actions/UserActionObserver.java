package com.sirma.itt.seip.instance.actions;

import java.util.Map;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.script.TransitionScriptEvaluator;
import com.sirma.itt.seip.instance.state.AfterOperationExecutedEvent;
import com.sirma.itt.seip.instance.state.BeforeOperationExecutedEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.version.InstanceVersionService;

/**
 * Observer that listens for user operation events. And evaluates the scripts defined in those.
 *
 * @author Ivo Rusev
 */
@Singleton
public class UserActionObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserActionObserver.class);

	@Inject
	private TransitionScriptEvaluator scripts;

	/**
	 * Listens for the event after user operation. Those events are thrown for specific actions described in the
	 * definitions.
	 *
	 * @param event
	 *            - OperationExecutedEvent
	 */
	public void onAfterUserOperation(@Observes AfterOperationExecutedEvent event) {
		LOGGER.trace("Intercepted after operation event with operation: {}", event.getOperation());
		Map<String, Object> context = event.getContext();
		Operation operation = event.getOperation();
		Instance instance = event.getInstance();

		// currently this functionality isn't supported for versions
		if (InstanceVersionService.isVersion(instance.getId())) {
			return;
		}

		checkIfValidScriptOperation(instance, false, operation, context);
	}

	/**
	 * Listens for the event before user operation execution. Those events are thrown for specific actions described in
	 * the definitions.
	 *
	 * @param event
	 *            - BeforeOpereationExecutedEvent
	 */
	public void onBeforeUserOperation(@Observes BeforeOperationExecutedEvent event) {
		LOGGER.trace("Intercepted before operation event with operation: {}", event.getOperation());
		Instance instance = event.getInstance();

		// not supported for versions
		if (InstanceVersionService.isVersion(instance.getId())) {
			return;
		}

		checkIfValidScriptOperation(instance, true, event.getOperation(), event.getContext());
	}

	/**
	 * Checks if it is a valid script operation and calls script processing if so.
	 *
	 * @param instance
	 *            the instance
	 * @param isBefore
	 *            true - the before operation execution event, false otherwise.
	 * @param operation
	 *            the operation
	 * @param context
	 *            the context
	 */
	private void checkIfValidScriptOperation(Instance instance, boolean isBefore,
			Operation operation, Map<String, Object> context) {
		if (operation != null && operation.isUserOperation()) {
			processScript(instance, operation.getUserOperationId(), isBefore, context);
		}

	}

	/**
	 * Processes the script. If script was not executed correctly or has compilation error, show notification to the
	 * user.
	 *
	 * @param instance
	 *            the instance
	 * @param actionId
	 *            the action id from the definition
	 * @param isBefore
	 *            if the script is executed before or after the event.
	 */
	private void processScript(Instance instance, String actionId, boolean isBefore, Map<String, Object> context) {
		try {
			scripts.executeScriptsForTransition(instance, actionId, isBefore, context);
		} catch (Exception e) {
			LOGGER.error("Error occured while generating message: ", e);
		}
	}

}
