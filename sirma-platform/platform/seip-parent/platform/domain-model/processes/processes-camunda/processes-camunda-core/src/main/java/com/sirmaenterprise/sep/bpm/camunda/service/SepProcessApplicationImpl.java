package com.sirmaenterprise.sep.bpm.camunda.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.impl.EjbProcessApplication;
import org.camunda.bpm.application.impl.EjbProcessApplicationReference;
import org.camunda.bpm.application.impl.ProcessApplicationLogger;
import org.camunda.bpm.container.impl.RuntimeContainerDelegateImpl;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

import com.sirma.itt.seip.security.context.SecurityContext;
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

	@Override
	public ExecutionListener getExecutionListener() {
		return cdiEventListener;
	}

	@Override
	public TaskListener getTaskListener() {
		return cdiEventListener;
	}

}
