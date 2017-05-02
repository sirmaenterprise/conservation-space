package com.sirmaenterprise.sep.bpm.camunda.service;

import java.lang.invoke.MethodHandles;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;
import javax.inject.Named;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.cdi.impl.ProcessEngineServicesProducer;
import org.camunda.bpm.engine.impl.ServiceImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirmaenterprise.sep.bpm.camunda.configuration.CamundaConfiguration;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import com.sirmaenterprise.sep.bpm.exception.BPMRuntimeException;

/**
 * Required integration component that produces CDI managed beans for Camunda services. All beans are
 * {@link RequestScoped} so during each request the correct process engine service to be produced.
 *
 * @author bbanchev
 */
@Specializes
@ApplicationScoped
public class MultiEngineServicesProducer extends ProcessEngineServicesProducer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private CamundaConfiguration camundaConfiguration;

	@Inject
	private SecurityContext securityContext;

	private BPMProcessEngine bpmProcessEngine;

	/**
	 * Provide process engine based on current tenant configuration for process engine.
	 *
	 * @return the process engine or throws exception if engine is not initialized
	 */
	protected ProcessEngine provideEngine() {
		String currentEngineId = camundaConfiguration.getEngineName().get();
		LOGGER.trace("Provide engine with name {} ", currentEngineId);
		ProcessEngine processEngine = ProcessEngines.getProcessEngine(currentEngineId, false);
		if (processEngine == null) {
			throw new CamundaIntegrationRuntimeException("Engine not initialized for " + currentEngineId);
		}
		return processEngine;
	}

	@Named
	@Produces
	@Override
	public ProcessEngine processEngine() {
		return bpmProcessEngine;
	}

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initProcessEngine() {
		bpmProcessEngine = new BPMProcessEngine(securityContext::getCurrentTenantId);
		bpmProcessEngine.initializeWith(this::provideEngine);
	}

	@RequestScoped
	@Named
	@Produces
	@Override
	public RuntimeService runtimeService() {
		return processEngine().getRuntimeService();
	}

	@RequestScoped
	@Named
	@Produces
	@Override
	public TaskService taskService() {
		return processEngine().getTaskService();
	}

	@RequestScoped
	@Named
	@Produces
	@Override
	public RepositoryService repositoryService() {
		return processEngine().getRepositoryService();
	}

	@RequestScoped
	@Named
	@Produces
	@Override
	public FormService formService() {
		return processEngine().getFormService();
	}

	@RequestScoped
	@Named
	@Produces
	@Override
	public HistoryService historyService() {
		return processEngine().getHistoryService();
	}

	@RequestScoped
	@Named
	@Produces
	@Override
	public IdentityService identityService() {
		return processEngine().getIdentityService();
	}

	@RequestScoped
	@Named
	@Produces
	@Override
	public ManagementService managementService() {
		return processEngine().getManagementService();
	}

	@RequestScoped
	@Named
	@Produces
	@Override
	public AuthorizationService authorizationService() {
		return processEngine().getAuthorizationService();
	}

	@RequestScoped
	@Named
	@Produces
	@Override
	public FilterService filterService() {
		return processEngine().getFilterService();
	}

	@RequestScoped
	@Named
	@Produces
	@Override
	public ExternalTaskService externalTaskService() {
		return processEngine().getExternalTaskService();
	}

	@RequestScoped
	@Named
	@Produces
	@Override
	public CaseService caseService() {
		return processEngine().getCaseService();
	}

	@RequestScoped
	@Named
	@Produces
	@Override
	public DecisionService decisionService() {
		return processEngine().getDecisionService();
	}

	/**
	 * Produces an {@link CommandExecutor} related to the currently produced {@link ProcessEngine}
	 *
	 * @return the {@link CommandExecutor} or throw exception if it could not be produced
	 */
	@RequestScoped
	@Produces
	public CommandExecutor commandExecutor() {
		RuntimeService runtimeService = runtimeService();
		if (runtimeService instanceof ServiceImpl) {
			return ((ServiceImpl) runtimeService).getCommandExecutor();
		}
		throw new BPMRuntimeException("Expression manager could not be produced in CDI context");
	}
}
