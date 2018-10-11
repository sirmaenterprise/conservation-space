package com.sirmaenterprise.sep.bpm.camunda.bpmn.execution.listeners;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirmaenterprise.sep.bpm.camunda.listeners.delegate.CDIExecutionListener;
import com.sirmaenterprise.sep.bpm.camunda.service.SecureProcessEngine;

/**
 * {@link CollectUsers} from several sources and returns the unique ids or if used as listener, target value is
 * populated with the set. Based on story INUC03-UI2-S21.
 * 
 * @author bbanchev
 */
@Singleton
@Named("collectMultiInstanceUsers")
public class CollectMultiInstanceUsers extends CollectUsers implements CDIExecutionListener<CollectMultiInstanceUsers> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private Expression source;
	private FixedValue target;

	@Override
	public void validateParameters() {
		Objects.requireNonNull(source,
				"Invalid source data provided to collect users from. Provide a comma sepparated list of properties to extract values from!");
		Objects.requireNonNull(target, "Invalid target data property provided to store resolved users!");
	}

	@Override
	@SecureProcessEngine
	public void notify(DelegateExecution execution, CollectMultiInstanceUsers sourceEvent) {
		LOGGER.info("Called {} listener for execution with id {} on process {}! Paremeters: {}/{}",
				CollectMultiInstanceUsers.class, execution.getId(), execution.getProcessInstanceId(),
				sourceEvent.source, sourceEvent.target);
		String sourceProperties = (String) sourceEvent.source.getValue(execution);
		Objects.requireNonNull(sourceProperties,
				() -> "Property/ies to read multiple instances assignment authorities is a required parameter: "
						+ sourceEvent.source);

		String targetProperty = (String) sourceEvent.target.getValue(execution);
		Objects.requireNonNull(targetProperty,
				() -> "Property to store multiple instances assignment is a required parameter: " + sourceEvent.target);

		Set<String> users = resolveUsers(execution, sourceProperties);
		execution.setVariable(targetProperty, users);
	}

}