package com.sirma.itt.seip.eai.service;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.eai.configuration.EAIConfigurationProvider;
import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * The {@link EAIConfigurationService} provides a knowledge about system integration by proxing information from
 * specific subsystem. The information is provided using {@link EAIConfigurationProvider} extension.
 *
 * @author bbanchev
 */
@ApplicationScoped
public class EAIConfigurationService {
	@Inject
	@ExtensionPoint(EAIConfigurationProvider.NAME)
	private Plugins<EAIConfigurationProvider> configurationProviders;

	/**
	 * Gets all the registered systems (user and backend services)
	 *
	 * @return the registered systems
	 */
	public Set<String> getAllRegisteredSystems() {
		return getRegisteredSystems(all -> true);
	}

	/**
	 * Gets the registered systems using specific filter
	 * 
	 * @param filter
	 *            is the filter parameters
	 * @return the registered systems
	 */
	public Set<String> getRegisteredSystems(Predicate<EAIConfigurationProvider> filter) {
		return configurationProviders
				.stream()
					.filter(e -> e.isEnabled().get())
					.filter(filter)
					.map(EAIConfigurationProvider::getName)
					.map(String::toUpperCase)
					.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * Check if {@link EAIConfigurationProvider} is registered for specified system id.
	 *
	 * @param systemId
	 *            is the system id as returned by {@link EAIConfigurationProvider#getName()}
	 * @return true if system exists, false otherwise
	 */
	public boolean hasRegisteredSystem(String systemId) {
		if (systemId == null) {
			return false;
		}
		return findIntegrationConfiguration(systemId).isPresent();
	}

	/**
	 * Finds and returns any subsystem configuration provider from the list of extension
	 * {@link EAIConfigurationProvider} enabled or not.
	 *
	 * @param systemId
	 *            is the system id as returned by {@link EAIConfigurationProvider#getName()}. If null,
	 *            {@link Optional#empty()} is returned
	 * @return the provider for that service or {@link Optional#empty()} if not found
	 */
	public Optional<EAIConfigurationProvider> resolveIntegrationConfiguration(String systemId) {
		if (systemId == null) {
			return Optional.empty();
		}
		return configurationProviders
				.stream()
				.filter(config -> EqualsHelper.nullSafeEquals(config.getName(), systemId, true))
				.findAny();
	}

	/**
	 * Return the subsystem configuration provider from the list of extension {@link EAIConfigurationProvider} that are
	 * enabled.
	 *
	 * @param systemId
	 *            is the system id as returned by {@link EAIConfigurationProvider#getName()}. If null,
	 *            {@link Optional#empty()} is returned
	 * @return the provider for that service or {@link Optional#empty()} if not found
	 */
	public Optional<EAIConfigurationProvider> findIntegrationConfiguration(String systemId) {
		return resolveIntegrationConfiguration(systemId).filter(config -> config.isEnabled().get());
	}

	/**
	 * Return the subsystem using {@link #findIntegrationConfiguration(String)} and throws {@link EAIRuntimeException}
	 * if not found
	 * 
	 * @param systemId
	 *            is the system id as returned by {@link EAIConfigurationProvider#getName()}
	 * @return the provider for that service or throws {@link EAIRuntimeException} on missing
	 */
	public EAIConfigurationProvider getIntegrationConfiguration(final String systemId) {
		return findIntegrationConfiguration(systemId)
				.orElseThrow(() -> new EAIRuntimeException("Missing configuration factory for " + systemId));
	}
}
