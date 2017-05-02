/**
 *
 */
package com.sirma.itt.seip.configuration.build;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.MultiTenantConfigurationProperty;
import com.sirma.itt.seip.configuration.convert.PropertyConverterProvider;
import com.sirma.itt.seip.configuration.sync.ReadWriteSynchronizer;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tenant.context.TenantManager;

/**
 * Configuration builder that builds tenant aware configuration properties. These properties are proxies of properties
 * build by
 * {@link DefaultConfigurationBuilder#build(ConfigurationInstance, RawConfigurationAccessor, PropertyConverterProvider, ConfigurationProvider, ConfigurationInstanceProvider)}
 * method.
 *
 * @author BBonev
 */
@ApplicationScoped
public class MultiTenantConfigurationBuilder implements ConfigurationBuilder {

	@Inject
	private SecurityContext securityContext;

	@Inject
	private Instance<TenantManager> tenantManager;

	@Override
	public <T> ConfigurationProperty<T> buildProperty(ConfigurationInstance configuration,
			RawConfigurationAccessor configurationValueProvider, PropertyConverterProvider converterProvider,
			ConfigurationProvider configurationProvider, ConfigurationInstanceProvider configurationInstanceProvider)
					throws ConfigurationException {

		// for system single value configurations no need to build complex proxies
		if (configuration.isSystemConfiguration()) {
			return DefaultConfigurationBuilder.build(configuration, configurationValueProvider, converterProvider,
					configurationProvider, configurationInstanceProvider);
		}

		// the actual per tenant properties are build lazily on request
		return new MultiTenantConfigurationProperty<>(configuration, securityContext::getCurrentTenantId,
				tenantManager.get()::addOnTenantRemoveListener, new ReadWriteSynchronizer(),
				() -> DefaultConfigurationBuilder.build(configuration, configurationValueProvider, converterProvider,
						configurationProvider, configurationInstanceProvider));
	}

}
