package com.sirma.itt.seip.tenant.audit.step;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.emf.audit.configuration.AuditConfiguration;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.tenant.wizard.BaseSubsystemTenantAddressProvider;

/**
 * PostgreSQL relational db address provider for audit module.
 *
 * @author BBonev
 */
@ApplicationScoped
@Named("postgresql_audit")
public class AuditPostgresSubsystemAddressProvider extends BaseSubsystemTenantAddressProvider {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.auditDb.postgresql.maxTenantsPerHost", system = true, sensitive = true, type = Integer.class, defaultValue = "100", shared = true, label = "Maximum tenants per audit PostgreSQL host")
	private ConfigurationProperty<Integer> maxTenantsPerHost;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.auditDb.postgresql.addresses", system = true, sensitive = true, type = Set.class, defaultValue = "localhost", shared = true, label = "List of server addresses that have a PostgreSQL installed for auditing")
	private ConfigurationProperty<Set<String>> auditDbAddresses;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.auditDb.postgresql.portMapping", system = true, sensitive = true, type = Map.class, defaultValue = "all:5432", shared = true, converter = BaseSubsystemTenantAddressProvider.PORT_MAPPING_CONVERTER, label = "Port mapping for Audit PostgreSQL servers. To specify single port for all addresses use 'all' like -> all:8080. Format is: address1:port1,address2:port2,all:port3")
	private ConfigurationProperty<Map<String, Integer>> auditDbPortMapping;

	@Inject
	private AuditConfiguration databaseConfiguration;

	@Override
	public URI provideAddressForNewTenant(String preferredHost) {
		String host = resolveAddress(preferredHost, databaseConfiguration.getDatabaseAddressConfigurationName(),
				auditDbAddresses.get(), maxTenantsPerHost.get().intValue());

		return buildUri("postgresql", host, getPortForHost(host), null);
	}

	private int getPortForHost(String host) {
		return getPortForHost(host, auditDbPortMapping.get(), 5432);
	}
}
