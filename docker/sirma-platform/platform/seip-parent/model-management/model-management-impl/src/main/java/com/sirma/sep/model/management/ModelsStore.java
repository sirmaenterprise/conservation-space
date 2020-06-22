package com.sirma.sep.model.management;

import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.sep.model.management.exception.ChangeSetCollisionException;
import com.sirma.sep.model.management.operation.ChangeSetValidationFailed;
import com.sirma.sep.model.management.operation.ModelChangeSetInfo;

/**
 * Storage that builds, stores and manages contextual {@link Models} instances representing the current runtime semantic and definition
 * models in the available system contexts along with a copy with any non deployed {@link ModelChangeSetInfo}.
 *
 * @author Mihail Radkov
 * @see ModelBuilder
 */
@Singleton
public class ModelsStore {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private ModelBuilder modelBuilder;

	@Inject
	private Contextual<Models> modelsContext;

	@Inject
	private Contextual<Models> runtimeModelsContext;

	@Inject
	private ModelPersistence modelPersistence;

	@Inject
	private ModelChangeSetOperationManager operationManager;

	@PostConstruct
	void initialize() {
		// modelsContext should initialize runtimeModelsContext
		modelsContext.initializeWith(this::calculateModels);
		runtimeModelsContext.initializeWith(this::calculateRuntimeModels);
	}

	/**
	 * Clears the {@link Models} for the current system context if they were calculated.
	 */
	public void clear() {
		if (modelsContext.isSet()) {
			runtimeModelsContext.clearContextValue();
			modelsContext.clearContextValue();
		} else {
			LOGGER.debug("Requested clear for models context but there is nothing to clear");
		}
	}

	/**
	 * Returns the {@link Models} for the current system context. They will be calculated if not yet.
	 *
	 * @return the current context's {@link Models} instance
	 */
	Models getModels() {
		return modelsContext.getContextValue();
	}

	/**
	 * Returns a copy of the {@link Models} for the current system context. They will be calculated if not yet.
	 *
	 * @return copy of the current context's {@link Models} instance
	 */
	public Models getModelsCopy() {
		// TODO add model locking during cloning
		return copy(modelsContext.getContextValue());
	}

	/**
	 * Returns the runtime {@link Models} for the current context. Runtime models are those which the application is currently using.
	 *
	 * @return the runtime {@link Models} for the current context
	 */
	public Models getRuntimeModelsCopy() {
		return copy(runtimeModelsContext.getContextValue());
	}

	private Models copy(Models toCopy) {
		return modelBuilder.copyModels(toCopy);
	}

	private Models calculateRuntimeModels() {
		TimeTracker timeTracker = TimeTracker.createAndStart();
		try {
			return modelBuilder.buildModels();
		} finally {
			LOGGER.debug("Runtime models calculation took {} ms", timeTracker.stop());
		}
	}

	private Models calculateModels() {
		Models models = getRuntimeModelsCopy();

		TimeTracker timeTracker = TimeTracker.createAndStart();

		applyNonDeployedChanges(models);
		setModelVersion(models);

		LOGGER.debug("Models calculation with non deployed changes took {} ms", timeTracker.stop());
		return models;
	}

	private void setModelVersion(Models models) {
		models.setVersion(modelPersistence.getModelVersion());
	}

	private void applyNonDeployedChanges(Models models) {
		List<ModelChangeSetInfo> changesSince = modelPersistence.getChangesSince(models.getVersion());
		LOGGER.debug("Found {} not deployed changes to the main model", changesSince.size());
		operationManager.execute(models, changesSince, (model, change) -> {
			if (!change.isIntermediate()) {
				model.modelUpdated();
			}
		}, this::markChangeAsFailed);
		LOGGER.debug("Main model version set to {} after processing {} changes", models.getVersion(),
				changesSince.size());
	}

	private boolean markChangeAsFailed(ChangeSetValidationFailed validationException, ModelChangeSetInfo change) {
		if (validationException instanceof ChangeSetCollisionException) {
			String changePath = change.getChangeSet().getPath().prettyPrint();
			LOGGER.warn("Detected data collision for path={}. Change will be applied ignoring the error {}", changePath,
					validationException.getMessage());
			change.setStatusMessage(validationException.getMessage());
			modelPersistence.updateChange(change);
			return true;
		}
		if (change.isIntermediate()) {
			String changePath = change.getChangeSet().getPath().prettyPrint();
			LOGGER.warn("Fail to apply temporary change path={} to runtime model due to {}. Change skipped", changePath,
					validationException.getMessage());
			return false;
		}

		LOGGER.warn("Fail to apply change id={} to runtime model due to {}. Marked the change as failed",
				change.getIndex(), validationException.getMessage());
		change.markAsFailedToApply(validationException.getMessage());
		modelPersistence.updateChange(change);
		return false;
	}
}
