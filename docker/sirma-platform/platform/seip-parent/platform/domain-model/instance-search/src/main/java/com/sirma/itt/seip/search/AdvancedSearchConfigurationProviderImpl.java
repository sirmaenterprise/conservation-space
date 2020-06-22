package com.sirma.itt.seip.search;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.event.DefinitionsChangedEvent;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.search.facet.DateRangeConfigTransformer;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.time.DateRangeConfig;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Provides a configuration object {@link AdvancedSearchConfiguration} based on the configured definition of type
 * <b>advancedSearchConfig</b>.
 *
 * @author Mihail Radkov
 */
@ApplicationScoped
public class AdvancedSearchConfigurationProviderImpl implements AdvancedSearchConfigurationProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	// ObjectType.ADVANCED_SEARCH_CONFIG.name() - this should be the value of the enum
	static final String DEFINTION_TYPE = "ADVANCED_SEARCH_CONFIG";

	@Inject
	private DefinitionService definitionService;

	@Inject
	private DateRangeConfigTransformer transformer;

	/** Current configuration. */
	@Inject
	private Contextual<AdvancedSearchConfiguration> configuration;

	/**
	 * Reloads the configurations once the bean has been constructed.
	 */
	@RunAsAllTenantAdmins
	@Startup(async = true)
	public void init() {
		reset();
	}

	@Override
	public AdvancedSearchConfiguration getConfiguration() {
		return configuration.getContextValue();
	}

	@Override
	public void reset() {
		TimeTracker timeTracker = TimeTracker.createAndStart();
		GenericDefinition definition = getConfigurationDefinition();
		if (definition != null) {
			configuration.replaceContextValue(extractConfiguration(definition));
			LOGGER.debug("Advanced search configuration reloaded in {} seconds.", timeTracker.stopInSeconds());
		} else {
			configuration.clearContextValue();
			LOGGER.warn("No valid advanced search configuration could be loaded!");
		}
	}

	/**
	 * Observes the event fired after definition loading to trigger property reloading. This method is invoked
	 * <b>ONLY</b> if the service has been initialized to avoid double reloading.
	 *
	 * @param event
	 *            - the reload event
	 */
	public void onDefinitionReload(@Observes(notifyObserver = Reception.IF_EXISTS) DefinitionsChangedEvent event) {
		reset();
	}

	/**
	 * Gets the declared fields for date ranges in the configuration definition and extracts the date configurations
	 * from it.
	 *
	 * @param definition
	 *            - the configuration definition
	 * @return the search configuration object
	 */
	private AdvancedSearchConfiguration extractConfiguration(GenericDefinition definition) {
		AdvancedSearchConfiguration localConfiguration = new AdvancedSearchConfiguration();

		List<PropertyDefinition> rangeFields = definition.getFields();
		if (CollectionUtils.isEmpty(rangeFields)) {
			LOGGER.warn("The advanced search configuration definition is empty.");
			return localConfiguration;
		}

		List<DateRangeConfig> dateRanges = transformer.extractDateRanges(rangeFields);
		Collections.sort(dateRanges);
		localConfiguration.setDateRanges(dateRanges);
		return localConfiguration;
	}

	/**
	 * Finds the configuration definition in the currently loaded definitions.
	 *
	 * @return the found definition or null if it was not found
	 */
	private GenericDefinition getConfigurationDefinition() {
		List<GenericDefinition> genericDefinitions = definitionService.getAllDefinitions(GenericDefinition.class);
		for (GenericDefinition genericDefinition : genericDefinitions) {
			if (DEFINTION_TYPE.equalsIgnoreCase(genericDefinition.getType())) {
				return genericDefinition;
			}
		}
		return null;
	}
}
