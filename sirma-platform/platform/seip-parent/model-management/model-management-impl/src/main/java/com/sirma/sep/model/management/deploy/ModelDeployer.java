package com.sirma.sep.model.management.deploy;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.validation.ValidationMessage.MessageSeverity;
import com.sirma.itt.seip.domain.validation.ValidationReport;
import com.sirma.itt.seip.domain.validation.ValidationReportTranslator;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.model.management.DeploymentValidationReport;
import com.sirma.sep.model.management.ModelChangeSetOperationManager;
import com.sirma.sep.model.management.ModelChanges;
import com.sirma.sep.model.management.ModelOpSynchronization;
import com.sirma.sep.model.management.ModelPersistence;
import com.sirma.sep.model.management.ModelProperty;
import com.sirma.sep.model.management.Models;
import com.sirma.sep.model.management.ModelsStore;
import com.sirma.sep.model.management.Path;
import com.sirma.sep.model.management.deploy.definition.DefinitionModelDeployer;
import com.sirma.sep.model.management.deploy.semantic.SemanticModelDeployer;
import com.sirma.sep.model.management.exception.ChangeSetCollisionException;
import com.sirma.sep.model.management.operation.ModelChangeSetInfo;
import com.sirmaenterprise.sep.jms.annotations.DestinationDef;
import com.sirmaenterprise.sep.jms.annotations.QueueListener;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service responsible for deploying changes respective data stores so the models could be used from the end users. <br>
 * Deployment is going to happen asynchronously.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 01/08/2018
 */
