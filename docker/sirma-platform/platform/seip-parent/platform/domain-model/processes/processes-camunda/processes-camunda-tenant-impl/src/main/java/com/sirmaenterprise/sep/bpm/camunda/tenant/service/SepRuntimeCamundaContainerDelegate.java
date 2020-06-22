package com.sirmaenterprise.sep.bpm.camunda.tenant.service;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.container.impl.RuntimeContainerDelegateImpl;
import org.camunda.bpm.container.impl.deployment.Attachments;
import org.camunda.bpm.container.impl.deployment.DeployProcessArchivesStep;
import org.camunda.bpm.container.impl.deployment.NotifyPostProcessApplicationUndeployedStep;
import org.camunda.bpm.container.impl.deployment.PostDeployInvocationStep;
import org.camunda.bpm.container.impl.deployment.PreUndeployInvocationStep;
import org.camunda.bpm.container.impl.deployment.ProcessesXmlStartProcessEnginesStep;
import org.camunda.bpm.container.impl.deployment.ProcessesXmlStopProcessEnginesStep;
import org.camunda.bpm.container.impl.deployment.StartProcessApplicationServiceStep;
import org.camunda.bpm.container.impl.deployment.StopProcessApplicationServiceStep;
import org.camunda.bpm.container.impl.deployment.UndeployProcessArchivesStep;
import org.camunda.bpm.container.impl.spi.ServiceTypes;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.context.TenantManager;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirmaenterprise.sep.bpm.camunda.configuration.CamundaConfiguration;
import com.sirmaenterprise.sep.bpm.camunda.service.custom.RuntimeProcessesStep;
import com.sirmaenterprise.sep.bpm.camunda.service.custom.TenantInitializationStep;
import com.sirmaenterprise.sep.bpm.camunda.tenant.step.CamundaDbProvisioning;

/**
 * Represents a tenant as single engine at runtime. In addition are registered listeners for tenant updates to
 * deploy/undeploy engine.
 *
 * @author bbanchev
 */
@ApplicationScoped
public class SepRuntimeCamundaContainerDelegate extends RuntimeContainerDelegateImpl {
	private boolean isDeployed = false;
	@Inject
	private TenantManager tenantManager;
	@Inject
	private SecurityContextManager securityContextManager;
	@Inject
	private CamundaConfiguration camundaConfiguration;
	@Inject
	private CamundaDbProvisioning camundaDbProvisioning;
	@Inject
	private TransactionSupport transactionSupport;

	// Initialized on application startup
	private AbstractProcessApplication processApplication;

	@Override
	public void deployProcessApplication(AbstractProcessApplication processApplication) {
		ensureNotNull("Process application", processApplication);

		this.processApplication = processApplication;

		Stream<TenantInfo> allTenantsInfo = tenantManager.getActiveTenantsInfo(false);
		allTenantsInfo.forEach(tenantInfo -> deployEngine(tenantInfo.getTenantId()));
	}

	@Override
	public void undeployProcessApplication(AbstractProcessApplication processApplication) {
		Stream<TenantInfo> allTenantsInfo = tenantManager.getActiveTenantsInfo(false);
		allTenantsInfo.forEach(tenantInfo -> undeployEngine(tenantInfo.getTenantId()));
	}

	public void deployEngine(String processAppName) {
		try {
			securityContextManager.initializeTenantContext(processAppName);
			// invoke in tx in the new thread context
			transactionSupport.invokeInNewTx(() -> deployEngineInternal(processAppName));
		} catch (Exception e) {
			ProcessEngineLogger.PROCESS_APPLICATION_LOGGER.exceptionWhileInitializingProcessengine(e);
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	protected void deployEngineInternal(String processAppName) {
		ensureNotNull("Process application", processApplication);
		String engineName = camundaConfiguration.getEngineName().get();
		Object service = serviceContainer.getService(ServiceTypes.PROCESS_ENGINE, engineName);
		if (service != null) {
			LOG.processEngineAlreadyInitialized();
			return;
		}
		final String operationName = "Deployment of Process Application for " + processAppName;
		serviceContainer
				.createDeploymentOperation(operationName)
					.addAttachment(Attachments.PROCESS_APPLICATION, processApplication)
					.addStep(new TenantInitializationStep(securityContextManager, camundaDbProvisioning))
					.addStep(new RuntimeProcessesStep(engineName))
					.addStep(new ProcessesXmlStartProcessEnginesStep())
					.addStep(new DeployProcessArchivesStep())
					.addStep(new StartProcessApplicationServiceStep())
					.addStep(new PostDeployInvocationStep())
					.execute();
		LOG.paDeployed(processAppName);
	}

	public void undeployEngine(String processAppName) {
		ensureNotNull("Process application", processApplication);
		try {
			securityContextManager.initializeTenantContext(processAppName);
			transactionSupport.invokeInNewTx(() -> undeployEngineInternal(processAppName));
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	private void undeployEngineInternal(String processAppName) {
		String engineName = camundaConfiguration.getEngineName().get();
		// if the process application is not deployed, ignore the request.
		if (serviceContainer.getService(ServiceTypes.PROCESS_ENGINE, engineName) == null) {
			LOG.processEngineClosed(engineName);
			return;
		}
		final String operationName = "Undeployment of Process Application for " + processAppName;
		// perform the undeployment
		serviceContainer
				.createUndeploymentOperation(operationName)
					.addAttachment(Attachments.PROCESS_APPLICATION, processApplication)
					.addStep(new PreUndeployInvocationStep())
					.addStep(new UndeployProcessArchivesStep())
					.addStep(new ProcessesXmlStopProcessEnginesStep())
					.addStep(new StopProcessApplicationServiceStep())
					.addStep(new NotifyPostProcessApplicationUndeployedStep())
					.execute();

		LOG.paUndeployed(processApplication.getName());
	}
}
