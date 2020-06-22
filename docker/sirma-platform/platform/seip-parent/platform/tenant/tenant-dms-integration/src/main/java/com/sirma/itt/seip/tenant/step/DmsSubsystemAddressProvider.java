package com.sirma.itt.seip.tenant.step;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.seip.adapters.AdaptersConfiguration;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.tenant.wizard.BaseSubsystemTenantAddressProvider;

/**
 * Alfresco 4 address provider.
 *
 * @author BBonev
 */
@ApplicationScoped
@Named("DMSAlfresco4")
public class DmsSubsystemAddressProvider extends BaseSubsystemTenantAddressProvider {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.dms.alfresco4.maxTenantsPerHost", system = true, sensitive = true, type = Integer.class, defaultValue = "10", shared = true, label = "Maximum tenants per DMS host")
	private ConfigurationProperty<Integer> maxTenantsPerHost;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.dms.alfresco4.addresses", system = true, sensitive = true, type = Set.class, defaultValue = "localhost", shared = true, label = "List of server addresses that have a DMS installed")
	private ConfigurationProperty<Set<String>> dmsAddresses;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.dms.alfresco4.protocol", system = true, sensitive = true, defaultValue = "http", shared = true, label = "List of server addresses that have a DMS installed")
	private ConfigurationProperty<String> dmsProtocol;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.dms.alfresco4.portMapping", system = true, sensitive = true, type = Map.class, defaultValue = "all:8080", shared = true, converter = BaseSubsystemTenantAddressProvider.PORT_MAPPING_CONVERTER, label = "The protocol used to access DMS servers")
	private ConfigurationProperty<Map<String, Integer>> dmsPortMapping;

	@Inject
	private AdaptersConfiguration adaptersConfiguration;

	@Override
	public URI provideAddressForNewTenant(String preferredHost) {
		String hostAddress = resolveAddress(preferredHost, adaptersConfiguration.getDmsHostConfiguration(),
				dmsAddresses.get(), maxTenantsPerHost.get().intValue());

		return buildUri(dmsProtocol.get(), hostAddress, getPortForHost(hostAddress), null);
	}

	private int getPortForHost(String host) {
		return getPortForHost(host, dmsPortMapping.get(), 8080);
	}

}
