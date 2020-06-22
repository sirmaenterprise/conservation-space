package com.sirmaenterprise.sep.bpm.camunda.tenant.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.engine.ProcessEngine;

import com.sirma.itt.seip.plugin.Extension;
import com.sirmaenterprise.sep.bpm.camunda.configuration.CamundaConfigurationEnricher;
import com.sirmaenterprise.sep.bpm.camunda.configuration.SepProcessEngineConfigurator;
import com.sirmaenterprise.sep.bpm.camunda.service.job.SepRuntimeJobExecutor;

/**
 * Updates the {@link ProcessEngine} configuration with custom job executor
 * 
 * @author bbanchev
 */
@Singleton
@Extension(target = CamundaConfigurationEnricher.TARGET, order = 4)
public class RuntimeJobExecutorEnricher implements CamundaConfigurationEnricher {
	@Inject
	private SepRuntimeJobExecutor sepRuntimeJobExecutor;

	@Override
	public void enrich(SepProcessEngineConfigurator configuration) {
		configuration.setJobExecutor(sepRuntimeJobExecutor);
	}

}
