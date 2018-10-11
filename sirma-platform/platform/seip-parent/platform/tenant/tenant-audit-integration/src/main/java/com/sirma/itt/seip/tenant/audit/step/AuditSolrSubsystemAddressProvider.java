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
 * Audit Solr address provider.
 *
 * @author bbanchev
 */
@ApplicationScoped
@Named(AuditSolrSubsystemAddressProvider.APPLICATION_NAME)
public class AuditSolrSubsystemAddressProvider extends BaseSubsystemTenantAddressProvider {

	public static final String APPLICATION_NAME = "solr_audit";

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.solr.audit.audit.maxTenantsPerHost", system = true, sensitive = true, type = Integer.class, defaultValue = "10", shared = true, label = "Maximum tenants per Solr host")
	private ConfigurationProperty<Integer> maxTenantsPerHost;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.solr.audit.addresses", system = true, sensitive = true, type = Set.class, defaultValue = "localhost", shared = true, label = "List of server addresses that have a Solr installed")
	private ConfigurationProperty<Set<String>> addresses;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.solr.audit.protocol", system = true, sensitive = true, defaultValue = "http", shared = true, label = "The protocol for the access to Solr servers")
	private ConfigurationProperty<String> protocol;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.solr.audit.portMapping", system = true, sensitive = true, type = Map.class, defaultValue = "all:8983", shared = true, converter = BaseSubsystemTenantAddressProvider.PORT_MAPPING_CONVERTER, label = "Port mapping for Solr servers. To specify single port for all addresses use 'all' like -> all:8080. Format is: address1:port1,address2:port2,all:port3")
	private ConfigurationProperty<Map<String, Integer>> portMapping;

	@Inject
	private AuditConfiguration auditConfiguration;

	@Override
	public URI provideAddressForNewTenant(String preferredHost) {
		String hostName = resolveAddress(preferredHost, auditConfiguration.getSolrHostConfigurationName(),
				addresses.get(), maxTenantsPerHost.get().intValue());

		return buildUri(protocol.get(), hostName, getPortForHost(hostName), getApplicationPath());
	}

	/**
	 * Gets the application path after the address. Default value is {@code /solr}
	 *
	 * @return the application path
	 */
	@SuppressWarnings("static-method")
	protected String getApplicationPath() {
		return "/solr";
	}

	private int getPortForHost(String host) {
		return getPortForHost(host, portMapping.get(), 8983);
	}

}
