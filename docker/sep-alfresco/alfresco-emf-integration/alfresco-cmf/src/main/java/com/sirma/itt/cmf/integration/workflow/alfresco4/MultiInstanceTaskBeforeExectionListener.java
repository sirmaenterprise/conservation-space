package com.sirma.itt.cmf.integration.workflow.alfresco4;

import java.util.Collection;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.apache.log4j.Logger;

/**
 * Handler that should be used as handler of workflow that is started and the
 * first task is multi instance task
 *
 * @author bbanchev
 */
public class MultiInstanceTaskBeforeExectionListener implements ExecutionListener {
	private static final String BPM_ASSIGNEES = "bpm_assignees";
	/** The LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(MultiInstanceTaskBeforeExectionListener.class);

	/** The Constant traceEnabled. */
	private static final boolean traceEnabled = LOGGER.isDebugEnabled();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notify(DelegateExecution execution) throws Exception {
		if (traceEnabled) {
			LOGGER.trace("Execution variables: " + execution.getVariables());
		}
		Object assignees = execution.getVariable(BPM_ASSIGNEES);
		if (assignees instanceof Collection) {
			execution.setVariable("cmfwf_multiInstanceCount", ((Collection<?>) assignees).size());
			if (traceEnabled) {
				LOGGER.trace("Settings multiinstance assingees: " + assignees);
			}
		} else {
			LOGGER.error("Missing variable '" + BPM_ASSIGNEES + "'!");
		}
		if (!execution.hasVariable("cmfwf_requiredApprovePercent")) {
			execution.setVariable("cmfwf_requiredApprovePercent", 100);
		}
		execution.setVariable("cmfwf_actualPercent", 0);
		execution.setVariable("cmfwf_actualRejectPercent", 0);

		execution.setVariable("cmfwf_approveCount", 0);
		execution.setVariable("cmfwf_rejectCount", 0);
	}
}
