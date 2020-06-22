package com.sirma.itt.seip.eai.cms.configuration;

import java.io.File;
import java.net.URI;
import java.util.Iterator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.json.JSONObject;

import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.eai.configuration.EAIConfigurationProvider;
import com.sirma.itt.seip.eai.cs.EAIServicesConstants;
import com.sirma.itt.seip.eai.cs.service.model.ModelConfigurationBuilder;
import com.sirma.itt.seip.eai.exception.EAIModelException;
import com.sirma.itt.seip.eai.service.communication.BaseEAIServices;
import com.sirma.itt.seip.eai.service.communication.CommunicationConfiguration;
import com.sirma.itt.seip.eai.service.communication.RemoteCommunicationConfiguration;
import com.sirma.itt.seip.eai.service.communication.ServiceEndpoint;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;
import com.sirma.itt.seip.eai.service.search.SearchModelConfiguration;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.exception.SecurityException;

/**
 * CMS subsystem configurations as extension of {@link EAIConfigurationProvider}
 * 
 * @author bbanchev
 */
@ApplicationScoped
@Extension(target = EAIConfigurationProvider.NAME, order = 10)
public class CMSIntegrationConfigurationProvider implements EAIConfigurationProvider {

	/** The CMS subsystem id. */
	public static final String SYSTEM_ID = "CMS";
	/** The CMS subsystem namespace. */
	public static final String NAMESPACE = "cultObj";

	@ConfigurationPropertyDefinition(name = "eai.cms.communication.uri", sensitive = true, label = "Base service URI holding the protocol+address+port+context information")
	private static final String EAI_CMS_COMMUNICATION_URI = "eai.cms.communication.uri";
	@ConfigurationPropertyDefinition(name = "eai.cms.communication.services", sensitive = true, label = "Services relative addresses to use for CMS request. Represent a json object in format <code>{\"search\":{\"GET\":{\"uri\":\"service/art/objects.json\", \"timeout\":12000}}</code>.The first key is the method type to a complex values for method configuration")
	private static final String EAI_CMS_COMMUNICATION_SERVICES = "eai.cms.communication.services";
	@ConfigurationPropertyDefinition(name = "eai.cms.communication.security.header", sensitive = true, defaultValue = "Authorization", label = "Name of header field to set API token to")
	private static final String EAI_CMS_COMMUNICATION_SECURITY_TOKEN_HEADER = "eai.cms.communication.security.header";

	@ConfigurationPropertyDefinition(name = "eai.cms.communication.security.token", sensitive = true, label = "Token value of specific to the subsystem type")
	private static final String EAI_CMS_COMMUNICATION_SECURITY_TOKEN_VALUE = "eai.cms.communication.security.token";

	@ConfigurationPropertyDefinition(type = String.class, sensitive = true, name = "eai.cms.config.client", label = "Tenant aware subsystem id.")
	private static final String EAI_CMS_CONFIG_CLIENTID = "eai.cms.config.client";

	@ConfigurationPropertyDefinition(name = "eai.cms.config.model.xlsx.path", sensitive = true, type = File.class, label = "Location of folder containing CMS configurations. Should have subfolders for each tenant registered")
	private static final String EAI_CMS_MODEL_LOCATION_CONFIG = "eai.cms.config.model.xlsx.path";

	@ConfigurationPropertyDefinition(name = "eai.cms.config.model.xlsx.mapping", sensitive = true, label = "Json string representing the xlsx column to data mapping.")
	private static final String EAI_CMS_MODEL_MAPPING_CONFIG = "eai.cms.config.model.xlsx.mapping";

	@ConfigurationPropertyDefinition(name = "eai.cms.communication.security.trust.all.certificates", sensitive = true, type = Boolean.class, defaultValue = "false", label = "Trust all certificates flag. If false the certificates should be CA certified.")
	private static final String EAI_CMS_CONFIG_TRUST_ALL_CERTIFICATES = "eai.cms.communication.security.trust.all.certificates";

