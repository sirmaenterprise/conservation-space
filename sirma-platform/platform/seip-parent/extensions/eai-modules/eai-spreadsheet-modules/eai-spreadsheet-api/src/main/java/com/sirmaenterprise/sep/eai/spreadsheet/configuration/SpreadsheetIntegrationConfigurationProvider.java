package com.sirmaenterprise.sep.eai.spreadsheet.configuration;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.definition.event.DefinitionsChangedEvent;
import com.sirma.itt.seip.definition.event.LoadSemanticDefinitions;
import com.sirma.itt.seip.domain.codelist.event.ResetCodelistEvent;
import com.sirma.itt.seip.eai.configuration.EAIConfigurationProvider;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.service.communication.CommunicationConfiguration;
import com.sirma.itt.seip.eai.service.communication.LocalContentCommunicationConfiguration;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;
import com.sirma.itt.seip.eai.service.search.SearchModelConfiguration;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.annotation.SecureObserver;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirmaenterprise.sep.eai.spreadsheet.service.model.SpreadsheetEAIModelConfigurationProvider;

/**
 * Spreadsheet subsystem configurations as extension of {@link EAIConfigurationProvider}. Configurations are updated on
 * related event observed are and always up to date
 *
 * @author bbanchev
 */
@Singleton
@Extension(target = EAIConfigurationProvider.NAME, order = 5)
public class SpreadsheetIntegrationConfigurationProvider implements EAIConfigurationProvider {

	/** The spreadsheet subsystem id. */
	public static final String SYSTEM_ID = "SHEET";

	@ConfigurationPropertyDefinition(type = String.class, sensitive = true, name = "eai.spreadsheet.config.client", label = "Tenant aware subsystem id.")
	private static final String EAI_SPREADSHEET_CONFIG_CLIENTID = "eai.spreadsheet.config.client";

	@ConfigurationPropertyDefinition(name = "eai.spreadsheet.enabled", sensitive = false, type = Boolean.class, defaultValue = "true", label = "Set as enabled/disabled CMS integration")
	private static final String EAI_SPREADSHEET_ENABLED = "eai.spreadsheet.enabled";

	@ConfigurationGroupDefinition(type = ModelConfiguration.class, properties = { EAI_SPREADSHEET_ENABLED })
	private static final String EAI_SPREADSHEET_MODEL_CONFIG = "eai.spreadsheet.config.model";

	@ConfigurationGroupDefinition(type = SearchModelConfiguration.class, properties = { EAI_SPREADSHEET_ENABLED })
	private static final String EAI_SPREADSHEET_SEARCH_MODEL_CONFIG = "eai.spreadsheet.config.search.model";

	@ConfigurationGroupDefinition(type = CommunicationConfiguration.class, properties = { EAI_SPREADSHEET_ENABLED })
	private static final String EAI_SPREADSHEET_COMMUNICATION_CONFIG = "eai.spreadsheet.communication.config";

	@Inject
	@Configuration(EAI_SPREADSHEET_CONFIG_CLIENTID)
	private ConfigurationProperty<String> clientId;

	@Inject
	@Configuration(EAI_SPREADSHEET_MODEL_CONFIG)
	private ConfigurationProperty<ModelConfiguration> mappingModel;
	@Inject
	@Configuration(EAI_SPREADSHEET_SEARCH_MODEL_CONFIG)
	private ConfigurationProperty<SearchModelConfiguration> searchModel;

	@Inject
	@Configuration(EAI_SPREADSHEET_COMMUNICATION_CONFIG)
	private ConfigurationProperty<CommunicationConfiguration> communicationConfiguration;

	@Inject
	@Configuration(EAI_SPREADSHEET_ENABLED)
	private ConfigurationProperty<Boolean> enabled;

