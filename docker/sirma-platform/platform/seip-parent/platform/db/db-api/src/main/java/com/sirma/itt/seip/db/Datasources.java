package com.sirma.itt.seip.db;

import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Provides means of building the system datasource names
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/11/2017
 */
public final class Datasources {
	private static final String DATASOURCE_PREFIX = "java:jboss/datasources/";

	private Datasources() {
		// utility class
	}

	/**
	 * Get the system database datasource jndi name
	 *
	 * @return the core jndi name
	 */
	public static String coreJndi() {
		return DATASOURCE_PREFIX + SecurityContext.SYSTEM_TENANT;
	}

	/**
	 * Build a datasource name for the given tenant
	 *
	 * @param tenantId the tenant identifier
	 * @return the tenant jndi anme
	 */
	public static String forTenant(String tenantId) {
		return DATASOURCE_PREFIX + tenantId;
	}
}
