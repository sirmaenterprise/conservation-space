package com.sirmaenterprise.sep.bpm.camunda.configuration;

import org.camunda.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup;
import org.camunda.bpm.engine.impl.cfg.BpmnParseFactory;
import org.camunda.bpm.engine.impl.cfg.JtaProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.jobexecutor.DefaultJobExecutor;

/**
 * The {@link SepProcessEngineConfigurator} is Camunda wrapper for engine configurations.
 *
 * @author bbanchev
 */
public class SepProcessEngineConfigurator extends JtaProcessEngineConfiguration {

	@Override
	protected void init() {
		// delegate configuration retrieval to SEIP code
		CamundaConfiguration camundaConfiguration = ProgrammaticBeanLookup.lookup(CamundaConfiguration.class);
		setHistory(HISTORY_FULL);
		setDatabaseSchemaUpdate(DB_SCHEMA_UPDATE_TRUE);
		setDataSourceJndiName("java:jboss/datasources/" + camundaConfiguration.getDatasourceName().get());
		setTransactionManagerJndiName("java:jboss/TransactionManager");
		setBpmnStacktraceVerbose(true);
		setCmmnEnabled(true);
		setDmnEnabled(true);
		setJobExecutor(new DefaultJobExecutor());
		setJobExecutorActivate(false);
		// disable metrics because we don't need them but they slow down server shutdown
		setMetricsEnabled(false);
		CamundaConfigurationEnricher configurationEnricherService = ProgrammaticBeanLookup
				.lookup(CamundaConfigurationEnricher.class);
		configurationEnricherService.enrich(this);
		super.init();
	}

	/**
	 * Provides custom {@link BpmnParseFactory}
	 * 
	 * @param factory
	 *            to set as {@link BpmnParseFactory}
	 */
	public void setCustomBpmnParseFactory(BpmnParseFactory factory) {
		this.bpmnParseFactory = factory;
	}
}
