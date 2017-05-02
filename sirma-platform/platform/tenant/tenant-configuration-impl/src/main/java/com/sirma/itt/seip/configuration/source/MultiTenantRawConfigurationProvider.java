package com.sirma.itt.seip.configuration.source;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.build.ConfigurationInstanceProvider;
import com.sirma.itt.seip.configuration.build.ConfigurationSource;
import com.sirma.itt.seip.configuration.build.RawConfigurationAccessor;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Raw configuration provider that considers the tenant context. If no tenant context is active then no context will be
 * used for configuration querying
 *
 * @author BBonev
 */
@ApplicationScoped
public class MultiTenantRawConfigurationProvider implements RawConfigurationAccessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	private SecurityContext securityContext;

	@Inject
	@ExtensionPoint(ConfigurationSource.NAME)
	private Iterable<ConfigurationSource> sources;

	@Inject
	private ConfigurationInstanceProvider configurationInstanceProvider;

	@Override
	public String getRawConfigurationValue(String name) {
		ConfigurationInstance configuration = configurationInstanceProvider.getConfiguration(name);
		if (configuration == null) {
			// external request for non registered configuration
			return null;
		}
		for (ConfigurationSource source : sources) {
			String value = getTenantConfiguration(name, source, configuration.isSystemConfiguration());
			if (value != null) {
				LOGGER.trace("Read from {} configuration {}", source.getClass().getSimpleName(),
						configuration.isSystemConfiguration() ? "system" : "tenant", name);
				return value;
			}
		}
		return null;
	}

	private String getTenantConfiguration(String name, ConfigurationSource source, boolean isSystemConfig) {
		String value = null;
		// when we have a valid tenant use it for configurations resolving
		// if not or system tenant call the method that ignores tenant parameter
		// also do not search system configurations in the tenant
		boolean isValidNonSystemTenant = !isSystemConfig && securityContext.isActive()
				&& !securityContext.isSystemTenant() && securityContext.getCurrentTenantId() != null;
		if (isValidNonSystemTenant) {
			value = source.getConfigurationValue(name, securityContext.getCurrentTenantId());
		} else {
			value = source.getConfigurationValue(name);
		}
		return value;
	}

	@Override
	public Set<String> getAvailableConfigurations() {
		Set<String> configurations = new HashSet<>(64);
		for (ConfigurationSource source : sources) {
			for (Object key : source.getConfigurations().keySet()) {
				configurations.add(key.toString());
			}
		}
		return Collections.unmodifiableSet(configurations);
	}

}
