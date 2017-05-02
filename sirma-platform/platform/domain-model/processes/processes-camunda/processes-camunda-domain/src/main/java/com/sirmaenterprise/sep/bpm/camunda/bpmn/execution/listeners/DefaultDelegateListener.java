package com.sirmaenterprise.sep.bpm.camunda.bpmn.execution.listeners;

import java.lang.invoke.MethodHandles;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default listener for {@link DelegateExecution} event that logs the event.
 * 
 * @author bbanchev
 */
public class DefaultDelegateListener implements JavaDelegate {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public void execute(final DelegateExecution execution) throws Exception {
		LOGGER.info("Java Delegate executed for execution with id {} on process {}!", execution.getId(),
				execution.getProcessInstance().getId());
	}

}
