package com.sirma.itt.cmf.integration.workflow.alfresco4;

import java.util.Collection;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.apache.log4j.Logger;

/**
 * Handler that should be used as creation handler of multi instance task to
 * reset needed multiinstance properties
 *
 * @author bbanchev
 */
public class MultiInstanceTaskCreationHandler extends BaseTaskHandler {
	private static final String CMFWF_MULTI_INSTANCE_ASSIGNEES_VAR_NAME = "cmfwf_multiInstanceAssigneesVarName";
	private static final String BPM_ASSIGNEES = "bpm_assignees";
	/** The LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(BaseTaskHandler.class);
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
		Map<String, Object> taskVariables = task.getVariables();
		if (traceEnabled) {
			trace("Task variables: " + taskVariables);
		}
		DelegateExecution execution = task.getExecution();
		// set assignee in exec context to be evaluated
		String bpmAssigneesVar = (String) execution.getVariable(CMFWF_MULTI_INSTANCE_ASSIGNEES_VAR_NAME);
		bpmAssigneesVar = bpmAssigneesVar == null ? (String) taskVariables.get(CMFWF_MULTI_INSTANCE_ASSIGNEES_VAR_NAME)
				: bpmAssigneesVar;
		bpmAssigneesVar = bpmAssigneesVar == null ? BPM_ASSIGNEES : bpmAssigneesVar;
		if (taskVariables.containsKey(bpmAssigneesVar)) {
			Object assignees = taskVariables.get(bpmAssigneesVar);
			execution.setVariable(bpmAssigneesVar, assignees);
			if (assignees instanceof Collection) {
				execution.setVariable("cmfwf_multiInstanceCount", ((Collection<?>) assignees).size());
				if (traceEnabled) {
					LOGGER.trace("Settings multiinstance assingees: " + assignees);
				}
			} else {
				LOGGER.error("Missing variable '" + bpmAssigneesVar + "'!");
			}
		} else {
			LOGGER.error("Missing variable '" + bpmAssigneesVar + "'!");
		}
		if (!taskVariables.containsKey("cmfwf_requiredApprovePercent")) {
			execution.setVariable("cmfwf_requiredApprovePercent", 100);
		} else {
			execution.setVariable("cmfwf_requiredApprovePercent", taskVariables.get("cmfwf_requiredApprovePercent"));
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

}
