package com.sirmaenterprise.sep.bpm.camunda.service;

import static com.sirmaenterprise.sep.bpm.camunda.service.ProcessEngineStatus.AVAILABLE;
import static com.sirmaenterprise.sep.bpm.camunda.service.ProcessEngineStatus.ERROR;
import static com.sirmaenterprise.sep.bpm.camunda.service.ProcessEngineStatus.INVALID_REQUEST;
import static com.sirmaenterprise.sep.bpm.camunda.service.ProcessEngineStatus.UNAVAILABLE;
import static com.sirmaenterprise.sep.bpm.camunda.util.ProcessEngineUtil.getTenantId;
import static com.sirmaenterprise.sep.bpm.camunda.util.ProcessEngineUtil.lookupProcessEngine;

import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.engine.ProcessEngine;

import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirmaenterprise.sep.bpm.camunda.configuration.CamundaConfiguration;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationException;
import com.sirmaenterprise.sep.bpm.exception.BPMRuntimeException;

/**
 * Service to validate the current execution context and the produced beans and configurations.
 *
 * @author bbanchev
 */
@Singleton
public class BPMSecurityServiceImpl implements BPMSecurityService {

	@Inject
	private SecurityContextManager securityContextManager;
	@Inject
	private ProcessEngine processEngine;

	@Override
	public ProcessEngineStatus getProcessEngineStatus() {
		return validateProcessEngineInternal();
	}

	private ProcessEngineStatus validateProcessEngineInternal() {
		SecurityContext securityContext = securityContextManager.getCurrentContext();
		if (!securityContext.isActive() || securityContext.isSystemTenant()) {
			return INVALID_REQUEST.withMessage("Process engine could not be requested in system tenant mode!");
		}
		String requestedEngineName = CamundaConfiguration.getEngineName(securityContext);
		ProcessEngine contextEngine = lookupProcessEngine(requestedEngineName);
		if (contextEngine != null) {
			return checkInitializedEngine(contextEngine, requestedEngineName);
		}
		return UNAVAILABLE
				.withMessage("Requested Process engine '" + requestedEngineName + "' is not configured/activated!");
	}

	private ProcessEngineStatus checkInitializedEngine(ProcessEngine contextEngine, String requestedEngineName) {
		String engineName = processEngine.getName();
		if (!contextEngine.getName().equals(engineName)) {
			return ERROR.withMessage("Requested Process engine '" + requestedEngineName
					+ "' does not match the current active Process engine '" + engineName + "'!");
		}
		return AVAILABLE;
	}

	@Override
	public boolean isEngineAvailable() {
		return validateProcessEngineInternal().compareTo(AVAILABLE) == 0;
	}

	@Override
	public <T> T executeEngineOperation(String engineName, Callable<T> operaton) throws CamundaIntegrationException {
		SecurityContext securityContext = securityContextManager.getCurrentContext();
		String tenantId = getTenantId(engineName);
		if (securityContext.isActive() && !securityContext.isSystemTenant()
				&& !securityContext.getCurrentTenantId().equals(tenantId)) {
			throw new CamundaIntegrationException("Operation is executed in wrong context and could not be completed!");
		}
		return executeEngineOperationInContext(operaton, securityContext, tenantId);
	}

	private <T> T executeEngineOperationInContext(Callable<T> operaton, SecurityContext securityContext,
			String tenantId) throws CamundaIntegrationException {
		try {
			if (securityContext.isActive() && securityContext.getCurrentTenantId().equals(tenantId)) {
				return operaton.call();
			}
			try {
				securityContextManager.initializeTenantContext(tenantId);
				return operaton.call();
			} finally {
				securityContextManager.endContextExecution();
			}
		} catch (BPMRuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new CamundaIntegrationException("Failed to execute an operation!", e);
		}
	}
}
