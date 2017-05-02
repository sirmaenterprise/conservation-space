package com.sirma.itt.seip.tenant.wizard;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;

/**
 * Default implementation algorithm for host resolving.
 *
 * @author BBonev
 */
public abstract class BaseSubsystemTenantAddressProvider implements SubsystemTenantAddressProvider {

	/**
	 * The name of the custom port mapping converter that can be used for port mapping conversion of the format: <br>
	 * {@code host1:port1,host2:port2,all:port3,....}
	 */
	public static final String PORT_MAPPING_CONVERTER = "portMappingConverter";

	/**
	 * Port mapping converter that can be used. The name of the converter is {@link #PORT_MAPPING_CONVERTER}.
	 *
	 * @param context
	 *            the context
	 * @return the map
	 * @see #parsePortMapping(String)
	 */
	@ConfigurationConverter(PORT_MAPPING_CONVERTER)
	static Map<String, Integer> portMappingConverter(ConverterContext context) {
		return parsePortMapping(context.getRawValue());
	}

	@Inject
	protected ConfigurationManagement configurationManagement;

	/**
	 * Resolve address by traversing the configurations for specific hostConfiguration key name and comparing it with
	 * allowed addresses. If there is a preferred host it will be returned without processing the rest.
	 *
	 * @param preferredHost
	 *            the preferred host
	 * @param hostConfiguration
	 *            the host configuration
	 * @param addresses
	 *            the addresses
	 * @param maxTenantsPerHost
	 *            the max tenants per host
	 * @return the string
	 */
	protected String resolveAddress(String preferredHost, String hostConfiguration, Set<String> addresses,
			int maxTenantsPerHost) {
		return resolveAddress(preferredHost, hostConfiguration, addresses, maxTenantsPerHost, Function.identity());
	}

	/**
	 * Resolve address by traversing the configurations for specific hostConfiguration key name and comparing it with
	 * allowed addresses. If there is a preferred host it will be returned without processing the rest.
	 * <p>
	 * The method allows additionally for adding a configuration value transformer before processing the values.
	 *
	 * @param preferredHost
	 *            the preferred host
	 * @param hostConfiguration
	 *            the host configuration
	 * @param addresses
	 *            the addresses
	 * @param maxTenantsPerHost
	 *            the max tenants per host
	 * @param configValueConverter
	 *            the config value converter
	 * @return the string
	 */
	protected String resolveAddress(String preferredHost, String hostConfiguration, Set<String> addresses,
			int maxTenantsPerHost, Function<String, String> configValueConverter) {
		String hostName = preferredHost;
		if (hostName == null) {
			Stream<String> usedAddresses = streamUsedAddresses(hostConfiguration, configValueConverter);

			Optional<String> host = SubsystemTenantAddressProvider.resolveAvailableHost(usedAddresses,
					addresses,
					maxTenantsPerHost,
					getUndeterminedPicker());

			if (!host.isPresent()) {
				throw new RollbackedRuntimeException("Could not determine host to use!");
			}
			hostName = host.get();
		}
		return hostName;
	}

	/**
	 * Gets the undetermined picker to use when resolving addresses. The default value is
	 * {@link CollectionUtils#randomPicker()}
	 *
	 * @return the undetermined picker
	 */
	@SuppressWarnings("static-method")
	protected Function<Collection<String>, String> getUndeterminedPicker() {
		return CollectionUtils.randomPicker();
	}

	/**
	 * Streams the used addresses. The method streams all configuration values by filtering the one with the given
	 * configuration name and applying the given transformer before return.
	 *
	 * @param hostConfiguration
	 *            the host configuration name to filter
	 * @param configValueConverter
	 *            the config value converter
	 * @return the stream
	 */
	protected Stream<String> streamUsedAddresses(String hostConfiguration,
			Function<String, String> configValueConverter) {
		return configurationManagement.getAllConfigurations()
				.stream()
				.filter(e -> hostConfiguration.equals(e.getConfigurationKey()))
				.map(c -> Objects.toString(c.getValue(), null))
				.filter(StringUtils::isNotBlank)
				.map(configValueConverter);
	}

	/**
	 * Creates an undetermined picker. The picker will honor the preferred host if not <code>null</code> otherwise
	 * random address will be chosen.
	 *
	 * @param preferredHost
	 *            the preferred host
	 * @return a picker function.
	 */
	public static Function<Collection<String>, String> createUndeterminedPicker(String preferredHost) {
		if (preferredHost == null) {
			return CollectionUtils.randomPicker();
		}
		return c -> {
			if (c.contains(preferredHost)) {
				return preferredHost;
			}
			Function<Collection<String>, String> picker = CollectionUtils.randomPicker();
			return picker.apply(c);
		};
	}

	/**
	 * Gets the port for host. The host is searched in the given mapping. If not found a default mapping is searched. If
	 * not found a given default port will be returned.
	 *
	 * @param host
	 *            the host
	 * @param mapping
	 *            the mapping
	 * @param defaultPort
	 *            the default port to return if default mapping is missing.
	 * @return the port for host
	 */
	public static int getPortForHost(String host, Map<String, Integer> mapping, int defaultPort) {
		if (mapping == null) {
			return defaultPort;
		}
		Integer port = mapping.get(host);
		if (port == null) {
			port = mapping.get("all");
		}
		if (port == null) {
			port = Integer.valueOf(defaultPort);
		}
		return port.intValue();
	}

	/**
	 * Parses the port mapping configuration to an actual {@link Map} instance where the keys are the hosts and the
	 * values are the configured port
	 *
	 * @param rawValue
	 *            the raw value
	 * @return the port mapping
	 */
	public static Map<String, Integer> parsePortMapping(String rawValue) {
		String[] mappings = rawValue.split("\\s*,\\s*");
		Map<String, Integer> resultMapping = CollectionUtils.createHashMap(mappings.length);
		for (String mapping : mappings) {
			String[] split = mapping.split(":", 2);
			resultMapping.put(split[0], Integer.valueOf(split[1]));
		}
		return resultMapping;
	}

	/**
	 * Builds {@link URI} using the given parameters
	 *
	 * @param protocol
	 *            the protocol
	 * @param host
	 *            the host
	 * @param port
	 *            the port
	 * @param appPath
	 *            the application path
	 * @return the uri
	 */
	public static URI buildUri(String protocol, String host, int port, String appPath) {
		try {
			return new URI(protocol, null, host, port, appPath, null, null);
		} catch (URISyntaxException e) {
			throw new RollbackedRuntimeException(e);
		}
	}

}