	@ConfigurationPropertyDefinition(name = "eai.cms.enabled", sensitive = false, type = Boolean.class, defaultValue = "true", label = "Set as enabled/disabled CMS integration")
	private static final String EAI_CMS_ENABLED = "eai.cms.enabled";

	@ConfigurationGroupDefinition(type = ModelConfiguration.class, properties = { EAI_CMS_MODEL_LOCATION_CONFIG,
			EAI_CMS_MODEL_MAPPING_CONFIG, EAI_CMS_ENABLED })
	private static final String EAI_CMS_MODEL_CONFIG = "eai.cms.config.model";

	@ConfigurationGroupDefinition(type = SearchModelConfiguration.class, properties = { EAI_CMS_MODEL_LOCATION_CONFIG,
			EAI_CMS_MODEL_CONFIG, EAI_CMS_ENABLED })
	private static final String EAI_CMS_SEARCH_MODEL_CONFIG = "eai.cms.config.search.model";

	@ConfigurationGroupDefinition(type = CommunicationConfiguration.class, properties = { EAI_CMS_COMMUNICATION_URI,
			EAI_CMS_COMMUNICATION_SECURITY_TOKEN_HEADER, EAI_CMS_COMMUNICATION_SECURITY_TOKEN_VALUE, EAI_CMS_ENABLED })
	private static final String EAI_CMS_COMMUNICATION_CONFIG = "eai.cms.communication.config";

	@Inject
	@Configuration(EAI_CMS_CONFIG_CLIENTID)
	private ConfigurationProperty<String> cmsClientId;

	@Inject
	@Configuration(EAI_CMS_MODEL_CONFIG)
	private ConfigurationProperty<ModelConfiguration> mappingModel;

	@Inject
	@Configuration(EAI_CMS_SEARCH_MODEL_CONFIG)
	private ConfigurationProperty<SearchModelConfiguration> searchModel;

	@Inject
	@Configuration(EAI_CMS_COMMUNICATION_CONFIG)
	private ConfigurationProperty<CommunicationConfiguration> communicationConfiguration;

	@Inject
	@Configuration(EAI_CMS_CONFIG_TRUST_ALL_CERTIFICATES)
	private ConfigurationProperty<Boolean> trustAllCertificates;

	@Inject
	@Configuration(EAI_CMS_ENABLED)
	private ConfigurationProperty<Boolean> enabled;

	/**
	 * Builds the {@link CommunicationConfiguration}.
	 *
	 * @param context
	 *            the context
	 * @return the {@link CommunicationConfiguration}
	 */
	@ConfigurationConverter(EAI_CMS_COMMUNICATION_CONFIG)
	static CommunicationConfiguration buildCommunicationConfiguration(GroupConverterContext context) {
		if (!isEnabled(context)) {
			return null;
		}
		if (!context.getValue(EAI_CMS_COMMUNICATION_URI).isSet()) {
			throw new ConfigurationException("Missing mandatory configuration: " + EAI_CMS_COMMUNICATION_URI);
		}
		try {
			String uri = context.get(EAI_CMS_COMMUNICATION_URI);

			JSONObject services = JsonUtil.createObjectFromString((String) context.get(EAI_CMS_COMMUNICATION_SERVICES));
			RemoteCommunicationConfiguration communicationConfiguration = new RemoteCommunicationConfiguration(
					URI.create(uri));
			Iterator<String> keys = services.keys();
			while (keys.hasNext()) {
				String serviceName = keys.next();
				BaseEAIServices serviceId = BaseEAIServices.valueOf(serviceName.toUpperCase());
				if (serviceId == null) {
					throw new ConfigurationException("Unrecognized service id: " + serviceName);
				}
				communicationConfiguration.addServiceEndpoint(
						new ServiceEndpoint(serviceId, JsonUtil.toMap(services.getJSONObject(serviceName))));

			}
			
			String token = context.get(EAI_CMS_COMMUNICATION_SECURITY_TOKEN_VALUE);
			String header = context.get(EAI_CMS_COMMUNICATION_SECURITY_TOKEN_HEADER);
			boolean trustAllCertificates = context.get(EAI_CMS_CONFIG_TRUST_ALL_CERTIFICATES);
			communicationConfiguration.setTrustAllCertificates(trustAllCertificates);
			communicationConfiguration.addHttpHeader(header, token);
			return communicationConfiguration;
		} catch (Exception e) {
			throw new ConfigurationException(e);
		}

	}

