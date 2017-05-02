package com.sirma.itt.seip.configuration;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;

/**
 * Expose the default system configuration that are globally valid
 *
 * @author bbanchev
 */
@ApplicationScoped
public class SystemConfigurtionImpl implements SystemConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	/** serialVersionUID. */
	private static final long serialVersionUID = -1031801389107117795L;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "system.language", defaultValue = "en", label = "The default system language. If nothing other is specified or for operations where there is no logged in user.")
	private ConfigurationProperty<String> systemLanguage;

	/** The system default host name. */
	@ConfigurationPropertyDefinition(system = true, label = "The default ip address/hostname ")
	private static final String SYSTEM_DEFAULT_HOST_NAME = "system.default.host.name";

	/** The system default host port. */
	@ConfigurationPropertyDefinition(system = true, type = Integer.class, label = "The default socket port ")
	private static final String SYSTEM_DEFAULT_HOST_PORT = "system.default.host.port";

	/** The system default host protocol. */
	@ConfigurationPropertyDefinition(system = true, defaultValue = "http", label = "The default socket protocol ")
	private static final String SYSTEM_DEFAULT_HOST_PROTOCOL = "system.default.host.protocol";

	/** The host and port for preview of document or image */
	@ConfigurationPropertyDefinition(system = true, label = "The host and port for preview of document or image.")
	private static final String PREVIEW_PROXY_HOST = "system.proxyAccessUrl";

	@ConfigurationGroupDefinition(system = true, type = URI.class, properties = { SYSTEM_DEFAULT_HOST_NAME,
			SYSTEM_DEFAULT_HOST_PORT, SYSTEM_DEFAULT_HOST_PROTOCOL, PREVIEW_PROXY_HOST })
	private static final String SYSTEM_ACCESS_URI = "system.default.url";

	/** The system REST activator path */
	@ConfigurationPropertyDefinition(system = true, type = String.class, defaultValue = "/api", label = "The REST activator path")
	private static final String SYSTEM_REST_ACTIVATOR = "system.default.restActivatorPath";

	@ConfigurationGroupDefinition(system = true, type = URI.class, properties = { SYSTEM_ACCESS_URI,
			SYSTEM_REST_ACTIVATOR })
	private static final String REST_ACCESS_URI = "system.rest.url";

	@Inject
	@Configuration(SYSTEM_ACCESS_URI)
	private ConfigurationProperty<URI> proxyPreviewHost;

	@Inject
	@Configuration(REST_ACCESS_URI)
	private ConfigurationProperty<URI> restAccessURI;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = APPLICATION_MODE_DEVELOPMENT, type = Boolean.class, defaultValue = "false", system = true, subSystem = "application", label = "If application is in development mode or not")
	private ConfigurationProperty<Boolean> applicationModeDevelopement;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "appliction.name", type = String.class, defaultValue = "Sirma Enterprise Platform", subSystem = "application", label = "Customer brandable name")
	private ConfigurationProperty<String> applicationName;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "ui2.url", label = "URL where UI2 is located.")
	private ConfigurationProperty<String> ui2Url;

	@ConfigurationConverter(SYSTEM_ACCESS_URI)
	static URI buildAccessURL(GroupConverterContext context) {
		String protocol = context.get(SYSTEM_DEFAULT_HOST_PROTOCOL);
		String hostName = context.get(SYSTEM_DEFAULT_HOST_NAME);
		Integer hostPort = context.get(SYSTEM_DEFAULT_HOST_PORT);
		String proxyPreviewHost = context.get(PREVIEW_PROXY_HOST);

		// Note: this can cause a potential problem if the ngix proxy is on different protocol than
		// the seip jboss.

		if (StringUtils.isNotNullOrEmpty(proxyPreviewHost)) {
			String[] previewHostAndPort = proxyPreviewHost.split("_|:");

			// append host
			hostName = previewHostAndPort[0];
			if (previewHostAndPort.length == 2) {
				// append port if applicable
				hostPort = Integer.valueOf(previewHostAndPort[1]);
			} else {
				hostPort = null;
			}
		} else {
			LOGGER.warn("Configuration property [{}] is not set. Assuming no proxy is used.", PREVIEW_PROXY_HOST);
		}

		if (hostName == null) {
			LOGGER.warn("System access URI could not be build. Set {} or {}", SYSTEM_DEFAULT_HOST_NAME,
					PREVIEW_PROXY_HOST);
			return null;
		}

		int port = -1;
		if (hostPort != null) {
			port = hostPort.intValue();
		}
		try {
			return new URI(protocol, null, hostName, port, null, null, null);
		} catch (URISyntaxException e) {
			LOGGER.warn("", e);
			return null;
		}
	}

	@ConfigurationConverter(REST_ACCESS_URI)
	static URI buildRestURL(GroupConverterContext context) {
		URI systemAddress = context.get(SYSTEM_ACCESS_URI);
		String restPath = context.get(SYSTEM_REST_ACTIVATOR);
		String address = systemAddress.toASCIIString();

		if (address.endsWith("/")) {
			address = address.substring(0, address.length() - 1);
		}
		if (!restPath.startsWith("/")) {
			restPath = "/" + restPath;
		}
		return URI.create(address + restPath);
	}

	@Override
	public String getSystemLanguage() {
		return systemLanguage.get();
	}

	@Override
	public ConfigurationProperty<URI> getSystemAccessUrl() {
		return proxyPreviewHost;
	}

	@Override
	public ConfigurationProperty<URI> getRESTAccessUrl() {
		return restAccessURI;
	}

	@Override
	public ConfigurationProperty<Boolean> getApplicationMode() {
		return applicationModeDevelopement;
	}

	@Override
	public ConfigurationProperty<String> getUi2Url() {
		return ui2Url;
	}

	@Override
	public ConfigurationProperty<String> getApplicationName() {
		return applicationName;
	}

}
