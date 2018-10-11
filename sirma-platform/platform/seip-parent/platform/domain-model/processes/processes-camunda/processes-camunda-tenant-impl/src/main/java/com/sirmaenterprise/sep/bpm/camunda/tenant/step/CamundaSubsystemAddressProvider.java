package com.sirmaenterprise.sep.bpm.camunda.tenant.step;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.db.DatabaseConfiguration;
import com.sirma.itt.seip.tenant.wizard.BaseSubsystemTenantAddressProvider;

/**
 * PostgreSQL relational db address provider for Camunda.
 *
 * @author bbanchev
 */
@ApplicationScoped
@Named("postgresql_camunda")
public class CamundaSubsystemAddressProvider extends BaseSubsystemTenantAddressProvider {


	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.camunda.postgresql.portMapping", system = true, sensitive = true, type = Map.class, defaultValue = "all:5432", shared = true, converter = BaseSubsystemTenantAddressProvider.PORT_MAPPING_CONVERTER, label = "Port mapping for PostgreSQL servers. To specify single port for all addresses use 'all' like -> all:8080. Format is: address1:port1,address2:port2,all:port3")
	private ConfigurationProperty<Map<String, Integer>> camundaPortMapping;
	
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.camunda.postgresql.maxTenantsPerHost", system = true, sensitive = true, type = Integer.class, defaultValue = "100", shared = true, label = "Maximum tenants per PostgreSQL host")
	private ConfigurationProperty<Integer> camundaMaxTenantsPerHost;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.camunda.postgresql.addresses", system = true, sensitive = true, type = Set.class, defaultValue = "localhost", shared = true, label = "List of server addresses that have a PostgreSQL installed")
	private ConfigurationProperty<Set<String>> camundaAddresses;

	@Inject
	private DatabaseConfiguration databaseConfiguration;

	@Override
	public URI provideAddressForNewTenant(String preferredHost) {
		String camundaHostName = resolveAddress(preferredHost, databaseConfiguration.getDatabaseAddressConfigurationName(),
				camundaAddresses.get(), camundaMaxTenantsPerHost.get().intValue());

		return buildUri("postgresql", camundaHostName, getPortForHost(camundaHostName), null);
	}

	private int getPortForHost(String host) {
		return getPortForHost(host, camundaPortMapping.get(), 5432);
	}

}
