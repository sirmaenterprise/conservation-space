package com.sirma.itt.seip.adapters;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;

/**
 * @author BBonev
 */
@Singleton
public class Alfresco4Configurations implements AdaptersConfiguration {

	@ConfigurationPropertyDefinition(defaultValue = "localhost", sensitive = true, label = "DMS server host address")
	private static final String DMS_HOST = "dms.host";

	@ConfigurationPropertyDefinition(defaultValue = "8080", sensitive = true, type = Integer.class, label = "DMS server port")
	private static final String DMS_PORT = "dms.port";

	@ConfigurationPropertyDefinition(defaultValue = "http", sensitive = true, label = "DMS server protocol. <b>Default value is: http</b>")
	private static final String DMS_PROTOCOL = "dms.protocol";

	@ConfigurationGroupDefinition(type = URI.class, properties = { DMS_HOST, DMS_PORT,
			DMS_PROTOCOL }, label = "DMS address")
	private static final String DMS_ADDRESS = "dms.address";

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "default.container", sensitive = true, defaultValue = "seip", label = "Defines the default container/tenant ID. "
			+ "The container id is the containing DMS site id, "
			+ "where the emf/cmf files are stored. If the tenant id of the logged user is not "
			+ "provided then this is used for tenant separation.")
	private ConfigurationProperty<String> dmsContainer;

	@Inject
	@Configuration(DMS_ADDRESS)
	private ConfigurationProperty<URI> dmsAddress;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "content.store.alfresco4.enabled", defaultValue = "true", type = Boolean.class, sensitive = true, subSystem = "content", label = "Determines if instance primary content should go to Alfresco or to the Local content store")
	private ConfigurationProperty<Boolean> alfrescoStoreEnabled;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "content.store.alfresco4view.enabled", defaultValue = "true", type = Boolean.class, sensitive = true, subSystem = "content", label = "Determines if instance primary view should go to Alfresco or to the Local content store")
	private ConfigurationProperty<Boolean> alfrescoViewStoreEnabled;

	@ConfigurationConverter(DMS_ADDRESS)
	static URI buildAddess(GroupConverterContext converterContext) {
		converterContext.getValue(DMS_HOST).requireConfigured();

		String protocol = converterContext.get(DMS_PROTOCOL);
		String host = converterContext.get(DMS_HOST);
		Integer port = converterContext.get(DMS_PORT);

		try {
			return new URI(protocol, null, host, port.intValue(), null, null, null);
		} catch (URISyntaxException e) {
			throw new ConfigurationException("Failed to construct DMS address URI", e);
		}
	}

	@Override
	public ConfigurationProperty<String> getDmsContainerId() {
		return dmsContainer;
	}

	@Override
	public ConfigurationProperty<URI> getDmsAddress() {
		return dmsAddress;
	}

	@Override
	public String getDmsProtocolConfiguration() {
		return DMS_PROTOCOL;
	}

	@Override
	public String getDmsHostConfiguration() {
		return DMS_HOST;
	}

	@Override
	public String getDmsPortConfiguration() {
		return DMS_PORT;
	}

	@Override
	public ConfigurationProperty<Boolean> getAlfrescoStoreEnabled() {
		return alfrescoStoreEnabled;
	}

	@Override
	public ConfigurationProperty<Boolean> getAlfrescoViewStoreEnabled() {
		return alfrescoViewStoreEnabled;
	}
}