	/**
	 * Builds the {@link LocalContentCommunicationConfiguration}.
	 *
	 * @param context
	 *            the context
	 * @return the {@link CommunicationConfiguration}
	 */
	@ConfigurationConverter(EAI_SPREADSHEET_COMMUNICATION_CONFIG)
	static CommunicationConfiguration buildCommunicationConfiguration(GroupConverterContext context) {
		if (!isEnabled(context)) {
			return null;
		}
		return new LocalContentCommunicationConfiguration();
	}

	private static boolean isEnabled(GroupConverterContext context) {
		return context.getValue(EAI_SPREADSHEET_ENABLED).isSet()
				&& ((Boolean) context.get(EAI_SPREADSHEET_ENABLED)).booleanValue();
	}

	/**
	 * Builds the client id.
	 *
	 * @param context
	 *            the context
	 * @param securityContext
	 *            the security context
	 * @return the string
	 */
	@ConfigurationConverter(EAI_SPREADSHEET_CONFIG_CLIENTID)
	static String buildClientId(@SuppressWarnings("unused") ConverterContext context, SecurityContext securityContext) {// NONSONAR
		String tenantId = securityContext.getCurrentTenantId();
		return SYSTEM_ID + "@" + tenantId;
	}

	/**
	 * Builds the model configuration using {@link SpreadsheetEAIModelConfigurationProvider}
	 *
	 * @param context
	 *            the context
	 * @param builder
	 *            the specific {@link SpreadsheetEAIModelConfigurationProvider}
	 * @return the model configuration
	 * @throws EAIException
	 *             on any error
	 */
	@ConfigurationConverter(EAI_SPREADSHEET_MODEL_CONFIG)
	static ModelConfiguration buildModelConfiguration(GroupConverterContext context,
			SpreadsheetEAIModelConfigurationProvider builder) throws EAIException {
		if (!isEnabled(context)) {
			return null;
		}
		return builder.provideModel();
	}

	@ConfigurationConverter(EAI_SPREADSHEET_SEARCH_MODEL_CONFIG)
	static SearchModelConfiguration buildSearchModelConfiguration(GroupConverterContext context) {
		if (!isEnabled(context)) {
			return null;
		}
		SearchModelConfiguration searchModelConfiguration = new SearchModelConfiguration();
		searchModelConfiguration.seal();
		return searchModelConfiguration;
	}

	@Override
	public ConfigurationProperty<ModelConfiguration> getModelConfiguration() {
		return mappingModel;
	}

	@Override
	public ConfigurationProperty<SearchModelConfiguration> getSearchConfiguration() {
		return searchModel;
	}

	@Override
	public ConfigurationProperty<CommunicationConfiguration> getCommunicationConfiguration() {
		return communicationConfiguration;
	}

	@Override
	public String getName() {
		return SYSTEM_ID;
	}

	@Override
	public ConfigurationProperty<String> getSystemClientId() {
		return clientId;
	}

	@Override
	public ConfigurationProperty<Boolean> isEnabled() {
		return enabled;
	}

	@Override
	public Boolean isUserService() {
		// is is backend service, not visible in UI
		return Boolean.FALSE;
	}

	/**
	 * Update on codelist change - reset model configuration.
	 *
	 * @param event
	 *            the event observed
	 */
	@SecureObserver
	public void updateOnCodelistChange(@SuppressWarnings("unused") @Observes ResetCodelistEvent event) {
		getModelConfiguration().valueUpdated();
	}

	/**
	 * Update on semantic definition change - reset model configuration.
	 *
	 * @param event
	 *            the event observed
	 */
	@SecureObserver
	public void updateOnSemanticDefinitionChange(@SuppressWarnings("unused") @Observes LoadSemanticDefinitions event) {
		getModelConfiguration().valueUpdated();
	}

	/**
	 * Update on definition change - reset model configuration.
	 *
	 * @param event
	 *            the event observed
	 */
	@SecureObserver
	public void updateOnDefinitionChange(@SuppressWarnings("unused") @Observes DefinitionsChangedEvent event) {
		getModelConfiguration().valueUpdated();
	}
}
