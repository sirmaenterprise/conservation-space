/**
 *
 */
package com.sirma.itt.seip.tenant.security.interceptor;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.runtime.AfterPhaseStartEvent;
import com.sirma.itt.seip.runtime.BeforePhaseStartEvent;
import com.sirma.itt.seip.runtime.Component;
import com.sirma.itt.seip.runtime.boot.StartupException;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.annotation.OnTenantAdd;
import com.sirma.itt.seip.security.annotation.OnTenantRemove;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.security.annotation.RunAsSystem;
import com.sirma.itt.seip.security.context.SecurityContextManager;
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
	private TransactionSupport transactionSupport;

	@Inject
	private SecurityContextManager securityContextManager;

	@Inject
	private TenantManager tenantManager;

	@Inject
	private DefaultTenantInitializer tenantInitializer;

	private Collection<Component> onTenantAdded = new LinkedList<>();
	private Collection<Component> onTenantRemoved = new LinkedList<>();

	@PostConstruct
	void initialize() {
		// this is not with a single method because the collections are not initialized, yet
		// and if that are copied in the lambdas that will be empty
		tenantManager.addOnTenantAddedListener(info -> notifyForAddedTenant(info.getTenantId()));
		tenantManager.addOnTenantRemoveListener(info -> notifyForRemovedTenant(info.getTenantId()));
	}

	private void notifyForAddedTenant(String tenantId) {
		LOGGER.info("Going to execute {} components for new tenant {}", onTenantAdded.size(), tenantId);
		securityContextManager.executeAsTenant(tenantId).executable(executeComponents(onTenantAdded));
	}

	private void notifyForRemovedTenant(String tenantId) {
		LOGGER.info("Going to execute {} components for removed tenant {}", onTenantRemoved.size(), tenantId);
		securityContextManager.executeAsTenant(tenantId).executable(executeComponents(onTenantRemoved));
	}


	private Executable executeComponents(Collection<Component> components) {
		return () -> components.stream().sorted(TenantStartupObserver::compareComponents).forEach(this::startInNewTx);
	}

	@SuppressWarnings("squid:UnusedPrivateMethod")
	private void startInNewTx(Component component) {
		transactionSupport.invokeInNewTx(() -> {
			try {
				LOGGER.info("Executing: " + component.getName());
				component.start();
			} catch (StartupException e) {
				LOGGER.warn("Could not execute component {} due to: {}", component.getName(), e.getMessage());
				LOGGER.trace("Could not execute component {}", component.getName(), e);
			}
		});
	}

	@SuppressWarnings("squid:UnusedPrivateMethod")
	private static int compareComponents(Component c1, Component c2) {
		int phaseCompare = c1.getPhase().compareTo(c2.getPhase());
		if (phaseCompare == 0) {
			return Double.compare(c1.getOrder(), c2.getOrder());
		}
		return phaseCompare;
	}

	/**
	 * Pauses after deployment phases if there is no active tenant.
	 *
	 * @param event
	 *            the event
	 */
	@RunAsSystem(protectCurrentTenant = false)
	public void phaseInterceptor(@Observes AfterPhaseStartEvent event) {
		if (event.getCompletedPhase() == StartupPhase.DEPLOYMENT) {
			long count = tenantManager.getActiveTenantsInfo(true).count();
			// if no tenants are active pause the processing until tenant is added
			if (count == 0L) {
				createTenantFromExternalConfig();
			}
		}
	}

	void createTenantFromExternalConfig() {
		try {
			tenantInitializer.tryCreatingDefaultTenant();
		} catch (TenantValidationException e) {
			LOGGER.warn("", e);
		}
	}

	/**
	 * Decorates all components annotated with {@link RunAsAllTenantAdmins} and that are not going to be proxied by CDI.
	 * Non proxy instances are not processed correctly when called by startup operations.
	 *
	 * @param event
	 *            the event
	 */
	public void startupEnrichment(@Observes BeforePhaseStartEvent event) {
		List<Component> decorated = new LinkedList<>();

		for (Iterator<Component> it = event.getComponents().iterator(); it.hasNext();) {
			Component component = it.next();
			if (component.isAnnotationPresent(RunAsAllTenantAdmins.class)) {
				it.remove();
				decorated.add(new RunAsTenantComponentDecorator(component, tenantManager, securityContextManager));
			}
			if (component.isAnnotationPresent(OnTenantAdd.class)) {
				onTenantAdded.add(component);
			} else if (component.isAnnotationPresent(OnTenantRemove.class)) {
				onTenantRemoved.add(component);
			}
		}

		decorated.forEach(event::addComponent);
	}
}
