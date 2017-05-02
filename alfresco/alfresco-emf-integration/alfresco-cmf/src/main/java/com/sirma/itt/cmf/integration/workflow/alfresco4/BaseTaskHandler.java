package com.sirma.itt.cmf.integration.workflow.alfresco4;

import java.util.Map;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.apache.log4j.Logger;

/**
 * The Class BaseTaskHandler.
 */
public class BaseTaskHandler implements TaskListener {

	private static final String BPM_ASSIGNEE = "bpm_assignee";
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
		// set assignee in exec context to be evaluated
		if (taskVariables.containsKey(BPM_ASSIGNEE)) {
			Object assignee = taskVariables.get(BPM_ASSIGNEE);
			if (debugEnabled) {
				debug("Setting context assignee to " + assignee + "for " + task.getId());
			}
			task.getExecution().setVariable(BPM_ASSIGNEE, assignee);

		}
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
