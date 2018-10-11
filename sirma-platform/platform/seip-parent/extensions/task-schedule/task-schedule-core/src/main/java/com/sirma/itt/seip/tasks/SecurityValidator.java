package com.sirma.itt.seip.tasks;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.exception.SecurityException;
import com.sirma.itt.seip.tasks.entity.SchedulerEntity;

/**
 * Scheduler security validator that checks for tenant mismatch and some permissions.
 *
 * @author BBonev
 */
@Singleton
class SecurityValidator {

	private static final String ENTITY_TENANT_DOES_NOT_MATCH_THE_CURRENT_SECURITY_CONTEXT = "Entity tenant [{}] does not match the current security context [{}] for {}";
	private static final String CANNOT_ACCESS_OTHER_TENANTS_FROM_A_TENANT_CONTEXT = "Cannot access other tenants from a tenant context for ";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private SecurityContext securityContext;

	void onNewEntry(SchedulerConfiguration configuration) {
		if (!securityContext.isSystemTenant() && configuration.getRunAs() == RunAs.ALL_TENANTS) {
			String message = CANNOT_ACCESS_OTHER_TENANTS_FROM_A_TENANT_CONTEXT + configuration.getIdentifier();
			LOGGER.error(message);
			throw new SecurityException(message);
		}
	}

	void checkOnSave(SchedulerEntry entry) {
		if (!securityContext.isSystemTenant() && entry.getConfiguration().getRunAs() == RunAs.ALL_TENANTS) {
			String message = CANNOT_ACCESS_OTHER_TENANTS_FROM_A_TENANT_CONTEXT + entry.getIdentifier();
			LOGGER.error(message);
			throw new SecurityException(message);
		}
		if (!securityContext.getCurrentTenantId().equals(entry.getContainer())) {
			LOGGER.error(ENTITY_TENANT_DOES_NOT_MATCH_THE_CURRENT_SECURITY_CONTEXT, entry.getContainer(),
					securityContext.getCurrentTenantId(), entry.getIdentifier());
			throw new SecurityException(
					"Entity tenant [" + entry.getContainer() + "] does not match the current security context ["
							+ securityContext.getCurrentTenantId() + "] for " + entry.getIdentifier());
		}
	}

	void checkOnPersistNew(SchedulerEntity entity) {
		if (!securityContext.getCurrentTenantId().equals(entity.getTenantId())) {
			LOGGER.error(ENTITY_TENANT_DOES_NOT_MATCH_THE_CURRENT_SECURITY_CONTEXT, entity.getTenantId(),
					securityContext.getCurrentTenantId(), entity.getIdentifier());
			throw new SecurityException(
					"Entity tenant [" + entity.getTenantId() + "] does not match the current security context: "
							+ securityContext.getCurrentTenantId() + " for " + entity.getIdentifier());
		}
	}
}
