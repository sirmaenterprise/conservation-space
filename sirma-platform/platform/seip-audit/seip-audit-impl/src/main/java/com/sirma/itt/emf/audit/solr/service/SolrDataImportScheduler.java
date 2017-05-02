package com.sirma.itt.emf.audit.solr.service;

import java.lang.invoke.MethodHandles;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.security.annotation.OnTenantAdd;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;

/**
 * Scheduler class for running regular Solr data delta imports.
 */
@Singleton
@Named(SolrDataImportScheduler.ACTION_NAME)
public class SolrDataImportScheduler extends SchedulerActionAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static final String ACTION_NAME = "SolrDataImportScheduler";

	@Inject
	private SchedulerService schedulerService;

	@Inject
	private SolrService solrService;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "audit.solr.dataimport.cron", defaultValue = "0/15 * * ? * *", sensitive = true, label = "Cron expression that defines when to execute the data import")
	private ConfigurationProperty<String> solrDataImportCron;

	/**
	 * Initialize change listener for configuration changes
	 */
	@PostConstruct
	void initialize() {
		solrDataImportCron.addConfigurationChangeListener(c -> schedule(c.get()));
	}

	/**
	 * Initialize timer.
	 */
	@OnTenantAdd
	@RunAsAllTenantAdmins
	@Startup(async = true)
	public void initializeScheduler() {
		schedule(solrDataImportCron.get());
	}

	private void schedule(String expression) {
		LOGGER.info("Service initialized and scheduled for reloading using cron '{}'.", expression);
		SchedulerConfiguration config = schedulerService.buildEmptyConfiguration(SchedulerEntryType.CRON);
		config.setCronExpression(expression);
		config.setIdentifier(ACTION_NAME);
		schedulerService.schedule(ACTION_NAME, config);
	}

	@Override
	public void execute(SchedulerContext context) throws Exception {
		LOGGER.trace("Running scheduled solr data import.");
		solrService.dataImport(false);
	}

}
