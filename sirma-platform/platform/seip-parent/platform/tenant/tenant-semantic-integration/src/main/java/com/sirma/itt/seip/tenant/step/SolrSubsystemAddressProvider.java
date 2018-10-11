package com.sirma.itt.seip.tenant.step;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.emf.solr.configuration.SolrConfiguration;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.tenant.wizard.BaseSubsystemTenantAddressProvider;

/**
 * Semantic GraphDb address provider.
 *
 * @author BBonev
 */
@ApplicationScoped
@Named(SolrSubsystemAddressProvider.APPLICATION_NAME)
public class SolrSubsystemAddressProvider extends BaseSubsystemTenantAddressProvider {
	/** The subsystem name. */
	public static final String APPLICATION_NAME = "solr";

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.solr.maxTenantsPerHost", system = true, sensitive = true, type = Integer.class, defaultValue = "10", shared = true, label = "Maximum tenants per Solr host")
	private ConfigurationProperty<Integer> maxSolrCoresPerHost;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.solr.addresses", system = true, sensitive = true, type = Set.class, defaultValue = "localhost", shared = true, label = "List of server addresses that have a Solr installed")
	private ConfigurationProperty<Set<String>> solrAddresses;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.solr.protocol", system = true, sensitive = true, defaultValue = "http", shared = true, label = "The protocol for the access to Solr servers")
	private ConfigurationProperty<String> solrAddressProtocol;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.solr.portMapping", system = true, sensitive = true, type = Map.class, defaultValue = "all:8983", shared = true, converter = BaseSubsystemTenantAddressProvider.PORT_MAPPING_CONVERTER, label = "Port mapping for Solr servers. To specify single port for all addresses use 'all' like -> all:8080. Format is: address1:port1,address2:port2,all:port3")
	private ConfigurationProperty<Map<String, Integer>> solrPortMapping;

	@Inject
	private SolrConfiguration solrConfiguration;

	@Override
	public URI provideAddressForNewTenant(String preferredHost) {
		String host = resolveAddress(preferredHost, solrConfiguration.getSolrHostConfiguration(),
				solrAddresses.get(), maxSolrCoresPerHost.get().intValue());

		return buildUri(solrAddressProtocol.get(), host, getPortForHost(host), getApplicationPath());
	}

	private int getPortForHost(String host) {
		return getPortForHost(host, solrPortMapping.get(), 8983);
	}

	@SuppressWarnings("static-method")
	protected String getApplicationPath() {
		return "/" + APPLICATION_NAME;
	}
}
