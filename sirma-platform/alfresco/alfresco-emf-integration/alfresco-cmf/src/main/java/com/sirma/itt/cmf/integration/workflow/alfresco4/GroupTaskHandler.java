package com.sirma.itt.cmf.integration.workflow.alfresco4;

import java.util.Map;

import org.activiti.engine.delegate.DelegateTask;
import org.apache.log4j.Logger;

/**
 * The GroupTaskHandler handles creation notification for pool tasks, so to copy
 * needed data in execution context
 */
public class GroupTaskHandler extends BaseTaskHandler {

	private static final String BPM_ASSIGNEE = "bpm_groupAssignee";
	/** The LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(GroupTaskHandler.class);
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
		super.notify(task);
		Map<String, Object> taskVariables = task.getVariables();
		if (traceEnabled) {
			trace("Task variables: " + taskVariables);
		}
		// set group assignee in exec context to be evaluated
		if (taskVariables.containsKey(BPM_ASSIGNEE)) {
			Object assignee = taskVariables.get(BPM_ASSIGNEE);
			if (debugEnabled) {
				debug("Setting context group assignee to " + assignee + "for " + task.getId());
			}
			task.getExecution().setVariable(BPM_ASSIGNEE, assignee);

		}
	}

	/**
	 * Gets the logger.
	 *
	 * @return the logger
	 */
	public Logger getLogger() {
		return LOGGER;
	}

}
