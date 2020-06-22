package com.sirma.sep.model.management;

import java.lang.invoke.MethodHandles;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.event.SemanticDefinitionsReloaded;
import com.sirma.itt.seip.domain.codelist.event.ResetCodelistEvent;
import com.sirma.sep.model.ModelImportCompleted;

/**
 * Observer for system events external to the model management.
 * <p>
 * When observed, the current context's {@link Models} in the {@link ModelsStore} will be cleared via {@link ModelsStore#clear()}.
 *
 * @author Mihail Radkov
 * @see ModelsStore
 */
class ModelsResetObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ModelsStore modelsStore;

	@Inject
	ModelsResetObserver(ModelsStore modelsStore) {
		this.modelsStore = modelsStore;
	}

	/**
	 * Clears the models on {@link ModelImportCompleted}.
	 *
	 * @param modelImportCompleted event notifying that the model import have completed
	 */
	void onModelsImport(@Observes ModelImportCompleted modelImportCompleted) {
		LOGGER.debug("Triggered models clear after model import");
		modelsStore.clear();
	}

	/**
	 * Clears the models on {@link ResetCodelistEvent}.
	 *
	 * @param resetCodelistEvent event notifying that the code lists have been reloaded
	 */
	void onCodeListsReload(@Observes ResetCodelistEvent resetCodelistEvent) {
		LOGGER.debug("Triggered models clear after code lists reset");
		modelsStore.clear();
	}

	/**
	 * Clears the models on {@link SemanticDefinitionsReloaded}.
	 *
	 * @param semanticDefinitionsReloaded event notifying that the semantic definition cache have been reloaded
	 */
	void onSemanticDefinitionCacheReload(@Observes SemanticDefinitionsReloaded semanticDefinitionsReloaded) {
		LOGGER.debug("Triggered models clear after semantic cache reset");
		modelsStore.clear();
	}
}
