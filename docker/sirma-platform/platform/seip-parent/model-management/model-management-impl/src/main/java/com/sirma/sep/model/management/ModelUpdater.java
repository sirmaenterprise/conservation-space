package com.sirma.sep.model.management;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.sep.model.management.operation.ModelChangeSet;
import com.sirma.sep.model.management.operation.ModelChangeSetInfo;

/**
 * Service responsible for processing update model requests.
 * <p>
 * Update requests are processed in 2 steps.<br>
 * The first step is dry run and does not actually update the real mode. In it the changes are applied to a copy of the
 * model validated and persisted in transactional manner. Requests for changes to the model could happen in parallel<br>
 * The second step is the actual update of the model that should happen one at a time.
 * </p>
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 31/07/2018
 */
@ApplicationScoped
public class ModelUpdater {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final long DEFAULT_TIMEOUT = 1L;
	@Inject
	private ModelChangeSetOperationManager operationManager;
	@Inject
	private ModelPersistence modelPersistence;

	/**
	 * Perform dry run model update for the given changes. If model is successfully updated then the method will wait
	 * for the actual changes to happen and will return the updated model nodes.
	 *
	 * @param models the model instance to perform the dry run
	 * @param modelChanges the model changes to process
	 * @return true if the model was updated at least once and false if no change was applied at all
	 * @throws StaleModelException if the user model state is not compatible with the current model state. The user
	 * should fetch it's model again fix his/her collisions and try again.
	 * @throws com.sirma.sep.model.management.operation.ChangeSetValidationFailed if any of the changes fails its validation
	 */
	boolean dryRunUpdate(Models models, ModelChanges modelChanges) {
		LOGGER.info("Received model update request containing {} changes for model with version {}",
				modelChanges.getChanges().size(), modelChanges.getModelVersion());

		verifyModelVersion(models.getVersion(), modelChanges.getModelVersion());
		validateChanges(models, modelChanges.getChanges());
		List<ModelChangeSetInfo> appliedChanges = new ArrayList<>(modelChanges.getChanges().size());
		applyChanges(models, modelChanges.getChanges(), (model, change) -> {
			if (!change.isIntermediate()) {
				appliedChanges.add(change);
				model.modelUpdated();
			}
		});
		modelChanges.setChanges(appliedChanges);
		if (!modelChanges.isEmpty()) {
			validateModel(models);
			String changeRequestId = persistChanges(modelChanges);
			waitForModelChangesFrom(changeRequestId);
			return true;
		}
		return false;
	}

	private void validateChanges(Models models, List<ModelChangeSetInfo> changes) {
		List<Path> invalidNodePaths = changes.stream()
				.map(ModelChangeSetInfo::getChangeSet)
				.filter(isChangeForEdit())
				.map(ModelChangeSet::getPath)
				.filter(isNotSupportedAttribute(models).or(isNonEditableAttribute(models)))
				.collect(Collectors.toList());

		if (!invalidNodePaths.isEmpty()) {
			throw new ModelValidationException("Cannot edit not supported or non editable attributes", invalidNodePaths);
		}
	}

	private Predicate<? super ModelChangeSet> isChangeForEdit() {
		return change -> change.getOldValue() != null && change.getNewValue() != null;
	}

	private Predicate<Path> isNotSupportedAttribute(Models models) {
		return changePath -> {
			Object item = changePath.walk(models);
			// Missing meta information means the attribute is not supported
			return item == null || (item instanceof ModelAttribute && ((ModelAttribute) item).getMetaInfo() == null);
		};
	}

	private Predicate<Path> isNonEditableAttribute(Models models) {
		return changePath -> {
			Object item = changePath.walk(models);
			if (item instanceof ModelAttribute) {
				ModelAttribute attribute = (ModelAttribute) item;
				return !attribute.isEmpty() && attribute.getContext().isDeployed()
					&& !attribute.getMetaInfo().getValidationModel().isUpdateable();
			}
			return true;
		};
	}

	private void verifyModelVersion(long currentVersion, Long lastKnownVersion) {
		// TODO improve model verification
		if (currentVersion > lastKnownVersion) {
			// this will force single user edit
			// will update it to support parallel edit when we see how it works
			LOGGER.warn("Expected model version {}, but got {}", currentVersion, lastKnownVersion);
		}
	}

	private void applyChanges(Models models, List<ModelChangeSetInfo> changes,
			OnSuccessfulChangeSetLister onSuccessfulChange) {
		operationManager.execute(models, changes, onSuccessfulChange, OnFailedChangeSetListener.failingListener());
		LOGGER.debug("Model changes applied successfully");
	}

	private void validateModel(Models models) {
		// TODO implement model validation
		LOGGER.debug("Model validation passed successfully");

	}

	private void waitForModelChangesFrom(String changeRequestId) {
		TimeTracker timeTracker = TimeTracker.createAndStart();
		modelPersistence.waitForModelUpdate(changeRequestId, DEFAULT_TIMEOUT, TimeUnit.MINUTES);
		LOGGER.debug("Model changes for {} applied in {} s", changeRequestId, timeTracker.stopInSeconds());
	}

	private String persistChanges(ModelChanges modelChanges) {
		String requestId = modelPersistence.saveChanges(modelChanges);
		LOGGER.debug("Model changes stored under request id: {}", requestId);
		return requestId;
	}

	private void saveChange(ModelChangeSetInfo change) {
		modelPersistence.updateChange(change);
	}

	/**
	 * Perform actual model update. Internal operation
	 *
	 * @param modelChanges the change set to apply
	 */
	void actualUpdate(Models models, ModelChanges modelChanges) {
		LOGGER.info("Updating actual model with current version {} and initial changes version {}. Applying {} changes",
				models.getVersion(), modelChanges.getModelVersion(), modelChanges.getChanges().size());
		// TODO add locking of the model during model update to prevent parallel model clone and update
		verifyModelVersion(models.getVersion(), modelChanges.getModelVersion());
		applyChanges(models, modelChanges.getChanges(), (model, change) -> {
			if (!change.isIntermediate()) {
				change.markAsApplied(model.modelUpdated());
				saveChange(change);
			}
			LOGGER.debug("Successfully applied change {} to {}. Model version updated to {}",
					change.getChangeSet().getOperation(), change.getChangeSet().getPath(), model.getVersion());
		});
		validateModel(models);
		// unlock model
	}
}
