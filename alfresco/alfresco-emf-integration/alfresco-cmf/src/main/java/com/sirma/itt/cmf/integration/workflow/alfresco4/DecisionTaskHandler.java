package com.sirma.itt.cmf.integration.workflow.alfresco4;

import org.activiti.engine.delegate.DelegateTask;
import org.apache.log4j.Logger;

/**
 * Handler for decisions. Sets the execution model data so constraints to be
 * satisfied.
 *
 * @author bbanchev
 */
public class DecisionTaskHandler extends BaseTaskHandler {

	/** The Constant CMFWF_TASK_OUTCOME. */
	private static final String CMFWF_TASK_OUTCOME = "cmfwf_taskOutcome";
	/** The LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(BaseTaskHandler.class);
	// cache log levels
	private static final boolean debugEnabled = LOGGER.isDebugEnabled();

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.sirma.itt.cmf.integration.workflow.alfresco4.BaseTaskHandler#notify
	 * (org.activiti.engine.delegate.DelegateTask)
	 */
	@Override
	public void notify(DelegateTask delegateTask) {
		super.notify(delegateTask);

		if (delegateTask.hasVariable(CMFWF_TASK_OUTCOME)) {
			Object variable = delegateTask.getVariable(CMFWF_TASK_OUTCOME);
			delegateTask.getExecution().setVariable(CMFWF_TASK_OUTCOME, variable);
			if (debugEnabled) {
				debug("Decision outcome: ", variable, " for ", delegateTask.getId());
			}
		}
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
}
