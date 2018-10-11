package com.sirmaenterprise.sep.bpm.camunda.service;

import java.util.function.Supplier;

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
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;

import com.sirma.itt.seip.annotation.NoOperation;
import com.sirma.itt.seip.context.ContextualReference;

/**
 * Proxy class of ProcessEngine used for proper initialize of Camunda Process Engine.
 *
 * @author hlungov
 */
@NoOperation
public class BPMProcessEngine extends ContextualReference<ProcessEngine> implements ProcessEngine {

	/**
	 * Constructor.
	 */
	public BPMProcessEngine() {
		super();
	}

	/**
	 * Instantiates a new contextual reference.
	 *
	 * @param contextIdSupplier
	 *            the context id supplier
	 */
	public BPMProcessEngine(Supplier<String> contextIdSupplier) {
		super(contextIdSupplier);
	}

	@Override
	public String getName() {
		return getContextValue().getName();
	}

	@Override
	public void close() {
		getContextValue().close();
	}

	@Override
	public ProcessEngineConfiguration getProcessEngineConfiguration() {
		return getContextValue().getProcessEngineConfiguration();
	}

	@Override
	public RuntimeService getRuntimeService() {
		return getContextValue().getRuntimeService();
	}

	@Override
	public RepositoryService getRepositoryService() {
		return getContextValue().getRepositoryService();
	}

	@Override
	public FormService getFormService() {
		return getContextValue().getFormService();
	}

	@Override
	public TaskService getTaskService() {
		return getContextValue().getTaskService();
	}

	@Override
	public HistoryService getHistoryService() {
		return getContextValue().getHistoryService();
	}

	@Override
	public IdentityService getIdentityService() {
		return getContextValue().getIdentityService();
	}

	@Override
	public ManagementService getManagementService() {
		return getContextValue().getManagementService();
	}

	@Override
	public AuthorizationService getAuthorizationService() {
		return getContextValue().getAuthorizationService();
	}

	@Override
	public CaseService getCaseService() {
		return getContextValue().getCaseService();
	}

	@Override
	public FilterService getFilterService() {
		return getContextValue().getFilterService();
	}

	@Override
	public ExternalTaskService getExternalTaskService() {
		return getContextValue().getExternalTaskService();
	}

	@Override
	public DecisionService getDecisionService() {
		return getContextValue().getDecisionService();
	}
}
