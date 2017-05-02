package com.sirma.itt.cmf.integration.workflow.alfresco4;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Handler that collects task property to context mapped to the same key
 * 
 * @author bbanchev
 */
public class MultiInstanceCollectProperties extends BaseTaskHandler {
	private static final String CMFWF_MULTI_PROPERTIES = "cmfwf_multiCollectProperties";
	/** The LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(MultiInstanceCollectProperties.class);
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
		String props = (String) taskVariables.get(CMFWF_MULTI_PROPERTIES);
		if (StringUtils.isNotBlank(props)) {
			String[] propertyList = props.split(",");
			for (String nextProperty : propertyList) {
				addToExecution(execution, taskVariables.get(nextProperty), nextProperty);
			}
		} else {
			LOGGER.error("Missing variable '" + CMFWF_MULTI_PROPERTIES + "'!");
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addToExecution(DelegateExecution execution, Object data, String nextProperty) {
		Object execVariable = execution.getVariable(nextProperty);
		execVariable = execVariable == null ? execution.getVariableLocal(nextProperty) : execVariable;
		if (data instanceof Collection) {
			if (execVariable == null) {
				execVariable = new LinkedHashSet();
			}
			((Collection<?>) execVariable).addAll((Collection) data);
		} else if (data instanceof String) {
			if (execVariable == null) {
				execVariable = data;
			} else {
				execVariable = execVariable.toString() + data;
			}
		} else {
			throw new RuntimeException("Value is not supported! " + execVariable.getClass());
		}
		execution.setVariable(nextProperty, execVariable);
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