public class ModelDeployer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@DestinationDef
	public static final String MODEL_DEPLOY_QUEUE = "java:/jms.queue.DeployModelQueue";
	private static final String REQUEST_ID = "requestId";

	@Inject
	private SenderService senderService;
	@Inject
	private ModelsStore modelsStore;
	@Inject
	private ModelPersistence modelPersistence;
	@Inject
	private ModelChangeSetOperationManager changeSetOperationManager;
	@Inject
	private DefinitionModelDeployer definitionModelDeployer;
	@Inject
	private SemanticModelDeployer semanticModelDeployer;
	@Inject
	private TransactionSupport transactionSupport;
	@Inject
	private ModelOpSynchronization modelOpSynchronization;
	@Inject
	private LabelProvider labelProvider;

	/**
	 * Run validation on the selected nodes for deployment. The result of the call is a report that contains all
	 * requested nodes and any errors found during the validation for each node. No actual changes are applied
	 * to the database.
	 * <p>
	 * The provided models however will be modified by the validation process.
	 *
	 * @param deployRequest request carrying the models to deploy
	 */
	public DeploymentValidationReport validate(DeployModelRequest deployRequest) {
		DeploymentModels models = loadModelsForDeploy(deployRequest);
		List<Path> affected = computeAffectedNodes(models.getModels(), stringToPath(deployRequest.getPathsToDeploy()),
				deployRequest.getVersion());
		return validate(models, affected);
	}

	/**
	 * Run validation on the selected models with the models changes applied before real update is performed. The result
	 * of the call is a report that contains all the requested nodes and any errors found during the validation for
	 * each node. No actual changes are applied to the database.
	 *
	 * @param models the models to be validated
	 * @param changes the changes to be applied before validation
	 */
	public DeploymentValidationReport validate(Models models, ModelChanges changes) {
		DeploymentModels deploymentModels = new DeploymentModels(models);
		changes.getChanges().forEach(deploymentModels::addChange);
		List<Path> affectedPaths = changes
				.getChanges()
				.stream()
				.map(change -> change.getChangeSet().getPath().head())
				.distinct()
				.collect(Collectors.toList());
		return validate(deploymentModels, affectedPaths);
	}

	private DeploymentValidationReport validate(DeploymentModels models, List<Path> affectedPaths) {
		DeploymentValidationReport report = new DeploymentValidationReport();
		// TODO: class/prop deployer could also validate multiple nodes at once
		validateNodes(affectedPaths, Models::isClass, semanticModelDeployer::validateSemanticClass, models, report);
		validateNodes(affectedPaths, Models::isProperty, semanticModelDeployer::validateSemanticProperty, models, report);
		validateMultipleNodes(affectedPaths, Models::isDefinition, definitionModelDeployer::validateDefinitions, models, report);
		return report;
	}

	private void validateNodes(List<Path> nodes, Predicate<Path> pathFilter, SingleNodeValidator validator, DeploymentModels models,
			DeploymentValidationReport deploymentReport) {
		nodes.stream().filter(pathFilter).forEach(nodePath -> {
			ValidationReport validationReport = validator.validate(nodePath, models);
			toDeploymentReport(Collections.singletonList(nodePath), validationReport, deploymentReport);
		});
	}

	private void validateMultipleNodes(List<Path> nodes, Predicate<Path> pathFilter, MultiNodeValidator validator,
			DeploymentModels models, DeploymentValidationReport deploymentReport) {
		List<Path> filteredNodes = nodes.stream().filter(pathFilter).collect(Collectors.toList());
		ValidationReport validationReport = validator.validate(filteredNodes, models);
		toDeploymentReport(filteredNodes, validationReport, deploymentReport);
	}

	private void toDeploymentReport(List<Path> nodes, ValidationReport validationReport, DeploymentValidationReport deploymentReport) {
		ValidationReportTranslator reportTranslator = new ValidationReportTranslator(labelProvider, validationReport);

		List<String> genericErrors = reportTranslator.getGenericErrors();
		deploymentReport.addGenericErrors(genericErrors);

		Map<String, List<String>> errors = reportTranslator.getNodeMessages(MessageSeverity.ERROR);
		errors.forEach(deploymentReport::failedDeploymentValidationFor);

		Map<String, List<String>> warnings = reportTranslator.getNodeMessages(MessageSeverity.WARNING);
		warnings.forEach(deploymentReport::validationWarningFor);

		// Find and register successfully validated nodes
		List<String> validatedNodes = nodes.stream().map(Path::getValue).collect(Collectors.toList());
		validatedNodes.removeAll(errors.keySet());
		validatedNodes.removeAll(warnings.keySet());
		validatedNodes.forEach(deploymentReport::successfulDeploymentValidationFor);
	}

	/**
	 * Scheduler deployment of the models specified in the given deploy request
	 *
	 * @param deployRequest request carrying the models to deploy
	 */
	public DeploymentValidationReport deploy(DeployModelRequest deployRequest) {
		DeploymentValidationReport report = validate(deployRequest);
		if (report.isValid()) {
			String requestId = modelOpSynchronization.acquire();
			transactionSupport.invokeBiConsumerInNewTx(this::deployModel, deployRequest, requestId);
			modelOpSynchronization.waitForRequest(requestId, 5, TimeUnit.MINUTES);
		}
		return report;
	}

	private DeploymentModels loadModelsForDeploy(DeployModelRequest deployRequest) {
		Models runtimeModels = modelsStore.getRuntimeModelsCopy();
		DeploymentModels deploymentModels = new DeploymentModels(runtimeModels);
		Set<String> nodeIdsToDeploy = stringToPath(deployRequest.getPathsToDeploy()).stream()
				.map(Path::getValue)
				.collect(Collectors.toSet());
		// resolve only changes that affect the requested models and not all of them
		// this way we will not deploy something that was never picked by the user
		List<ModelChangeSetInfo> notDeployedChanges = modelPersistence.getNotDeployedChanges(deployRequest.getVersion())
				.stream()
				.filter(allowedForDeployment(nodeIdsToDeploy))
				.collect(Collectors.toList());
		changeSetOperationManager.execute(runtimeModels, notDeployedChanges,
				(m, c) -> {
					deploymentModels.addChange(c);
					LOGGER.trace("Successfully applied {}", c.getChangeSet().getPath().prettyPrint());
				},
				(v, c) -> {
					if (v instanceof ChangeSetCollisionException) {
						return true;
					}
					throw new IllegalStateException("Cannot apply non published change!", v);
				});
		return deploymentModels;
	}

	private Predicate<ModelChangeSetInfo> allowedForDeployment(Set<String> nodeIdsToDeploy) {
		return changeSetInfo -> {
			Path path = changeSetInfo.getChangeSet().getPath();
			return nodeIdsToDeploy.contains(path.getValue()) || Models.isProperty(path);
		};
	}

	private void deployModel(DeployModelRequest deployRequest, String requestId) {
		String messagePayload;
		try {
			ObjectMapper jsonMapper = new ObjectMapper();
			messagePayload = jsonMapper.writeValueAsString(deployRequest);
		} catch (JsonProcessingException e) {
			throw new RollbackedRuntimeException(e);
		}
		SendOptions sendOptions = SendOptions.create().withProperty(REQUEST_ID, requestId);
		senderService.sendText(MODEL_DEPLOY_QUEUE, messagePayload, sendOptions);
	}

	private static Set<Path> stringToPath(Set<String> paths) {
		return paths.stream().map(Path::parsePath).collect(Collectors.toSet());
	}

	private List<Path> computeAffectedNodes(Models models, Collection<Path> chosenPaths, Long version) {
		Set<Path> nonDeployedPaths = modelPersistence.getNonDeployedPaths(version);
		Stream<Path> modifiedProperties = nonDeployedPaths.stream().filter(Models::isProperty).filter(propertyPath -> {
			Object node = propertyPath.walk(models);
			if (node instanceof ModelProperty) {
				String propertyDomain = ((ModelProperty) node).getDomain();
				return chosenPaths.contains(Models.createClassPath(propertyDomain));
			}
			// do not deploy properties for non picked classes
			return false;
		});

		// return all path roots (classes, definitions, properties)
		return Stream.concat(modifiedProperties, chosenPaths.stream()).distinct().collect(Collectors.toList());
	}

	@QueueListener(MODEL_DEPLOY_QUEUE)
	public void onDeployModelRequest(Message message) throws JMSException {
		String body = message.getBody(String.class);
		try {
			ObjectMapper jsonMapper = new ObjectMapper();
			DeployModelRequest deployRequest = jsonMapper.readValue(body, DeployModelRequest.class);
			doDeploy(deployRequest);
		} catch (IOException e) {
			throw new RollbackedRuntimeException(e);
		} finally {
			String requestId = message.getStringProperty(REQUEST_ID);
			// no matter the deploy result we should notify for the change
			// may be we should return status success/fail and what failed
			modelOpSynchronization.release(requestId);
		}
	}

	private void doDeploy(DeployModelRequest deployRequest) {
		TimeTracker tracker = TimeTracker.createAndStart();

		DeploymentModels deploymentModels = loadModelsForDeploy(deployRequest);
		List<Path> affected = computeAffectedNodes(deploymentModels.getModels(), stringToPath(deployRequest.getPathsToDeploy()),
				deployRequest.getVersion());

		List<Path> affectedClasses = affected.stream().filter(Models::isClass).collect(Collectors.toList());
		List<Path> affectedProperties = affected.stream().filter(Models::isProperty).collect(Collectors.toList());
		List<Path> affectedDefinitions = affected.stream().filter(Models::isDefinition).collect(Collectors.toList());

		semanticModelDeployer.deploySemanticNodes(affectedProperties, affectedClasses, deploymentModels);
		definitionModelDeployer.deployDefinitions(affectedDefinitions, deploymentModels);

		LOGGER.debug("Models have been deployed in {} seconds, clearing the store.", tracker.stopInSeconds());
		modelsStore.clear();
	}

	@FunctionalInterface
	private interface SingleNodeValidator {
		ValidationReport validate(Path nodePath, DeploymentModels deploymentModels);
	}

	@FunctionalInterface
	private interface MultiNodeValidator {
		ValidationReport validate(List<Path> nodesPaths, DeploymentModels deploymentModels);
	}
}
