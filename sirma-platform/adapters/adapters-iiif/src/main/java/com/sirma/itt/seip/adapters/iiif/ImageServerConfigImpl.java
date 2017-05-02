package com.sirma.itt.seip.adapters.iiif;

import java.net.URI;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.IntegerPair;
import com.sirma.itt.seip.adapters.remote.FTPConfiguration;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;

/**
 * Implementation for the configuration of an image server.
 *
 * @author Nikolay Ch
 */
@Singleton
class ImageServerConfigImpl implements ImageServerConfigurations {

	@ConfigurationPropertyDefinition(sensitive = true, label = "FTP server host address")
	private static final String FTP_HOST = "image.server.host";

	@ConfigurationPropertyDefinition(defaultValue = "21", sensitive = true, type = Integer.class, label = "FTP server port")
	private static final String FTP_PORT = "image.server.port";

	@ConfigurationPropertyDefinition(defaultValue = "/", sensitive = true, label = "Remote directory to store the files")
	private static final String FTP_REMOTE_DIR = "image.server.remote.dir";

	@ConfigurationPropertyDefinition(sensitive = true, label = "The client's name")
	private static final String FTP_USER = "image.server.username";

	@ConfigurationPropertyDefinition(sensitive = true, password = true, label = "The client's pass")
	private static final String FTP_PASS = "image.server.password";

	@ConfigurationPropertyDefinition(defaultValue = "false", sensitive = true, type = Boolean.class, label = "Secure connection necessarity")
	private static final String FTP_SECURE = "image.server.secure";

	@ConfigurationPropertyDefinition(defaultValue = "true", sensitive = true, type = Boolean.class, label = "If the client should use FTP active module for file transfer or passive")
	private static final String FTP_ACTIVE_MODE = "image.server.ftpActiveMode";

	@ConfigurationPropertyDefinition(defaultValue = "0-0", sensitive = true, type = IntegerPair.class, label = "If the client should use FTP active module for file transfer or passive")
	private static final String FTP_ACTIVE_PORTS = "image.server.activePorts";

	@ConfigurationPropertyDefinition(defaultValue = "50", sensitive = true, type = Integer.class, label = "The number of the maximum active connections to the ftp server.")
	private static final String FTP_MAX_ALLOWED_CONNECTIONS = "image.server.maxAllowedConnections";

	@ConfigurationGroupDefinition(type = FTPConfiguration.class, properties = { FTP_HOST, FTP_PORT, FTP_REMOTE_DIR,
			FTP_USER, FTP_PASS, FTP_SECURE, FTP_ACTIVE_MODE, FTP_ACTIVE_PORTS,
			FTP_MAX_ALLOWED_CONNECTIONS }, label = "The image server configuration.")
	private static final String FTP_CONFIG = "image.server.config";

	@Inject
	@Configuration(FTP_CONFIG)
	private ConfigurationProperty<FTPConfiguration> imageServerConfig;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "image.server.access.address", type = URI.class, sensitive = true, label = "The base address to the apache or other server that can serve the original image content over http")
	private ConfigurationProperty<URI> accessServerAddress;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "image.server.iiif.address", type = URI.class, sensitive = true, label = "The base address to the iiif server")
	private ConfigurationProperty<URI> iiifServerAddress;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "image.server.iiif.address.suffix", type = String.class, sensitive = true, label = "What to append after the name of the image when building iiif server address")
	private ConfigurationProperty<String> iiifServerAddressSuffix;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "image.server.enabled", type = Boolean.class, defaultValue = "false", sensitive = true, label = "Configuration switch to enable/disable image server redirecting.")
	private ConfigurationProperty<Boolean> configurationEnabled;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "image.server.iiif.context", defaultValue = "http://www.shared-canvas.org/ns/context.json", type = URI.class, label = "The address of the context which exposes the iiif scheme.")
	private ConfigurationProperty<URI> iiifContextAddress;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "image.server.async.threshold", type = Long.class, converter = "KB to bytes", defaultValue = "0", label = "Configuration that determines the threshold for the file size that needs asynchronous upload in Kilobytes (KB). Value equal to 0 will disable synchronous upload and all upload will be performed asynchronously (recomended configuration). Value less than 0 means all files will be uploaded synchronously but that may lead to transaction timeout")
	private ConfigurationProperty<Long> asyncUploadThreshold;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "image.server.defaultImage", type = String.class, sensitive = true, label = "The name of the image to be returned when the image is not found because is not already converted")
	private ConfigurationProperty<String> defaultImageName;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "image.server.noContentImage", type = String.class, sensitive = true, label = "The name of the fallback image to be returned when the instance content is not available")
	private ConfigurationProperty<String> noContentImage;

	/**
	 * Returns configuration necessary for the connection to the tfp image server.
	 *
	 * @param converterContext
	 * @return an object with the configuration
	 */
	@SuppressWarnings("boxing")
	@ConfigurationConverter(FTP_CONFIG)
	static FTPConfiguration buildCOnfiguration(GroupConverterContext converterContext) {
		FTPConfiguration ftpConfig = new FTPConfiguration();

		ftpConfig.setHostname(converterContext.get(FTP_HOST));
		ftpConfig.setPort(converterContext.get(FTP_PORT));
		ftpConfig.setUsername(converterContext.get(FTP_USER));
		ftpConfig.setPassword(converterContext.get(FTP_PASS));
		ftpConfig.setRemoteDir(converterContext.get(FTP_REMOTE_DIR));
		ftpConfig.setSecured(converterContext.get(FTP_SECURE));
		ftpConfig.setUseActiveMode(converterContext.get(FTP_ACTIVE_MODE));
		ftpConfig.setActivePortRange(converterContext.get(FTP_ACTIVE_PORTS));
		ftpConfig.setMaxAllowedConnections(converterContext.get(FTP_MAX_ALLOWED_CONNECTIONS));

		return ftpConfig;
	}

	@Override
	public ConfigurationProperty<FTPConfiguration> getImageFTPServerConfig() {
		return imageServerConfig;
	}

	@Override
	public ConfigurationProperty<URI> getIiifServerAddress() {
		return iiifServerAddress;
	}

	@Override
	public ConfigurationProperty<URI> getImageAccessServerAddress() {
		return accessServerAddress;
	}

	@Override
	public ConfigurationProperty<Boolean> isImageServerEnabled() {
		return configurationEnabled;
	}

	@Override
	public ConfigurationProperty<URI> getIiifContextAddress() {
		return iiifContextAddress;
	}

	@Override
	public ConfigurationProperty<Long> getAsyncUploadThreshold() {
		return asyncUploadThreshold;
	}

	@Override
	public ConfigurationProperty<String> getDefaultImageName() {
		return defaultImageName;
	}

	@Override
	public ConfigurationProperty<String> getIiifServerAddressSuffix() {
		return iiifServerAddressSuffix;
	}

	@Override
	public ConfigurationProperty<String> getNoContentImageName() {
		return noContentImage;
	}

}
