package com.sirmaenterprise.sep.bpm.camunda.service;

import java.util.concurrent.Callable;

import org.camunda.bpm.engine.ProcessEngine;

import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationException;

/**
 * Service to validate the current execution context and the produced beans and configurations.Also there are methods
 * for execution of operation for specific engine
 *
 * @author bbanchev
 */
public interface BPMSecurityService {

	/**
	 * Check the current security context. Check may fail if no context is set or if context is inactive or initialized
	 * as the {@link SecurityContext#isSystemTenant()}. Otherwise it is checked if current context tenant is applicable
	 * for the currently produced {@link #processEngine}
	 * 
	 * @return one of the {@link ProcessEngineStatus#values()} based on the current status
	 */
	ProcessEngineStatus getProcessEngineStatus();

	/**
	 * Verifies that process engine for current security context is configured and available.
	 * 
	 * @return true if {@link ProcessEngine} is available
	 */
	boolean isEngineAvailable();

	/**
	 * Executes an operation in the context of requested {@link ProcessEngine}.
	 * <ul>
	 * <li>If operation is executed in system tenant or inactive context then a new context is initialized engine.</li>
	 * <li>If operation is executed in same tenant then operation is executed immediately.</li>
	 * <li>If operation is executed in different tenant than the process engine name is thrown exception.</li>
	 * </ul>
	 * 
	 * @param engineName
	 *            is the desired {@link ProcessEngine}
	 * @param operaton
	 *            the operation to execute
	 * @return the result of operation executed in correct context
	 * @throws CamundaIntegrationException
	 *             might throw exception if operation is executed in different tenant {@link SecurityContext}
	 */
	<T> T executeEngineOperation(String engineName, Callable<T> operaton) throws CamundaIntegrationException;
}
