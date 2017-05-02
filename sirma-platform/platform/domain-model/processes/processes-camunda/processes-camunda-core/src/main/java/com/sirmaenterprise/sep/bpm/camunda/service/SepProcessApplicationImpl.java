package com.sirmaenterprise.sep.bpm.camunda.service;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.impl.EjbProcessApplication;
import org.camunda.bpm.application.impl.EjbProcessApplicationReference;
import org.camunda.bpm.application.impl.ProcessApplicationLogger;
import org.camunda.bpm.container.impl.RuntimeContainerDelegateImpl;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.definition.event.LoadAllDefinitions;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.security.annotation.SecureObserver;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirmaenterprise.sep.bpm.camunda.BPMDownloadAlfresco4Service;
import com.sirmaenterprise.sep.bpm.camunda.BPMDownloadAlfresco4Service.CamundaModels;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import com.sirmaenterprise.sep.bpm.camunda.service.event.MultiEngineEventListener;

/**
 * EJB integration for Camunda container as local business implementation of {@link SepProcessApplication}. The
 * singleton is responsible to register engine and create deployments. The bean rely on
 * {@link RuntimeContainerDelegateImpl} SEIP extension which represents a tenant runtime instead of static xml
 * definition.
 * 
 * @author bbanchev
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Local(value = SepProcessApplication.class)
@ProcessApplication(name = "SEP Camunda Integration")
public class SepProcessApplicationImpl extends EjbProcessApplication implements SepProcessApplication {
	protected static final ProcessApplicationLogger LOG_APP = ProcessEngineLogger.PROCESS_APPLICATION_LOGGER;

	@Inject
	private MultiEngineEventListener cdiEventListener;
	@Inject
	private RuntimeContainerDelegateImpl runtimeContainerDelegate;
	@Inject
	private SecurityContext securityContext;
	// initialize on specific request since bean is singleton
	@Inject
	private Instance<ProcessEngine> processEngineInstance;
	@Inject
	private BPMDownloadAlfresco4Service bpmnProvider;
	@Inject
	private TransactionSupport transactionSupport;

	@Override
	public String getName() {
		return securityContext.getCurrentTenantId();
	}

	/**
	 * Initialize the process application with all available engines
	 */
	@PostConstruct
	public void initialize() {
		if (isDeployed) {
			LOG_APP.alreadyDeployed();
		} else {
			// deploy the application
			deploy();
			isDeployed = true;
		}
	}

	@Override
	public void deploy() {
		// container delegate does take care for correct deployment and redeployment
		runtimeContainerDelegate.deployProcessApplication(this);
	}

	@Override
	@PreDestroy
	public void undeploy() {
		if (!isDeployed) {
			LOG_APP.notDeployed();
		} else {
			// delegate stopping of the process application to the runtime container.
			runtimeContainerDelegate.undeployProcessApplication(this);
			isDeployed = false;
		}
	}

	@Override
	protected Class<? extends ProcessApplicationInterface> getBusinessInterface() {
		return SepProcessApplication.class;
	}

	@Override
	public ProcessApplicationReference getReference() {
		return new EjbProcessApplicationReference(this, getName());
	}

	/**
	 * Reloads the current models from corresponding subsystem
	 * 
	 * @param event
	 *            the trigger event
	 */
	@SecureObserver
	@SecureProcessEngine
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Override
	public void loadAllDefinitions(@Observes LoadAllDefinitions event) {
		ProcessEngine processEngine = null;
		try {
			processEngine = processEngineInstance.get();
			DeploymentBuilder deploymentBuilder = processEngine.getRepositoryService().createDeployment();
			deploymentBuilder.name(processEngine.getName());
			deploymentBuilder.enableDuplicateFiltering(true);

			loadModelsToDeployment(deploymentBuilder);
			if (!deploymentBuilder.getResourceNames().isEmpty()) {
				LOG_APP.registrationSummary("Going to deploy BPM models \t" + deploymentBuilder.getResourceNames());
				Deployment deployment = deploymentBuilder.deploy();
				LOG_APP.registrationSummary("Deployed BPM models with deployment id: " + deployment.getId());
			}
		} finally {
			processEngineInstance.destroy(processEngine);
		}

	}

	private void loadModelsToDeployment(DeploymentBuilder deploymentBuilder) {
		try {
			deploymentBuilder.source("DMS");
			List<FileDescriptor> retrieveAllBPMN = bpmnProvider.retrieveDefinitions(CamundaModels.values());
			for (FileDescriptor fileDescriptor : retrieveAllBPMN) {
				deploymentBuilder.addInputStream(fileDescriptor.getId(), fileDescriptor.getInputStream());
			}
		} catch (DMSException e) {
			throw new CamundaIntegrationRuntimeException("Failed to prepare deployment!", e);
		}
	}

	/**
	 * Method invoked automatically at the post process step of process engine initialization. Execute after Tx success
	 * to complete process engine initialization
	 */
	@PostDeploy
	public void postDeployment() {
		transactionSupport.invokeOnSuccessfulTransactionInTx(() -> loadAllDefinitions(null));
	}

	@Override
	public ExecutionListener getExecutionListener() {
		return cdiEventListener;
	}

	@Override
	public TaskListener getTaskListener() {
		return cdiEventListener;
	}

}
