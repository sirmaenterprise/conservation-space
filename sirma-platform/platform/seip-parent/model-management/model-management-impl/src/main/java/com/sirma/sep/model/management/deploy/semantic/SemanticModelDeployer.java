package com.sirma.sep.model.management.deploy.semantic;

import static com.sirma.sep.model.management.deploy.ModelDeploymentUtils.getIds;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.eclipse.rdf4j.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.validation.ValidationMessage;
import com.sirma.itt.seip.domain.validation.ValidationReport;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.model.management.Path;
import com.sirma.sep.model.management.deploy.ChangeSetAggregator;
import com.sirma.sep.model.management.deploy.DeploymentModels;
import com.sirma.sep.model.management.meta.ModelMetaInfo;
import com.sirma.sep.model.management.operation.ModelChangeSetInfo;
import com.sirma.sep.model.management.persistence.ModelChangesDao;
import com.sirma.sep.model.management.persistence.SemanticDatabasePersistence;

/**
 * Service for deploying semantic models to the runtime model of the system.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @author Mihail Radkov
 */
public class SemanticModelDeployer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private ModelChangesDao modelChangesDao;
	@Inject
	private SemanticModelGenerator semanticModelGenerator;
	@Inject
	private SemanticDatabasePersistence semanticDatabasePersistence;
	@Inject
	private SemanticDefinitionService semanticDefinitionService;
	@Inject
	private TransactionSupport transactionSupport;
	@Inject
	private SemanticClassDeployer semanticClassDeployer;

	/**
	 * Validate the semantic class information with the database for inconsistencies
	 *
	 * @param nodePath the path for the model class to validate
	 * @param deploymentModels model wrapper that can provide information about the model node and of it's changes
	 * @return validation report carrying any validation message errors or warnings
	 */
	public ValidationReport validateSemanticClass(Path nodePath, DeploymentModels deploymentModels) {
		LOGGER.debug("Validating model class {}", nodePath.getValue());
		return new ValidationReport(validateClass(nodePath, deploymentModels));
	}

	/**
	 * Validate the semantic property information with the database for inconsistencies
	 *
	 * @param nodePath the path for the model property to check
	 * @param deploymentModels model wrapper that can provide information about the model node and of it's changes
	 * @return validation report carrying any validation message errors or warnings
	 */
	public ValidationReport validateSemanticProperty(Path nodePath, DeploymentModels deploymentModels) {
		LOGGER.debug("Validating model property {}", nodePath.getValue());
		return new ValidationReport(validateSemanticChanges(nodePath, deploymentModels));
	}

	/**
	 * Deploy changes for the given semantic properties and classes to persistent storage
	 *
	 * @param properties the paths for the model properties to deploy
	 * @param classes the paths for the model classes to deploy
	 * @param deploymentModels model wrapper that can provide information about the model node and of it's changes
	 */
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public void deploySemanticNodes(List<Path> properties, List<Path> classes, DeploymentModels deploymentModels) {
		boolean deployedProperties = deploySemanticProperties(properties, deploymentModels);
		boolean deployedClasses = deploySemanticClasses(classes, deploymentModels);

		if (deployedProperties || deployedClasses) {
			// reset the semantic cache, need to be in another Tx after flushing of changes
			transactionSupport.invokeOnSuccessfulTransactionInTx(semanticDefinitionService::modelUpdated);
		}
	}

	private boolean deploySemanticClasses(List<Path> classes, DeploymentModels deploymentModels) {
		if (CollectionUtils.isEmpty(classes)) {
			return false;
		}
		LOGGER.info("Received request to deploy classes {}", getIds(classes));
		return persistClasses(classes, deploymentModels);
	}

	private boolean deploySemanticProperties(List<Path> properties, DeploymentModels deploymentModels) {
		if (CollectionUtils.isEmpty(properties)) {
			return false;
		}
		LOGGER.info("Received request to deploy properties {}", getIds(properties));
		return persistSemanticChanges(properties, deploymentModels);
	}

	private List<ValidationMessage> validateSemanticChanges(Path nodePath, DeploymentModels deploymentModels) {
		List<ModelChangeSetInfo> notAppliedChangesForNode = deploymentModels.getDeployableChangesFor(nodePath);
		List<SemanticChange> semanticChanges = semanticModelGenerator.generateDatabaseChanges(notAppliedChangesForNode);
		List<Statement> oldStatements = semanticChanges.stream().flatMap(SemanticChange::getToRemove).collect(toList());
		return semanticDatabasePersistence.validateDatabaseState(oldStatements);
	}

	private boolean persistSemanticChanges(List<Path> nodePaths, DeploymentModels deploymentModels) {
		List<List<SemanticChange>> changesToSave = nodePaths.stream()
				.map(deploymentModels::getDeployableChangesFor)
				.map(semanticModelGenerator::generateDatabaseChanges).collect(toList());

		for (List<SemanticChange> changes : changesToSave) {

			List<Statement> newStatements = changes.stream().flatMap(SemanticChange::getToAdd).collect(toList());
			List<Statement> oldStatements = changes.stream().flatMap(SemanticChange::getToRemove).collect(toList());

			semanticDatabasePersistence.saveChanges(newStatements, oldStatements);

			Set<Long> deployedChangeIds = changes.stream()
					.flatMap(change -> change.getOriginalChanges()
							.stream()
							.filter(ModelChangeSetInfo::hasIndex)
							.map(ModelChangeSetInfo::getIndex))
					.collect(toSet());

			modelChangesDao.markAsDeployed(deployedChangeIds);
		}
		return !changesToSave.isEmpty();
	}

	private List<ValidationMessage> validateClass(Path classPath, DeploymentModels deploymentModels) {
		List<ModelChangeSetInfo> changes = deploymentModels.getDeployableChangesFor(classPath);
		SemanticClassDeploymentPayload payload = buildDeploymentPayload(classPath, changes, deploymentModels);
		if (!payload.isEmpty()) {
			return semanticClassDeployer.validate(payload);
		}
		return Collections.emptyList();
	}

	private boolean persistClasses(List<Path> classPaths, DeploymentModels deploymentModels) {
		List<SemanticClassDeploymentPayload> deployed = new LinkedList<>();

		classPaths.forEach(classPath -> {
			List<ModelChangeSetInfo> changes = deploymentModels.getDeployableChangesFor(classPath);
			SemanticClassDeploymentPayload payload = buildDeploymentPayload(classPath, changes, deploymentModels);

			if (!payload.isEmpty()) {
				semanticClassDeployer.deploy(payload);

				Set<Long> deployedChangeIds = changes.stream()
						.filter(ModelChangeSetInfo::hasIndex)
						.map(ModelChangeSetInfo::getIndex)
						.collect(toSet());
				modelChangesDao.markAsDeployed(deployedChangeIds);
				deployed.add(payload);
			}
		});

		return !deployed.isEmpty();
	}

	private static SemanticClassDeploymentPayload buildDeploymentPayload(Path classPath, List<ModelChangeSetInfo> changes,
			DeploymentModels deploymentModels) {
		Map<String, ModelMetaInfo> classMetaInfo = deploymentModels.getModels().getModelsMetaInfo().getSemanticsMapping();
		SemanticClassDeploymentPayload payload = new SemanticClassDeploymentPayload(classPath.getValue(), classMetaInfo);
		return populatePayload(payload, changes);
	}

	private static SemanticClassDeploymentPayload populatePayload(SemanticClassDeploymentPayload payload,
			List<ModelChangeSetInfo> changes) {
		ChangeSetAggregator.aggregate(changes, SemanticModelDeployer::toLanguagePair)
				.forEach((path, groupedChanges) -> {
					// Use original change because after aggregation the tail could be for a language pair, e.g. /key=en
					String propertyUri = groupedChanges.get(0).getDelegate().getChangeSet().getPath().tail().getValue();

					Object oldValue = groupedChanges.get(0).getOldValue();
					payload.toRemove(propertyUri, (Serializable) oldValue);

					Object newValue = groupedChanges.get(groupedChanges.size() - 1).getNewValue();
					payload.toAdd(propertyUri, (Serializable) newValue);
				});
		return payload;
	}

	private static LanguagePair toLanguagePair(String key, Object value) {
		return new LanguagePair(key.toLowerCase(), value != null ? value.toString() : null);
	}

}
