/**
 *
 */
package com.sirma.itt.seip.configuration.db;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;
import javax.interceptor.Interceptor;

import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.SecurityException;

/**
 * Decorator for {@link ConfigurationManagement} that enforces configuration security access.
 *
 * @author BBonev
 */
@Decorator
@Priority(Interceptor.Priority.APPLICATION)
public abstract class ConfigurationManagementSecurityDecorator implements ConfigurationManagement {

	private static final String NOT_AUTHENTICATED_AS_SYSTEM_ADMIN = "Not authenticated as system admin";
	private static final String NOT_AUTHENTICATED_AS_ADMIN = "Not authenticated as admin";

	@Inject
	@Delegate
	private ConfigurationManagement delegate;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private SecurityContextManager contextManager;

	@Override
	public Collection<Configuration> getAllConfigurations() {
		if (isAuthenticatedAsSystemAdmin()) {
			return delegate.getAllConfigurations();
		}
		throw new SecurityException(NOT_AUTHENTICATED_AS_SYSTEM_ADMIN);
	}

	@Override
	public Collection<Configuration> getSystemConfigurations() {
		if (isAuthenticatedAsSystemAdmin()) {
			return delegate.getSystemConfigurations();
		}
		throw new SecurityException(NOT_AUTHENTICATED_AS_SYSTEM_ADMIN);
	}

	@Override
	public Collection<Configuration> getCurrentTenantConfigurations() {
		if (contextManager.isAuthenticatedAsAdmin()) {
			return delegate.getCurrentTenantConfigurations();
		}
		return delegate
				.getCurrentTenantConfigurations()
					.stream()
					.filter(c -> !c.isSensitive())
					.collect(Collectors.toList());
	}

	@Override
	public void updateSystemConfiguration(Configuration configuration) {
		if (!isAuthenticatedAsSystemAdmin()) {
			throw new SecurityException(NOT_AUTHENTICATED_AS_SYSTEM_ADMIN);
		}
		delegate.updateSystemConfiguration(configuration);
	}

	@Override
	public void updateSystemConfigurations(Collection<Configuration> configuration) {
		if (!isAuthenticatedAsSystemAdmin()) {
			throw new SecurityException(NOT_AUTHENTICATED_AS_SYSTEM_ADMIN);
		}
		delegate.updateSystemConfigurations(configuration);
	}

	@Override
	public void updateConfiguration(Configuration configuration) {
		if (!contextManager.isAuthenticatedAsAdmin()) {
			throw new SecurityException(NOT_AUTHENTICATED_AS_ADMIN);
		}
		delegate.updateConfiguration(configuration);
	}

	@Override
	public void updateConfigurations(Collection<Configuration> configuration) {
		if (!contextManager.isAuthenticatedAsAdmin()) {
			throw new SecurityException(NOT_AUTHENTICATED_AS_ADMIN);
		}
		delegate.updateConfigurations(configuration);
	}

	@Override
	public void removeSystemConfiguration(String key) {
		if (!isAuthenticatedAsSystemAdmin()) {
			throw new SecurityException(NOT_AUTHENTICATED_AS_SYSTEM_ADMIN);
		}
		delegate.removeSystemConfiguration(key);
	}

	@Override
	public void removeConfiguration(String key) {
		if (isAuthenticatedAsSystemAdmin() || contextManager.isAuthenticatedAsAdmin()) {
			delegate.removeConfiguration(key);
			return;
		}
		throw new SecurityException(NOT_AUTHENTICATED_AS_ADMIN);
	}

	@Override
	public Collection<Configuration> addConfigurations(Collection<Configuration> configurations) {
		if (isAuthenticatedAsSystemAdmin() || contextManager.isAuthenticatedAsAdmin()) {
			return delegate.addConfigurations(configurations);
		}
		throw new SecurityException(NOT_AUTHENTICATED_AS_ADMIN);
	}

	private boolean isAuthenticatedAsSystemAdmin() {
		return securityContext.isSystemTenant() && contextManager.isCurrentUserSuperAdmin()
				|| contextManager.isCurrentUserSystem();
	}

}
