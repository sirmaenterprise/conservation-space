package com.sirma.itt.cmf.integration.workflow.alfresco4;

import java.util.Collection;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.apache.log4j.Logger;

/**
 * Handler that should be used as complete handler of task that is predecessor
 * of the multi instance task
 *
 * @author bbanchev
 */
public class MultiInstanceTaskBeforeCreationHandler extends BaseTaskHandler {
	private static final String BPM_ASSIGNEES = "bpm_assignees";
	/** The LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(BaseTaskHandler.class);
	// cache log levels
	private static final boolean debugEnabled = LOGGER.isDebugEnabled();
	private static final boolean traceEnabled = LOGGER.isDebugEnabled();

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.activiti.engine.delegate.TaskListener#notify(org.activiti.engine.
	 * delegate.DelegateTask)
	 */
	@Override
	public void notify(DelegateTask task) {
		Map<String, Object> taskVariables = task.getVariables();
		if (traceEnabled) {
			trace("Task variables: " + taskVariables);
		}
		DelegateExecution execution = task.getExecution();
		// set assignee in exec context to be evaluated
		if (taskVariables.containsKey(BPM_ASSIGNEES)) {
			Object assignees = taskVariables.get(BPM_ASSIGNEES);
			execution.setVariable(BPM_ASSIGNEES, assignees);
			if (assignees instanceof Collection) {
				execution.setVariable("cmfwf_multiInstanceCount",
						((Collection<?>) assignees).size());
				if (traceEnabled) {
					LOGGER.trace("Settings multiinstance assingees: " + assignees);
				}
			} else {
				LOGGER.error("Missing variable '" + BPM_ASSIGNEES + "'!");
			}
		} else {
			LOGGER.error("Missing variable '" + BPM_ASSIGNEES + "'!");
		}
		if (!taskVariables.containsKey("cmfwf_requiredApprovePercent")) {
			execution.setVariable("cmfwf_requiredApprovePercent", 100);
		} else {
			execution.setVariable("cmfwf_requiredApprovePercent",
					taskVariables.get("cmfwf_requiredApprovePercent"));
		}
		execution.setVariable("cmfwf_actualPercent", 0);
		execution.setVariable("cmfwf_actualRejectPercent", 0);

		execution.setVariable("cmfwf_approveCount", 0);
		execution.setVariable("cmfwf_rejectCount", 0);
	}

	/**
	 * Gets the logger.
	 *
	 * @return the logger
	 */
	protected Logger getLogger() {
		return LOGGER;
	}

	/**
	 * Debug.
	 *
	 * @param message
	 *            the message
	 */
	protected void debug(Object... message) {
		if (debugEnabled) {
			StringBuilder builder = new StringBuilder();
			for (Object string : message) {
				builder.append(string);
			}
			getLogger().debug(builder.toString());
		}

	}

	/**
	 * Trace a message by logger.
	 *
	 * @param message
	 *            the message
	 */
	protected void trace(String message) {
		if (traceEnabled) {
			getLogger().trace(message);
		}
	}

}
