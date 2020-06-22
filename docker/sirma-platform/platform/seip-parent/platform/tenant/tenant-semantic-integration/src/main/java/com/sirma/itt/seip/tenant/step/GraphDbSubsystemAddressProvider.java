package com.sirma.itt.seip.tenant.step;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.tenant.wizard.BaseSubsystemTenantAddressProvider;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;

/**
 * Semantic GraphDb address provider.
 *
 * @author BBonev
 */
@ApplicationScoped
@Named(GraphDbSubsystemAddressProvider.APPLICATION_NAME)
public class GraphDbSubsystemAddressProvider extends BaseSubsystemTenantAddressProvider {
	/** The subsystem name. */
	public static final String APPLICATION_NAME = "graphdb-workbench";

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.semantic.graphdb.maxTenantsPerHost", system = true, sensitive = true, type = Integer.class, defaultValue = "10", shared = true, label = "Maximum tenants per GraphDb host")
	private ConfigurationProperty<Integer> maxReposPerHost;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.semantic.graphdb.addresses", system = true, sensitive = true, type = Set.class, defaultValue = "localhost", shared = true, label = "List of server addresses that have a GraphDb installed")
	private ConfigurationProperty<Set<String>> gdbAddresses;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.semantic.graphdb.protocol", system = true, sensitive = true, defaultValue = "http", shared = true, label = "The protocol for the access to GraphDb servers")
	private ConfigurationProperty<String> gdbProtocol;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.semantic.graphdb.portMapping", system = true, sensitive = true, type = Map.class, defaultValue = "all:8080", shared = true, converter = BaseSubsystemTenantAddressProvider.PORT_MAPPING_CONVERTER, label = "Port mapping for GraphDb servers. To specify single port for all addresses use 'all' like -> all:8080. Format is: address1:port1,address2:port2,all:port3")
	private ConfigurationProperty<Map<String, Integer>> gdbPortMapping;
	
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "subsystem.semantic.graphdb.applicationName", system = true, sensitive = true, shared = true, label = "The name of the application where the GraphDB is deployed. It can be: graphdb-workbench, graphdb-server or empty when the GraphDB is started as standalone application")
	private ConfigurationProperty<String> gdbApplicationName;

	@Inject
	private SemanticConfiguration semanticConfiguration;

	@Override
	public URI provideAddressForNewTenant(String preferredHost) {
		String semanticHost = resolveAddress(preferredHost, semanticConfiguration.getServerURLConfiguration(),
				gdbAddresses.get(), maxReposPerHost.get().intValue(), this::extractAddress);
		
		return buildUri(gdbProtocol.get(), semanticHost, getPortForHost(semanticHost), "/" + gdbApplicationName.computeIfNotSet(() -> ""));
	}

	protected String extractAddress(String configuration) {
		return configuration.substring(configuration.indexOf("//"), configuration.lastIndexOf(':'));
	}

	private int getPortForHost(String host) {
		return getPortForHost(host, gdbPortMapping.get(), 8080);
	}

}
