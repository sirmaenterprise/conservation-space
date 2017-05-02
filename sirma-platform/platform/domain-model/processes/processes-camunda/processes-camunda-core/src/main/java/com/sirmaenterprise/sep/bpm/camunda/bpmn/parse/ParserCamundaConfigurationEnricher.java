package com.sirmaenterprise.sep.bpm.camunda.bpmn.parse;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.BpmnParseFactory;

import com.sirma.itt.seip.plugin.Extension;
import com.sirmaenterprise.sep.bpm.camunda.configuration.CamundaConfigurationEnricher;
import com.sirmaenterprise.sep.bpm.camunda.configuration.SepCdiExpressionManager;
import com.sirmaenterprise.sep.bpm.camunda.configuration.SepProcessEngineConfigurator;

/**
 * Enriches the {@link ProcessEngine} with custom {@link BpmnParseFactory}.
 * 
 * @author bbanchev
 */
@Singleton
@Extension(target = CamundaConfigurationEnricher.TARGET, order = 2)
public class ParserCamundaConfigurationEnricher implements CamundaConfigurationEnricher {

	@Inject
	private SepCdiExpressionManager cdiExpressionManager;

	@Override
	public void enrich(SepProcessEngineConfigurator configuration) {
		configuration.setCustomBpmnParseFactory(new SepBpmnParseFactory());
		configuration.setExpressionManager(cdiExpressionManager);
	}

}
