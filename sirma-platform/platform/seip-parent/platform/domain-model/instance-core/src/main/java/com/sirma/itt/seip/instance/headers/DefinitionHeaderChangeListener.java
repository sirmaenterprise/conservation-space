package com.sirma.itt.seip.instance.headers;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.event.DefinitionsChangedEvent;
import com.sirma.itt.seip.definition.label.LabelService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.tasks.TransactionMode;

/**
 * Listener for definition changes to trigger header registration in {@link InstanceHeaderService}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 24/11/2017
 */
@Singleton
@Startup(transactionMode = TransactionMode.NOT_SUPPORTED)
class DefinitionHeaderChangeListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private InstanceHeaderService instanceHeaderService;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private LabelService labelService;

	@Inject
	private SystemConfiguration systemConfiguration;

	@Inject
	private TaskExecutor taskExecutor;

	@PostConstruct
	void initialize() {
		// on system language change trigger reindexing if needed
		systemConfiguration.getSystemLanguageConfiguration()
				.addConfigurationChangeListener(language -> taskExecutor.executeAsyncInTx(this::checkForHeaderChanges));
	}

	/**
	 * Listens for definition update and register their headers in the headers service.
	 *
	 * @param event the definition trigger event
	 */
	@Transactional
	void onDefinitionsUpdated(@Observes DefinitionsChangedEvent event) {
		checkForHeaderChanges();
	}

	private void checkForHeaderChanges() {
		LOGGER.info("Triggered definitions check if instance headers are changed");
		definitionService.getAllDefinitions().forEach(this::registerHeader);
	}

	private void registerHeader(DefinitionModel model) {
		// fetch the english label for the given header
		String systemLanguage = systemConfiguration.getSystemLanguage();
		String definitionId = model.getIdentifier();
		// allow clients to define special static label if not fallback to the breadcrumb
		Optional<PropertyDefinition> headerDefinition = model.getField(DefaultProperties.HEADER_LABEL)
				.filter(propertyDefinition -> StringUtils.isNotBlank(propertyDefinition.getLabelId()));
		if (!headerDefinition.isPresent()) {
			headerDefinition = model.getField(DefaultProperties.HEADER_BREADCRUMB);
		}
		headerDefinition
				.map(PropertyDefinition::getLabelId)
				.map(labelService::getLabel)
				.map(labelDefinition -> labelDefinition.getLabels().get(systemLanguage))
				.filter(StringUtils::isNotBlank)
				.ifPresent(label -> instanceHeaderService.trackHeader(definitionId, label));
	}
}
