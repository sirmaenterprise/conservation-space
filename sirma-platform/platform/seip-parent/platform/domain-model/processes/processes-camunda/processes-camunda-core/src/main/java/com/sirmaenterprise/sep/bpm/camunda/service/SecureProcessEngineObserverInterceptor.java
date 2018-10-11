package com.sirmaenterprise.sep.bpm.camunda.service;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.camunda.bpm.engine.ProcessEngine;

import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationException;

/**
 * Observer for {@link SecureProcessEngine}. Checks if security context is initialized and if current
 * {@link ProcessEngine} service is with the correct context.
 * 
 * @author bbanchev
 */
@Interceptor
@SecureProcessEngine
@Priority(Interceptor.Priority.APPLICATION - 200)
public class SecureProcessEngineObserverInterceptor {
	@Inject
	private BPMSecurityService bpmSecurityService;

	/**
	 * Check the current security context based on the {@link BPMSecurityService#validateProcessEngine()}
	 *
	 * @param ctx
	 *            the current context
	 * @return the context chain
	 * @throws Exception
	 *             on any error or if {@link #validateProcessEngine()} fails during check
	 */
	@AroundInvoke
	public Object checkSecurityContext(final InvocationContext ctx) throws Exception {
		ProcessEngineStatus processEngineStatus = bpmSecurityService.getProcessEngineStatus();
		SecureProcessEngine processEngineConfiguration = ctx.getMethod().getAnnotation(SecureProcessEngine.class);
		switch (processEngineStatus.getValue()) {
			case AVAILABLE:
				return ctx.proceed();
			case UNAVAILABLE:
				return checkUnavailable(ctx, processEngineConfiguration);
			case INVALID_REQUEST:
				throw new CamundaIntegrationException("Process engine cound be accessed using current context!");
			case ERROR:
				throw new CamundaIntegrationException(
						"Requested process engine could not be accessed! Check client code!");
			default:
				throw new CamundaIntegrationException("Unimplemented process status check! Check client code!");
		}
	}

	private static Object checkUnavailable(final InvocationContext ctx, SecureProcessEngine processEngineConfiguration)
			throws Exception, CamundaIntegrationException {
		if (processEngineConfiguration.notInitializedAccepted()) {
			return ctx.proceed();
		}
		throw new CamundaIntegrationException("Process engine is not configured/actived!");
	}
}
