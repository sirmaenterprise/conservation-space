package com.sirma.itt.seip.configuration;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneId;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.ConverterContext;
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
	private static final String PATH_DELIMITER = "/";

	/**
	 * "If application is in development mode or not"
	 */
	private static final String APPLICATION_MODE_DEVELOPMENT = "application.mode.development";

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "system.language", defaultValue = "en", label = "The default system language. If nothing other is specified or for operations where there is no logged in user.")
	private ConfigurationProperty<String> systemLanguage;

	/** The system default host name. */
	@ConfigurationPropertyDefinition(system = true, label = "The default ip address/hostname ")
	private static final String SYSTEM_DEFAULT_HOST_NAME = "system.default.host.name";

	/** The system default host port. */
	@ConfigurationPropertyDefinition(system = true, type = Integer.class, defaultValue = "8080", label = "The default socket port ")
	private static final String SYSTEM_DEFAULT_HOST_PORT = "system.default.host.port";

	/** The system default host protocol. */
	@ConfigurationPropertyDefinition(system = true, defaultValue = "http", label = "The default socket protocol ")
	private static final String SYSTEM_DEFAULT_HOST_PROTOCOL = "system.default.host.protocol";

	/** The system default context path */
	@ConfigurationPropertyDefinition(system = true, defaultValue = "/emf", label = "The REST service invoker path")
	private static final String SYSTEM_DEFAULT_CONTEXT_PATH = "system.default.context.path";

	@ConfigurationGroupDefinition(system = true, type = URI.class, properties = { SYSTEM_DEFAULT_HOST_NAME,
			SYSTEM_DEFAULT_HOST_PORT, SYSTEM_DEFAULT_HOST_PROTOCOL })
	private static final String SYSTEM_ACCESS_URI = "system.default.url";

	/** The system REST activator path */
	@ConfigurationPropertyDefinition(system = true, defaultValue = "/api", label = "The REST activator path")
	private static final String SYSTEM_REST_ACTIVATOR = "system.default.restActivatorPath";

	@ConfigurationGroupDefinition(system = true, type = URI.class, properties = { SYSTEM_ACCESS_URI,
			SYSTEM_REST_ACTIVATOR })
	private static final String REST_ACCESS_URI = "system.rest.url";

	@ConfigurationGroupDefinition(system = true, type = URI.class, properties = { SYSTEM_ACCESS_URI,
			SYSTEM_DEFAULT_CONTEXT_PATH, SYSTEM_REST_ACTIVATOR })
	private static final String REST_CONTEXT_PATH_URI = "system.rest.context.path.url";

	@ConfigurationPropertyDefinition(defaultValue = "GMT", type = ZoneId.class, label = "the ID for a TimeZone, either an abbreviation such as \"PST\", a full name such as \"Europe/Sofia\","
			+ " or a custom ID such as \"GMT-8\" or \"UTC-8:00\". If the format of the custom ID is invalid, GMT will be used.")
	private static final String DATE_TIMEZONE_ID = "date.timezone.id";

	@Inject
	@Configuration(SYSTEM_ACCESS_URI)
	private ConfigurationProperty<URI> proxyPreviewHost;

	@Inject
	@Configuration(REST_ACCESS_URI)
	private ConfigurationProperty<URI> restAccessURI;

	@Inject
	@Configuration(REST_CONTEXT_PATH_URI)
	private ConfigurationProperty<URI> restRemoteAccessURI;

	@Inject
	@Configuration(DATE_TIMEZONE_ID)
	protected ConfigurationProperty<ZoneId> timeZoneID;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = APPLICATION_MODE_DEVELOPMENT, type = Boolean.class, defaultValue = "false", system = true, subSystem = "application", label = "If application is in development mode or not")
	private ConfigurationProperty<Boolean> applicationModeDevelopement;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "application.name", type = String.class, defaultValue = "Sirma Enterprise Platform", subSystem = "application", label = "Customer brandable name")
	private ConfigurationProperty<String> applicationName;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "ui2.url", label = "URL where UI2 is located.")
	private ConfigurationProperty<String> ui2Url;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "help.support.email", label = "The e-mail address of the help support.")
	private ConfigurationProperty<String> helpSuportEmail;

	@Inject
	@Configuration(SYSTEM_DEFAULT_HOST_PROTOCOL)
	private ConfigurationProperty<String> defaultProtocol;

	@Inject
	@Configuration(SYSTEM_DEFAULT_HOST_NAME)
	private ConfigurationProperty<String> defaultHost;

	@Inject
	@Configuration(SYSTEM_DEFAULT_HOST_PORT)
	private ConfigurationProperty<Integer> defaultPort;

	@Inject
	@Configuration(SYSTEM_DEFAULT_CONTEXT_PATH)
	private ConfigurationProperty<String> defaultContextPath;

	@ConfigurationConverter(SYSTEM_ACCESS_URI)
	static URI buildAccessURL(GroupConverterContext context) {
		String protocol = context.get(SYSTEM_DEFAULT_HOST_PROTOCOL);
		String hostName = context.get(SYSTEM_DEFAULT_HOST_NAME);
		Integer hostPort = context.get(SYSTEM_DEFAULT_HOST_PORT);

		try {
			return new URI(protocol, null, hostName, hostPort, null, null, null);
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

		if (address.endsWith(PATH_DELIMITER)) {
			address = address.substring(0, address.length() - 1);
		}
		if (!restPath.startsWith(PATH_DELIMITER)) {
			restPath = PATH_DELIMITER + restPath;
		}
		return URI.create(address + restPath);
	}

	@ConfigurationConverter(REST_CONTEXT_PATH_URI)
	static URI buildRestInvokerURL(GroupConverterContext context) {
		URI systemAddress = context.get(SYSTEM_ACCESS_URI);
		String restContextPath = context.get(SYSTEM_DEFAULT_CONTEXT_PATH);
		String restActivatorPath = context.get(SYSTEM_REST_ACTIVATOR);
		String address = systemAddress.toASCIIString();

		if (address.endsWith(PATH_DELIMITER)) {
			address = address.substring(0, address.length() - 1);
		}
		if (!restContextPath.startsWith(PATH_DELIMITER)) {
			restContextPath = PATH_DELIMITER + restContextPath;
		}
		if (!restActivatorPath.startsWith(PATH_DELIMITER)) {
			restActivatorPath = PATH_DELIMITER + restActivatorPath;
		}
		return URI.create(address + restContextPath + restActivatorPath);
	}

	@ConfigurationConverter(DATE_TIMEZONE_ID)
	static ZoneId buildZoneId(ConverterContext context) {
		return ZoneId.of(context.getRawValue());
	}

	@Override
	public ConfigurationProperty<String> getSystemLanguageConfiguration() {
		return systemLanguage;
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
	public ConfigurationProperty<URI> getRESTRemoteAccessUrl() {
		return restRemoteAccessURI;
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

	@Override
	public ConfigurationProperty<ZoneId> getTimeZoneID() {
		return timeZoneID;
	}

	@Override
	public ConfigurationProperty<String> getHelpSuportEmail() {
		return helpSuportEmail;
	}

	@Override
	public ConfigurationProperty<String> getDefaultProtocol() {
		return defaultProtocol;
	}

	@Override
	public ConfigurationProperty<String> getDefaultHost() {
		return defaultHost;
	}

	@Override
	public ConfigurationProperty<Integer> getDefaultPort() {
		return defaultPort;
	}

	@Override
	public ConfigurationProperty<String> getDefaultContextPath() {
		return defaultContextPath;
	}

}
