package com.srima.itt.seip.adapters.mock;

import java.net.URI;

import com.sirma.itt.seip.adapters.iiif.ImageServerConfigurations;
import com.sirma.itt.seip.adapters.remote.FTPConfiguration;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Mock for the image server configuration.
 *
 * @author Nikolay Ch
 */
public class ImageServerConfigurationsMock implements ImageServerConfigurations {
	private FTPConfiguration ftpConfig;
	private String iiifAddress;
	private String accessAddress;
	private String iiifContext;
	private boolean isEnabled = true;
	private Long asyncThreshold = Long.valueOf(0L);

	/**
	 * Builds the configuration.
	 *
	 * @param host
	 *            the hostname
	 * @param port
	 *            the port on which the server will receive the data
	 * @param username
	 *            the username of the client
	 * @param password
	 *            the password of the client
	 * @param remoteDir
	 *            the remote dir where the data will be stored
	 */
	public void buildConfiguration(String host, int port, String username, String password, String remoteDir) {
		ftpConfig = new FTPConfiguration(host, port, username, password, remoteDir, false);
	}

	/**
	 * Sets the image ftp server configuration to be returned by the method {@link #getImageFTPServerConfig()}
	 *
	 * @param configuration
	 *            the new image ftp server config
	 */
	public void setImageFTPServerConfig(FTPConfiguration configuration) {
		ftpConfig = configuration;
	}

	@Override
	public ConfigurationProperty<FTPConfiguration> getImageFTPServerConfig() {
		return new ConfigurationPropertyMock<>(ftpConfig);
	}

	/**
	 * Sets the iiif server address.
	 *
	 * @param address
	 *            the new iiif server address
	 */
	public void setIiifServerAddress(String address) {
		iiifAddress = address;

	}

	@Override
	public ConfigurationProperty<URI> getIiifServerAddress() {
		return new ConfigurationPropertyMock<>(URI.create(iiifAddress));
	}

	/**
	 * Sets the access address.
	 *
	 * @param accessAddress
	 *            the new access address
	 */
	public void setAccessAddress(String accessAddress) {
		this.accessAddress = accessAddress;
	}

	@Override
	public ConfigurationProperty<URI> getImageAccessServerAddress() {
		return new ConfigurationPropertyMock<>(URI.create(accessAddress));
	}

	@Override
	public ConfigurationProperty<Boolean> isImageServerEnabled() {
		return new ConfigurationPropertyMock<>(Boolean.valueOf(isEnabled));
	}

	/**
	 * Sets the enabled.
	 *
	 * @param isEnabled
	 *            the new enabled
	 */
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	/**
	 * Sets the iiif context address.
	 *
	 * @param iiifContext
	 *            the address to the context
	 */
	public void setContext(String iiifContext) {
		this.iiifContext = iiifContext;
	}

	@Override
	public ConfigurationProperty<URI> getIiifContextAddress() {
		return new ConfigurationPropertyMock<>(URI.create(iiifContext));
	}

	@Override
	public ConfigurationProperty<Long> getAsyncUploadThreshold() {
		return new ConfigurationPropertyMock<>(asyncThreshold);
	}

	/**
	 * Sets the async threshold.
	 *
	 * @param asyncThreshold
	 *            the new async threshold
	 */
	public void setAsyncThreshold(Long asyncThreshold) {
		this.asyncThreshold = asyncThreshold;
	}

	@Override
	public ConfigurationProperty<String> getDefaultImageName() {
		return new ConfigurationPropertyMock<>("defaultImage");
	}

	@Override
	public ConfigurationProperty<String> getIiifServerAddressSuffix() {
		return new ConfigurationPropertyMock<>();
	}

	@Override
	public ConfigurationProperty<Integer> getMaximumWidthForPreview() {
		return new ConfigurationPropertyMock<>(1920);
	}

	@Override
	public ConfigurationProperty<String> getNoContentImageName() {
		return new ConfigurationPropertyMock<>("noContentImage");
	}

}