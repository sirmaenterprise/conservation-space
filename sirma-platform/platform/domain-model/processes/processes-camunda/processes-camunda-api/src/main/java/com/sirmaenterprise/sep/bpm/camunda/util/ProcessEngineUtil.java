package com.sirmaenterprise.sep.bpm.camunda.util;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ProcessEngineServicesAware;

import com.sirmaenterprise.sep.bpm.camunda.configuration.CamundaConfiguration;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;

/**
 * The {@link ProcessEngineUtil} is util class to facilitate work with the {@link ProcessEngine}.
 *
 * @author bbanchev
 */
public class ProcessEngineUtil {
	private ProcessEngineUtil() {
		// utility class
	}

	/**
	 * Lookup process engine by its name.
	 *
	 * @param engineName
	 *            the engine name
	 * @return the process engine or null if not found
	 */
	public static ProcessEngine lookupProcessEngine(String engineName) {
		return ProcessEngines.getProcessEngine(engineName, false);
	}

	/**
	 * Lookup process engine by an execution
	 *
	 * @param execution
	 *            the execution delagate to check engine for
	 * @return the process engine or null if not found
	 */
	public static ProcessEngine lookupProcessEngine(DelegateExecution execution) {
		return ProcessEngines.getProcessEngine(getProcessEngineName(execution), false);
	}

	/**
	 * Gets the process engine name by the current execution.
	 *
	 * @param execution
	 *            the execution to check engine name for
	 * @return the process engine name or {@link CamundaIntegrationRuntimeException} if cound not be retrieved.
	 */
	public static String getProcessEngineName(ProcessEngineServicesAware execution) {
		if (execution.getProcessEngineServices() instanceof ProcessEngine) {
			return ((ProcessEngine) execution.getProcessEngineServices()).getName();
		}
		throw new CamundaIntegrationRuntimeException(
				"Unsupported process engine service: " + execution.getProcessEngineServices());
	}

	/**
	 * Extracts the tenant id from engineName.
	 * 
	 * @param engineName
	 *            is engine name, generated as would be by {@link CamundaConfiguration#getEngineName()}
	 * @return the tenant id.
	 */
	public static String getTenantId(String engineName) {
		return engineName.replaceAll("_", ".");
	}

}
