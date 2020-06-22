package com.sirma.itt.seip.configuration.sync;

import static org.apache.commons.lang.StringUtils.trimToNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.build.ConfigurationInstanceProvider;
import com.sirma.itt.seip.configuration.build.RawConfigurationAccessor;
import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.annotation.OnTenantAdd;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Synchronizes configuration value for all configurations to database on application startup
 *
 * @author BBonev
 */
@ApplicationScoped
public class TenantConfigurationSynchronization {

	@Inject
	private RawConfigurationAccessor configurationAccessor;
	@Inject
	private ConfigurationInstanceProvider configurationInstanceProvider;

	@Inject
	private ConfigurationManagement configurationManagement;
	@Inject
	private SecurityContext securityContext;

	/**
	 * Sync system configurations.
	 */
	@Startup(phase = StartupPhase.BEFORE_APP_START, order = 1)
	public void syncSystemConfigurations() {

		// skip group configs
		Predicate<ConfigurationInstance> filer = c -> !c.isComplex();
		syncConfigurations(filer, configurationAccessor::getRawConfigurationValue);
	}

	/**
	 * Sync tenant configurations.
	 */
	@OnTenantAdd
	@RunAsAllTenantAdmins
	@Startup(phase = StartupPhase.BEFORE_APP_START, order = 1.1)
	public void syncTenantConfigurations() {

		syncConfigurations(tenantConfigFilter(), configurationAccessor::getRawConfigurationValue);
	}

	/**
	 * Skip group and system configurations
	 *
	 * @return the predicate
	 */
	static Predicate<ConfigurationInstance> tenantConfigFilter() {
		return c -> !c.isComplex() && !c.isSystemConfiguration();
	}

	/**
	 * Sync configurations by first filtering them using the given predicate and then converting them to
	 * {@link Configuration} instances populated with values provided by the given function.
	 *
	 * @param filer
	 *            the filer
	 * @param rawAccessor
	 *            the raw accessor
	 * @return the added configurations
	 */
	Collection<Configuration> syncConfigurations(Predicate<ConfigurationInstance> filer,
			Function<String, String> rawAccessor) {
		List<Configuration> configurations = configurationInstanceProvider
				.getAllInstances()
					.stream()
					.filter(filer)
					.map(c -> toConfiguration(c, rawAccessor))
					.collect(Collectors.toList());

		return configurationManagement.addConfigurations(configurations);
	}

	private Configuration toConfiguration(ConfigurationInstance instance, Function<String, String> rawAccessor) {
		String rawValue = rawAccessor.apply(instance.getName());
		// value not configured anywhere use default if any
		if (rawValue == null) {
			rawValue = trimToNull(((ConfigurationPropertyDefinition) instance.getAnnotation()).defaultValue());
		}
		return new Configuration(instance.getName(), rawValue, securityContext.getCurrentTenantId());
	}
}
