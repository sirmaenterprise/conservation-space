package com.sirma.itt.seip.tenant.security.interceptor;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.runtime.AbstractDecoratingStartupComponent;
import com.sirma.itt.seip.runtime.ComponentValidationException;
import com.sirma.itt.seip.runtime.StartupComponent;
import com.sirma.itt.seip.runtime.boot.StartupException;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.context.TenantManager;

/**
 * Decorating component that executes the decorated component for every active tenant.
 *
 * @author BBonev
 */
public class RunAsTenantComponentDecorator extends AbstractDecoratingStartupComponent {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final TenantManager tenantManager;
	private final SecurityContextManager securityContextManager;

	/**
	 * Instantiates a new run as tenant component decorator.
	 *
	 * @param decorated
	 *            the decorated
	 * @param tenantManager
	 *            the tenant manager
	 * @param securityContextManager
	 *            the security context manager
	 */
	public RunAsTenantComponentDecorator(StartupComponent decorated, TenantManager tenantManager,
			SecurityContextManager securityContextManager) {
		super(decorated);
		this.tenantManager = tenantManager;
		this.securityContextManager = securityContextManager;
		if (!isAnnotationPresent(RunAsAllTenantAdmins.class)) {
			throw new ComponentValidationException(
					"Cannot decorate component that is not annotated with " + RunAsAllTenantAdmins.class.getName());
		}
	}

	@Override
	public void execute() {
		RunAsAllTenantAdmins annotation = getAnnotation(RunAsAllTenantAdmins.class);
		// in case of parallel processing not to have some concurrent modification exception
		Collection<Throwable> exceptions = new ConcurrentLinkedDeque<>();

		Stream<TenantInfo> tenants;
		if (annotation.includeInactive()) {
			tenants = tenantManager.getAllTenantsInfo(annotation.parallel());
		} else {
			tenants = tenantManager.getActiveTenantsInfo(annotation.parallel());
		}
		tenants.forEach(info -> executeInTenantContext(info, exceptions));

		// collect all exceptions for all tenants and then throw a single exception containing all of them
		// this way a functionality failing for one tenant will not stop for others
		// the question is if we throw this exception what will happen with the running transaction,
		// because the StartupException is marked for transaction rollback

		if (!exceptions.isEmpty()) {
			StartupException toThrow = new StartupException("Component " + getName() + " failed to start");
			exceptions.forEach(toThrow::addSuppressed);
			throw toThrow;
		}
	}

	void executeInTenantContext(TenantInfo tenantInfo, Collection<Throwable> exceptions) {
		securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
		try {
			getDecorated().execute();
		} catch (Exception e) {
			exceptions.add(e);
			LOGGER.warn("Could not execute component {} for tenant {}", getName(), tenantInfo.getTenantId(), e);
		} finally {
			securityContextManager.endContextExecution();
		}
	}
}
