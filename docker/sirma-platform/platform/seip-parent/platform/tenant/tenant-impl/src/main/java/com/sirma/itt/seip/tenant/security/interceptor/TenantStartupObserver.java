package com.sirma.itt.seip.tenant.security.interceptor;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.runtime.BeforePhaseStartEvent;
import com.sirma.itt.seip.runtime.StartupComponent;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.security.annotation.RunAsSystem;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.itt.seip.tenant.context.DefaultTenantInitializer;
import com.sirma.itt.seip.tenant.context.TenantManager;
import com.sirma.itt.seip.tenant.exception.TenantValidationException;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Observer that listens for startup events. Forces the startup to pause until tenants is present and adds a decorator
 * for annotated components with {@link RunAsAllTenantAdmins}.
 *
 * @author BBonev
 */
@Singleton
class TenantStartupObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private SecurityContextManager securityContextManager;

	@Inject
	private TenantManager tenantManager;

	@Inject
	private DefaultTenantInitializer tenantInitializer;

	@Inject
	private TransactionSupport transactionSupport;

	/**
	 * Pauses after deployment phases if there is no active tenant.
	 *
	 * @param event the event
	 */
	@RunAsSystem(protectCurrentTenant = false)
	public void phaseInterceptor(@Observes BeforePhaseStartEvent event) {
		if (event.getPhase() == StartupPhase.STARTUP_COMPLETE) {
			long count = tenantManager.getActiveTenantsInfo(true).count();
			// if no tenants are active pause the processing until tenant is added
			if (count == 0L) {
				createTenantFromExternalConfig();
			}
		}
	}

	private void createTenantFromExternalConfig() {
		try {
			tenantInitializer.tryCreatingDefaultTenant();
		} catch (TenantValidationException e) {
			LOGGER.warn("", e);
		}
	}

	/**
	 * Decorates all components annotated with {@link RunAsAllTenantAdmins} and that are not going
	 * to be proxied by CDI. Non proxy instances are not processed correctly when called by startup
	 * operations.
	 *
	 * @param event the event
	 */
	public void startupEnrichment(@Observes BeforePhaseStartEvent event) {
		List<StartupComponent> decorated = new LinkedList<>();

		for (Iterator<StartupComponent> it = event.getComponents().iterator(); it.hasNext(); ) {
			StartupComponent component = it.next();
			boolean modified = false;
			if (component.getAnnotation(Startup.class).transactionMode() != TransactionMode.NOT_SUPPORTED) {
				// this should be moved in a separate observer but CDI does not support observer ordering for now so
				// this is why it's in this class. If decorated in inverse order a transaction will span multiple
				// tenants and this is not desired
				component = new TransactionalStartupComponentDecorator(component, transactionSupport);
				modified = true;
			}
			if (component.isAnnotationPresent(RunAsAllTenantAdmins.class)) {
				component = new RunAsTenantComponentDecorator(component, tenantManager, securityContextManager);
				modified = true;
			}
			if (modified) {
				it.remove();
				decorated.add(component);
			}
		}

		decorated.forEach(event::addComponent);
	}
}
