package com.sirma.itt.emf.audit.web.menu;

import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.emf.audit.configuration.AuditConfigurationProperties;
import com.sirma.itt.emf.configuration.Config;

/**
 * Provides conditions for the audit log page.
 * @author Nikolay Velkov
 */
@Named
public class AuditLogProvider {
	/** Configuration property for enabling/disabling the audit module. */
	@Inject
	@Config(name = AuditConfigurationProperties.AUDIT_ENABLED, defaultValue = "false")
	private Boolean auditEnabled;
	
	/**
	 * Checks if is audit log enabled.
	 *
	 * @return true, if the audit log is enabled
	 */
	public boolean isAuditLogEnabled() {
		return auditEnabled;
	}
}
