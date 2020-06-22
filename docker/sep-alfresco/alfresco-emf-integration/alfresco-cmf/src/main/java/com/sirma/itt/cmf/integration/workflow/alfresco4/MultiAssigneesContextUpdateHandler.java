package com.sirma.itt.cmf.integration.workflow.alfresco4;

import java.util.Map;

import org.activiti.engine.delegate.DelegateTask;
import org.apache.log4j.Logger;

/**
 * The MultiAssigneesContextUpdateHandler handles completion of task proceeding
 * multi assignee context
 */
public class MultiAssigneesContextUpdateHandler extends BaseTaskHandler {
	/** The LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(MultiAssigneesContextUpdateHandler.class);
	// cache log levels
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

		// copy data to execution context
		task.getExecution().setVariable(MultiAssigneesTaskHandler.BPM_MULTI_ASSIGNEES,
				taskVariables.get(MultiAssigneesTaskHandler.BPM_MULTI_ASSIGNEES));
		task.getExecution().setVariable(MultiAssigneesTaskHandler.BPM_GROUP_ASSIGNEES,
				taskVariables.get(MultiAssigneesTaskHandler.BPM_GROUP_ASSIGNEES));
		task.getExecution().setVariable(MultiAssigneesTaskHandler.BPM_USERS_ASSIGNEES,
				taskVariables.get(MultiAssigneesTaskHandler.BPM_USERS_ASSIGNEES));
		if (traceEnabled) {
			trace("Task variables: " + taskVariables);
			trace("Execution variables: " + task.getExecution().getVariables());
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
