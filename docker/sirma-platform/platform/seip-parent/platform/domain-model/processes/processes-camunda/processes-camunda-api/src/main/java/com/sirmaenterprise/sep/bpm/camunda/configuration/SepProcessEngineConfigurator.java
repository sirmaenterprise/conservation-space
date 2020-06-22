package com.sirmaenterprise.sep.bpm.camunda.configuration;

import java.lang.invoke.MethodHandles;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup;
import org.camunda.bpm.engine.impl.cfg.BpmnParseFactory;
import org.camunda.bpm.engine.impl.cfg.JtaProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.jobexecutor.DefaultJobExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.db.ConfigurationDatasourceProvisioner;

/**
 * The {@link SepProcessEngineConfigurator} is Camunda wrapper for engine configurations.
 *
 * @author bbanchev
 */
public class SepProcessEngineConfigurator extends JtaProcessEngineConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	protected void init() {
		// delegate configuration retrieval to SEIP code
		CamundaConfiguration configurations = ProgrammaticBeanLookup.lookup(CamundaConfiguration.class);
		setHistory(HISTORY_FULL);
		setDatabaseSchemaUpdate(DB_SCHEMA_UPDATE_TRUE);
		setDataSourceJndiName("java:jboss/datasources/" + configurations.getDatasourceName().get());
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
		try {
			super.init();
		} catch (ProcessEngineException e) {
			ConfigurationDatasourceProvisioner provisioner = ProgrammaticBeanLookup.lookup(ConfigurationDatasourceProvisioner.class);
			LOGGER.trace("Couldn't lookup datasource for " + getDataSourceJndiName() + ". Attempting to recreate it.", e);
			provisioner.lookupDataSource(getDataSourceJndiName(), configurations);
			super.init();
		}
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
