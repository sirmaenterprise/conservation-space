package com.sirma.itt.seip.eai.service.model;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.eai.configuration.EAIConfigurationProvider;
import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.service.EAIConfigurationService;
import com.sirma.itt.seip.eai.service.model.transform.EAIModelConverter;
import com.sirma.itt.seip.eai.service.search.SearchModelConfiguration;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * ModelService is main service to work with dynamic models used for mapping and conversions. Provides access to
 * {@link ModelConfiguration} and {@link EAIModelConverter}
 * 
 * @author bbanchev
 */
@ApplicationScoped
public class ModelService {

	@Inject
	private EAIConfigurationService integrationService;
	@Inject
	@ExtensionPoint(value = EAIModelConverter.PLUGIN_ID)
	private Plugins<EAIModelConverter> converters;

	/**
	 * Gets the model configuration for current system using the current tenant information.
	 *
	 * @param systemId
	 *            is the system name as returned by one of {@link EAIConfigurationProvider#getName()}
	 * @return the model configuration
	 */
	public ModelConfiguration getModelConfiguration(String systemId) {
		EAIConfigurationProvider integrationConfiguration = integrationService.getIntegrationConfiguration(systemId);
		return integrationConfiguration.getModelConfiguration().get();
	}
	
	
	/**
	 * Gets the search model configuration for current system using the current tenant information.
	 *
	 * @param systemId
	 *            is the system name as returned by one of {@link EAIConfigurationProvider#getName()}
	 * @return the search model configuration
	 */
	public SearchModelConfiguration getSearchConfiguration(String systemId) {
		EAIConfigurationProvider integrationConfiguration = integrationService.getIntegrationConfiguration(systemId);
		return integrationConfiguration.getSearchConfiguration().get();
	}

	/**
	 * Provide a model converter with the given name.
	 *
	 * @param systemId
	 *            is the system name as returned by one of {@link EAIModelConverter#getName()}
	 * @return the model converter or throws {@link EAIRuntimeException} if not found
	 */
	public EAIModelConverter provideModelConverter(String systemId) {
		String converterId = systemId.toUpperCase();
		return converters.get(converterId).orElseThrow(
				() -> new EAIRuntimeException("Missing model converter for: " + converterId));
	}

	/**
	 * Gets the model configuration by namespace. {@link ModelConfiguration} is returned if
	 * {@link ModelConfiguration#hasNamespace(String)} is true
	 *
	 * @param namespace
	 *            the namespace to search
	 * @return the model configuration by namespace
	 */
	public ModelConfiguration getModelConfigurationByNamespace(String namespace) {
		Set<String> registeredSystems = integrationService.getAllRegisteredSystems();
		for (String systemId : registeredSystems) {
			ModelConfiguration modelConfiguration = integrationService
					.getIntegrationConfiguration(systemId)
						.getModelConfiguration()
						.get();
			if (modelConfiguration.hasNamespace(namespace)) {
				return modelConfiguration;
			}
		}
		throw new EAIRuntimeException("Missing model configuration for namespace " + namespace);
	}

	/**
	 * Retrieves the model converter when provided a namespace.
	 * 
	 * @param namespace
	 *            the namespace
	 * @return the model converter for the given namespace.
	 */
	public EAIModelConverter provideModelConverterByNamespace(String namespace) {
		Set<String> registeredSystems = integrationService.getAllRegisteredSystems();
		for (String systemId : registeredSystems) {
			ModelConfiguration modelConfiguration = integrationService
					.getIntegrationConfiguration(systemId)
						.getModelConfiguration()
						.get();
			if (modelConfiguration.hasNamespace(namespace)) {
				return provideModelConverter(systemId);
			}
		}
		throw new EAIRuntimeException("Missing model converter for namespace: " + namespace);
	}
}
