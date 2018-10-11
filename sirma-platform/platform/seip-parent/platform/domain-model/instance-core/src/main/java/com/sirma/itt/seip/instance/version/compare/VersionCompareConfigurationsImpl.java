package com.sirma.itt.seip.instance.version.compare;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;

/**
 * Contains configurations related to external service that provides PDFs compare. Also contains builder for the base
 * service URL for the external services that is used from compare operation and compare content store.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
public class VersionCompareConfigurationsImpl implements VersionCompareConfigurations {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@ConfigurationPropertyDefinition(defaultValue = "http", type = String.class, sensitive = true, label = "The protocol of the external tool which provides content compare.")
	private static final String SERVICE_PROTOCOL = "compare.service.protocol";

	@ConfigurationPropertyDefinition(defaultValue = "localhost", type = String.class, sensitive = true, label = "Host address of the external tool which provides content compare.")
	private static final String SERVICE_HOST = "compare.service.host";

	@ConfigurationPropertyDefinition(defaultValue = "8125", type = Integer.class, sensitive = true, label = "The port of the external tool which provides content compare.")
	private static final String SERVICE_PORT = "compare.service.port";

	@ConfigurationGroupDefinition(type = URI.class, properties = { SERVICE_PROTOCOL, SERVICE_HOST, SERVICE_PORT })
	private static final String COMPARE_SERVICE_URI = "compare.service.base.url";

	@Inject
	@Configuration(COMPARE_SERVICE_URI)
	private ConfigurationProperty<URI> compareServiceUrl;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "compare.version.result.file.expiration.time", type = Integer.class, sensitive = true, defaultValue = "24", label = "Configuration that sets the expiration time for the result file after successful compare of versions. The time is set in hours. The default value is 24. This time begins, when the file is stored in the content store. After it ends the file will be deleted from the store.")
	private ConfigurationProperty<Integer> resultFileExpirationTime;

	/**
	 * Builds base URL for accessing the service for content compare.
	 *
	 * @param context
	 *            {@link GroupConverterContext} containing the configurations for building full URL
	 * @return {@link URI} representing the base address for accessing the external service for content compare
	 */
	@ConfigurationConverter(COMPARE_SERVICE_URI)
	static URI buildServiceUrl(GroupConverterContext context) {
		String protocol = context.get(SERVICE_PROTOCOL);
		String host = context.get(SERVICE_HOST);
		Integer port = context.get(SERVICE_PORT);

		try {
			return new URI(protocol, null, host, port.intValue(), VersionCompareContentStore.BASE_SERVICE_PATH, null,
					null);
		} catch (URISyntaxException e) {
			LOGGER.warn("Could not build correct URI from provided configurations for compare service.");
			LOGGER.trace("protocol: {}, host: {}, port: {}", protocol, host, port, e);
			return null;
		}
	}

	@Override
	public ConfigurationProperty<URI> getServiceBaseUrl() {
		return compareServiceUrl;
	}

	@Override
	public int getExpirationTime() {
		return resultFileExpirationTime.get().intValue();
	}

}
