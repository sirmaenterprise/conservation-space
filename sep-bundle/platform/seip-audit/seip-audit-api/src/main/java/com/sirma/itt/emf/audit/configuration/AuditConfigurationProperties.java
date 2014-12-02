package com.sirma.itt.emf.audit.configuration;

import com.sirma.itt.emf.configuration.Configuration;
import com.sirma.itt.emf.util.Documentation;

/**
 * Configuration properties for the audit log.
 * 
 * @author Mihail Radkov
 */
@Documentation("Configuration for the audit log.")
public interface AuditConfigurationProperties extends Configuration {

	/** Property defining if the audit module will log. */
	@Documentation("Property defining if the audit module will log.")
	String AUDIT_ENABLED = "audit.enabled";

	/** The Solr address. */
	@Documentation("The address of the solr server including the core name")
	String SOLR_ADDRESS = "audit.solr.address";

	/** Time interval for importing data in Solr. */
	@Documentation("Interval for importing data in Solr")
	String SOLR_DATA_IMPORT_INTERVAL = "audit.solr.dataimport.interval";
}
