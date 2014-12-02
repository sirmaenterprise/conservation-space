package com.sirma.itt.emf.audit.solr.service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.audit.configuration.AuditConfigurationProperties;
import com.sirma.itt.emf.configuration.Config;

/**
 * Scheduler class for running regular Solr data delta imports.
 */
@Startup
@Singleton
public class SolrDataImportScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(SolrDataImportScheduler.class);

	@Resource
	private TimerService service;

	@Inject
	private SolrService solrService;

	/** The solr data import interval in ms. */
	@Inject
	@Config(name = AuditConfigurationProperties.SOLR_DATA_IMPORT_INTERVAL, defaultValue = "0")
	private Integer solrDataImportInterval;

	/** Enables/disables the audit module. */
	@Inject
	@Config(name = AuditConfigurationProperties.AUDIT_ENABLED, defaultValue = "false")
	private Boolean auditEnabled;

	/**
	 * Initialize timer.
	 */
	@PostConstruct
	protected void initializeParameters() {
		if (auditEnabled) {
			if (solrDataImportInterval == null || solrDataImportInterval <= 0) {
				LOGGER.error("Solr data import interval is not properly configured. Data Import will be disabled.");
				return;
			}
			TimerConfig config = new TimerConfig();
			config.setPersistent(false);
			service.createIntervalTimer(0, solrDataImportInterval, config);
		}
	}

	/**
	 * Runs regular solr data import.
	 */
	@Timeout
	public void solrDataImport() {
		if (auditEnabled) {
			LOGGER.trace("Running scheduled solr data import.");
			solrService.dataImport(false);
		}
	}

}
