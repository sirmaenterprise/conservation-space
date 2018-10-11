package com.sirma.itt.seip.adapters.iiif;

import java.lang.invoke.MethodHandles;
import java.net.URI;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@ConfigurationPropertyDefinition(sensitive = true, label = "FTP server host address")
	private static final String FTP_HOST = "image.server.host";

	@ConfigurationPropertyDefinition(defaultValue = "21", sensitive = true, type = Integer.class,
			label = "FTP server port")
	private static final String FTP_PORT = "image.server.port";

	@ConfigurationPropertyDefinition(defaultValue = "/", sensitive = true,
			label = "Remote directory to store the files")
	private static final String FTP_REMOTE_DIR = "image.server.remote.dir";

	@ConfigurationPropertyDefinition(sensitive = true, label = "The client's name")
	private static final String FTP_USER = "image.server.username";

	@ConfigurationPropertyDefinition(sensitive = true, password = true, label = "The client's pass")
	private static final String FTP_PASS = "image.server.password";

	@ConfigurationPropertyDefinition(defaultValue = "false", sensitive = true, type = Boolean.class,
			label = "Secure connection necessarity")
	private static final String FTP_SECURE = "image.server.secure";

	@ConfigurationPropertyDefinition(defaultValue = "false", sensitive = true, type = Boolean.class,
			label = "If the client should use FTP active module for file transfer or passive")
	private static final String FTP_ACTIVE_MODE = "image.server.ftpActiveMode";

	@ConfigurationPropertyDefinition(defaultValue = "0-0", sensitive = true, type = IntegerPair.class,
			label = "If the client should use FTP active module for file transfer or passive")
	private static final String FTP_ACTIVE_PORTS = "image.server.activePorts";

	@ConfigurationPropertyDefinition(defaultValue = "50", sensitive = true, type = Integer.class,
			label = "The number of the maximum active connections to the ftp server.")
	private static final String FTP_MAX_ALLOWED_CONNECTIONS = "image.server.maxAllowedConnections";

	@ConfigurationPropertyDefinition(defaultValue = "false", sensitive = true, type = Boolean.class, label = "Enable or disable verification that the remote host taking part of a data connection is the same as the host to which the control connection is attached.")
	private static final String FTP_REMOTE_VERIFICATION_ENABLED = "image.server.ftp.remoteVerificationEnabled";

	@ConfigurationPropertyDefinition(defaultValue = "1048576", sensitive = true, type = Integer.class, label = "Define a buffer size in bytes to use when transferring files to FTP server. Too low values (<100K) will cause slow file transfers and too high values (>20MB) may cause memory issues")
	private static final String FTP_BUFFER_SIZE = "image.server.ftp.bufferSize";

	@ConfigurationGroupDefinition(type = FTPConfiguration.class,
			properties = { FTP_HOST, FTP_PORT, FTP_REMOTE_DIR, FTP_USER, FTP_PASS, FTP_SECURE, FTP_ACTIVE_MODE,
					FTP_ACTIVE_PORTS, FTP_MAX_ALLOWED_CONNECTIONS, FTP_REMOTE_VERIFICATION_ENABLED, FTP_BUFFER_SIZE }, label = "The image server configuration.")
	private static final String FTP_CONFIG = "image.server.config";

	@Inject
	@Configuration(FTP_CONFIG)
	private ConfigurationProperty<FTPConfiguration> imageServerConfig;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "image.server.access.address", type = URI.class, sensitive = true,
			label = "The base address to the apache or other server that can serve the original image content over http")
	private ConfigurationProperty<URI> accessServerAddress;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "image.server.iiif.address", type = URI.class, sensitive = true,
			label = "The base address to the iiif server")
	private ConfigurationProperty<URI> iiifServerAddress;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "image.server.iiif.address.suffix", type = String.class, sensitive = true,
			label = "What to append after the name of the image when building iiif server address")
	private ConfigurationProperty<String> iiifServerAddressSuffix;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "image.server.enabled", type = Boolean.class, defaultValue = "false",
			sensitive = true, label = "Configuration switch to enable/disable image server redirecting.")
	private ConfigurationProperty<Boolean> configurationEnabled;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "image.server.iiif.context",
			defaultValue = "http://www.shared-canvas.org/ns/context.json", type = URI.class,
			label = "The address of the context which exposes the iiif scheme.")
	private ConfigurationProperty<URI> iiifContextAddress;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "image.server.async.threshold", type = Long.class,
			converter = "KB to bytes", defaultValue = "0",
			label = "Configuration that determines the threshold for the file size that needs asynchronous upload in Kilobytes (KB). Value equal to 0 will disable synchronous upload and all upload will be performed asynchronously (recomended configuration). Value less than 0 means all files will be uploaded synchronously but that may lead to transaction timeout")
	private ConfigurationProperty<Long> asyncUploadThreshold;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "image.server.defaultImage", type = String.class, sensitive = true,
			label = "The name of the image to be returned when the image is not found because is not already converted")
	private ConfigurationProperty<String> defaultImageName;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "image.server.noContentImage", type = String.class, sensitive = true,
			defaultValue = "nocontent",
			label = "The name of the fallback image to be returned when the instance content is not available")
	private ConfigurationProperty<String> noContentImage;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "image.preview.maxWidth", type = Integer.class, defaultValue = "1920",
			label = "The maximum width of image returned from the preview service for image instances")
	private ConfigurationProperty<Integer> previewMaxWidth;

	/**
	 * Returns configuration necessary for the connection to the tfp image server.
	 *
	 * @param converterContext
	 * @return an object with the configuration
	 */
	@SuppressWarnings("boxing")
	@ConfigurationConverter(FTP_CONFIG)
	static FTPConfiguration buildConfiguration(GroupConverterContext converterContext) {
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
		ftpConfig.setRemoteVerificationEnabled(converterContext.get(FTP_REMOTE_VERIFICATION_ENABLED));
		Integer bufferSize = converterContext.get(FTP_BUFFER_SIZE);
		// 100K < buffer size < 20MB
		ftpConfig.setBufferSize(Math.min(1024 * 1024 * 20, Math.max(1024 * 100, bufferSize)));

		if (ftpConfig.isUseActiveMode() && ftpConfig.getActiveModePorts().getFirst() == 0
				&& ftpConfig.getActiveModePorts().getSecond() == 0) {
			ftpConfig.setUseActiveMode(false);
			LOGGER.warn("Trying to use FTP active mode but FTP active ports are not set. Falling back to passive mode!");
		}
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
	public ConfigurationProperty<Integer> getMaximumWidthForPreview() {
		return previewMaxWidth;
	}

	@Override
	public ConfigurationProperty<String> getNoContentImageName() {
		return noContentImage;
	}

}
