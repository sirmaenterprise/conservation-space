package com.sirma.itt.seip.tenant.management;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.runtime.Component;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupException;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.itt.seip.tenant.context.TenantManager;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Tenant add/remove operation observer, responsible for registering all collected tenant
 * operations.
 * 
 * @author nvelkov
 */
@Singleton
public class TenantOperationObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private TenantManager tenantManager;

	@Inject
	private TenantOperationCollector collector;

	@Inject
	private TransactionSupport transactionSupport;

	@Inject
	private SecurityContextManager securityContextManager;

	/**
	 * Register all collected tenant operations.
	 */
	@Startup(phase = StartupPhase.BEFORE_APP_START, order = -1001, transactionMode = TransactionMode.NOT_SUPPORTED)
	public void init() {
		tenantManager.addOnTenantAddedListener(info -> notifyForAddedTenant(info.getTenantId()));
		tenantManager.addOnTenantRemoveListener(info -> notifyForRemovedTenant(info.getTenantId()));
	}

	private void notifyForAddedTenant(String tenantId) {
		securityContextManager.executeAsTenant(tenantId)
				.executable(executeComponents(collector.getTenantAddedComponents()));
	}

	private void notifyForRemovedTenant(String tenantId) {
		securityContextManager.executeAsTenant(tenantId)
				.executable(executeComponents(collector.getTenantRemovedComponets()));
	}

	private Executable executeComponents(Collection<Component> components) {
		return () -> {
			List<Pair<Component, Throwable>> suppressedExceptions = new LinkedList<>();
			for (Component component : components) {
				startInNewTx(component, suppressedExceptions);
			}
			if (!suppressedExceptions.isEmpty()) {
				RollbackedRuntimeException exception = new RollbackedRuntimeException(
						"Could not start components: " + suppressedExceptions.stream()
								.map(Pair::getFirst)
								.map(Component::getName)
								.collect(Collectors.joining(", ", "[", "]")));
				suppressedExceptions.forEach(pair -> exception.addSuppressed(pair.getSecond()));
				throw exception;
			}
		};
	}

	private void startInNewTx(Component component, List<Pair<Component, Throwable>> suppressedExceptions) {
		LOGGER.info("Executing: {}", component.getName());
		try {
			transactionSupport.invokeInNewTx(component::execute);
		} catch (Exception e) {
			suppressedExceptions.add(new Pair<>(component, readException(e)));
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Could not execute component {}", component.getName(), e);
			} else {
				LOGGER.error("Could not execute component {} due to: {}", component.getName(), e.getMessage());
			}
		}
	}

	private Throwable readException(Throwable original) {
		if (original instanceof StartupException && original.getCause() != null) {
			return original.getCause();
		}
		return original;
	}

}
