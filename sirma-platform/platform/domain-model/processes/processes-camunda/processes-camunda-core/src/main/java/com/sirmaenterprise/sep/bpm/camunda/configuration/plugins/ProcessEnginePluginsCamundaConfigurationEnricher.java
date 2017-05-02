package com.sirmaenterprise.sep.bpm.camunda.configuration.plugins;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.plugin.AdministratorAuthorizationPlugin;
import org.camunda.connect.plugin.impl.ConnectProcessEnginePlugin;
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin;

import com.sirma.itt.seip.plugin.Extension;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener;
import com.sirmaenterprise.sep.bpm.camunda.configuration.CamundaConfigurationEnricher;
import com.sirmaenterprise.sep.bpm.camunda.configuration.SepProcessEngineConfigurator;
import com.sirmaenterprise.sep.bpm.camunda.plugin.IdentityIntegrationPlugin;
import com.sirmaenterprise.sep.bpm.camunda.plugin.ModelParserEventListenerPlugin;

/**
 * Enriches the {@link ProcessEngine} with all default {@link ProcessEnginePlugin}s.
 * 
 * @author bbanchev
 */
@Singleton
@Extension(target = CamundaConfigurationEnricher.TARGET, order = 1)
public class ProcessEnginePluginsCamundaConfigurationEnricher implements CamundaConfigurationEnricher {
	@Inject
	private IdentityIntegrationPlugin identityIntegrationPlugin;
	@Inject
	private BPMNParserEventListener bpmnParserEventListener;

	@Override
	public void enrich(SepProcessEngineConfigurator configuration) {
		List<ProcessEnginePlugin> plugins = configuration.getProcessEnginePlugins();
		if (plugins == null) {
			plugins = new LinkedList<>();
			configuration.setProcessEnginePlugins(plugins);
		}
		plugins.add(identityIntegrationPlugin);
		plugins.add(new ModelParserEventListenerPlugin(Collections.singletonList(bpmnParserEventListener), null));
		plugins.add(new ConnectProcessEnginePlugin());
		plugins.add(new SpinProcessEnginePlugin());
		plugins.add(new AdministratorAuthorizationPlugin());
	}

}