	@ConfigurationConverter(EAI_CMS_CONFIG_CLIENTID)
	static String buildClientId(ConverterContext context, SecurityContext securityContext) {// NOSONAR
		try {
			String tenantId = securityContext.getCurrentTenantId();
			return SYSTEM_ID + "@" + tenantId;
		} catch (SecurityException e) {
			throw new ConfigurationException(e);
		}
	}

	@ConfigurationConverter(EAI_CMS_MODEL_CONFIG)
	static ModelConfiguration buildModelConfiguration(GroupConverterContext context, SecurityContext securityContext) {
		if (!isEnabled(context)) {
			return null;
		}
		if (context.getValue(EAI_CMS_MODEL_LOCATION_CONFIG).isSet()) {
			try {
				File modelsPath = new File(context.get(EAI_CMS_MODEL_LOCATION_CONFIG).toString(),
						securityContext.getCurrentTenantId());
				JSONObject jsonObject = JsonUtil.createObjectFromString(context.get(EAI_CMS_MODEL_MAPPING_CONFIG));
				ModelConfiguration modelConfiguration = ModelConfigurationBuilder.provideDataModelFromXlsx(modelsPath,
						JsonUtil.toMap(jsonObject));
				modelConfiguration.registerNamespace(NAMESPACE);
				modelConfiguration.seal();
				return modelConfiguration;
			} catch (EAIModelException e) {
				throw new ConfigurationException(e);
			}
		}
		throw new ConfigurationException(EAI_CMS_MODEL_LOCATION_CONFIG
				+ " is not configured. Integration with external systems could not be started!");
	}

	@ConfigurationConverter(EAI_CMS_SEARCH_MODEL_CONFIG)
	static SearchModelConfiguration buildSearchModelConfiguration(GroupConverterContext context,
			SecurityContext securityContext, SemanticDefinitionService semanticDefinitionService) {
		if (!isEnabled(context)) {
			return null;
		}
		if (context.getValue(EAI_CMS_MODEL_LOCATION_CONFIG).isSet()) {
			try {
				ModelConfiguration modelConfiguration = context.get(EAI_CMS_MODEL_CONFIG);
				File modelsPath = new File(context.get(EAI_CMS_MODEL_LOCATION_CONFIG).toString(),
						securityContext.getCurrentTenantId());
				SearchModelConfiguration searchConfiguration = ModelConfigurationBuilder.provideSearchModelFromXlsx(
						modelsPath, modelConfiguration, semanticDefinitionService,
						EAIServicesConstants.TYPE_CULTURAL_OBJECT);
				searchConfiguration.seal();
				return searchConfiguration;
			} catch (EAIModelException e) {
				throw new ConfigurationException(e);
			}
		}
		throw new ConfigurationException(EAI_CMS_MODEL_LOCATION_CONFIG
				+ " is not configured. Integration with external systems could not be started!");
	}

	private static boolean isEnabled(GroupConverterContext context) {
		return context.getValue(EAI_CMS_ENABLED).isSet() && ((Boolean) context.get(EAI_CMS_ENABLED)).booleanValue();
	}

	@Override
	public ConfigurationProperty<ModelConfiguration> getModelConfiguration() {
		return mappingModel;
	}

	@Override
	public ConfigurationProperty<CommunicationConfiguration> getCommunicationConfiguration() {
		return communicationConfiguration;
	}

	@Override
	public ConfigurationProperty<SearchModelConfiguration> getSearchConfiguration() {
		return searchModel;
	}

	@Override
	public String getName() {
		return SYSTEM_ID;
	}

	@Override
	public ConfigurationProperty<String> getSystemClientId() {
		return cmsClientId;
	}

	@Override
	public ConfigurationProperty<Boolean> isEnabled() {
		return enabled;
	}
}
