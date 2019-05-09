package com.sirma.sep.model.management;

import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.sep.model.ModelNode;
import com.sirma.sep.model.management.deploy.DeployModelRequest;
import com.sirma.sep.model.management.deploy.ModelDeployer;
import com.sirma.sep.model.management.exception.UpdateModelFailed;
import com.sirma.sep.model.management.hierarchy.ModelHierarchyClass;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;
import com.sirma.sep.model.management.operation.ModelChangeSet;
import com.sirma.sep.model.management.operation.ModelChangeSetInfo;
import com.sirma.sep.model.management.request.ModelUpdateRequest;
import com.sirma.sep.model.management.request.ModelDeploymentRequest;
import com.sirma.sep.model.management.response.ModelResponse;
import com.sirma.sep.model.management.response.ModelUpdateResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ModelManagementService} that manages {@link ModelsStore} containing the current runtime semantic and definition
 * models.
 * <p>
 * These models can be used to view and manage the system's ontology, definitions and code lists through a single point that aggregates
 * them.
 * <p>
 * The service hold the calculated models in a {@link Contextual} {@link Models} to enable multi tenant caching.
 *
 * @author Mihail radkov
 * @see ModelUpdater
 * @see ModelDeployer
 * @see ModelsStore
 */
@Singleton
public class ModelManagementServiceImpl implements ModelManagementService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private ModelsStore modelsStore;

	@Inject
	private ModelUpdater modelUpdater;

	@Inject
	private ModelPersistence modelPersistence;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private ModelDeployer modelDeployer;

	@Override
	public List<ModelHierarchyClass> getModelHierarchy() {
		return modelsStore.getModels().getModelHierarchy();
	}

	@Override
	public ModelsMetaInfo getMetaInfo() {
		return modelsStore.getModels().getModelsMetaInfo();
	}

	@Override
	public List<ModelProperty> getProperties() {
		return new LinkedList<>(modelsStore.getModels().getProperties().values());
	}

	@Override
	public ModelResponse getModel(String id) {
		Models models = modelsStore.getModels();
		ModelResponse response = new ModelResponse();
		// always return the current model version
		response.setModelVersion(models.getVersion());

		if (models.getClasses().containsKey(id)) {
			ModelClass selectedClass = models.getClasses().get(id);

			List<ModelClass> modelClasses = collectModelNodes(selectedClass);
			response.setClasses(modelClasses);

			List<ModelDefinition> modelDefinitions = collectDefinitions(models, modelClasses);
			response.setDefinitions(modelDefinitions);
		} else if (models.getDefinitions().containsKey(id)) {
			ModelDefinition selectedDefinition = models.getDefinitions().get(id);

			List<ModelDefinition> modelDefinitions = collectModelNodes(selectedDefinition);
			response.setDefinitions(modelDefinitions);

			List<ModelClass> modelClasses = collectClasses(models, modelDefinitions);
			response.setClasses(modelClasses);
		} else {
			// Missing model
			response.setClasses(Collections.emptyList());
			response.setDefinitions(Collections.emptyList());
			return response;
		}

		return response;
	}

	@Override
	public Models getModels() {
		return modelsStore.getModels();
	}

	private Models getModelsCopy() {
		return modelsStore.getModelsCopy();
	}

	@Override
	public ModelUpdateResponse updateModel(ModelUpdateRequest updateRequest) {
		if (updateRequest.isEmpty()) {
			return getChangesSince(updateRequest.getModelVersion());
		}
		Models modelsCopy = getModelsCopy();
		ModelChanges changes = buildModelChanges(updateRequest);
		DeploymentValidationReport report = modelDeployer.validate(modelsCopy, changes);
		if(!report.isValid()) {
			throw new UpdateModelFailed(report);
		}
		if (!modelUpdater.dryRunUpdate(modelsCopy, changes)) {
			LOGGER.warn("All requested changes were already applied. Skipping update request.");
		}
		return getChangesSince(updateRequest.getModelVersion());
	}

	private ModelChanges buildModelChanges(ModelUpdateRequest updateRequest) {
		ModelChanges changes = new ModelChanges();
		changes.setModelVersion(updateRequest.getModelVersion());
		// we set the initial version to all changes to mark them that this is the version when they were created
		changes.setChanges(updateRequest.getChanges()
				.stream()
				.map(toChangeInfo(updateRequest.getModelVersion()))
				.collect(Collectors.toList()));
		return changes;
	}

	private Function<ModelChangeSet, ModelChangeSetInfo> toChangeInfo(Long modelVersion) {
		final Date createdOn = new Date();
		String createdBy = securityContext.getAuthenticated().getSystemId().toString();
		return change -> {
			ModelChangeSetInfo info = new ModelChangeSetInfo();
			info.setChangeSet(change);
			info.setCreatedOn(createdOn);
			info.setCreatedBy(createdBy);
			info.setInitialVersion(modelVersion);
			return info;
		};
	}

	@Override
	public ModelUpdateResponse getChangesSince(Long modelVersion) {
		List<ModelChangeSetInfo> changes = modelPersistence.getChangesSince(modelVersion);

		Models model = getModels();
		return new ModelUpdateResponse()
				.setModelVersion(model.getVersion())
				.setChangeSets(changes)
				.setModelHierarchyChanged(isThereChangesInHierarchy(changes));
	}

	private boolean isThereChangesInHierarchy(List<ModelChangeSetInfo> changes) {
		// TODO: implement actual check when we can modify the hierarchy
		return !changes.isEmpty();
	}

	@Override
	public DeploymentValidationReport validateDeploymentCandidates() {
		Models models = getModels();
		Long version = models.getVersion();

		Set<Path> nonDeployedPaths = modelPersistence.getNonDeployedPaths(version);
		Set<Path> affectedPropertyDomainClasses = getNonDeployedPropertyDomains(models, nonDeployedPaths);
		Set<Path> pathsToDeploy = nonDeployedPaths.stream()
				.filter(path -> Models.isClass(path) || Models.isDefinition(path))
				.collect(Collectors.toSet());

		// property domain classes should be visible for selection so we should add them to the list of changes
		// even there are no actual changes in the classes
		pathsToDeploy.addAll(affectedPropertyDomainClasses);
		DeploymentValidationReport report = validateChangesForDeploy(pathsToDeploy, version);
		copyPropertyValidationToDomainClasses(models, report, nonDeployedPaths);

		return filterReport(report, pathsToDeploy);
	}

	private Set<Path> getNonDeployedPropertyDomains(Models models, Set<Path> nonDeployedPaths) {
		return nonDeployedPaths.stream()
				.filter(Models::isProperty)
				.map(propertyPath -> Models.createClassPath(getPropertyDomain(models, propertyPath)))
				.collect(Collectors.toSet());
	}

	private void copyPropertyValidationToDomainClasses(Models models, DeploymentValidationReport report,
			Collection<Path> nonDeployedPaths) {

		Set<String> changedPropertyNames = nonDeployedPaths.stream()
				.filter(Models::isProperty)
				.map(Path::getValue)
				.collect(Collectors.toSet());

		Map<String, String> domainClassesOfSuccessfulChangedProperties = getPropertyDomain(models,
				report.getSuccessfulEntries(), changedPropertyNames);
		Map<String, String> domainClassesOfFailedChangedProperties = getPropertyDomain(models,
				report.getFailedEntries(), changedPropertyNames);
		Map<String, String> domainClassesOfChangedPropertiesWithWarnings = getPropertyDomain(models,
				report.getEntriesWithWarnings(), changedPropertyNames);

		// separate failing from successful when multiple properties related to the same class are affected and
		// some of them failed and some are fine
		domainClassesOfSuccessfulChangedProperties.values().removeAll(domainClassesOfFailedChangedProperties.values());

		// for the successful properties add their domain classes to the report as successful
		domainClassesOfSuccessfulChangedProperties.forEach((property, domain) -> report.successfulDeploymentValidationFor(domain));

		// append the errors from the failed properties to their domain classes
		domainClassesOfFailedChangedProperties.forEach((property, domain) -> {
			List<String> propertyErrors = getNodeMessages(report.getFailedEntries(), property);
			report.failedDeploymentValidationFor(domain, propertyErrors);
		});

		domainClassesOfChangedPropertiesWithWarnings.forEach((property, domain) -> {
			List<String> propertyWarnings = getNodeMessages(report.getEntriesWithWarnings(), property);
			report.validationWarningFor(domain, propertyWarnings);
		});
	}

	private static List<String> getNodeMessages(List<DeploymentValidationReport.ValidationReportEntry> entries, String id) {
		return entries.stream()
				.filter(node -> id.equals(node.getId()))
				.map(DeploymentValidationReport.ValidationReportEntry::getMessages)
				.flatMap(Collection::stream)
				.map(DeploymentValidationReport.ReportMessage::getMessage)
				.collect(Collectors.toList());
	}

	private <V extends DeploymentValidationReport.ValidationReportEntry> Map<String, String> getPropertyDomain(
			Models models, List<V> validationReport, Set<String> changedPropertyNames) {
		return validationReport
				.stream()
				.map(DeploymentValidationReport.ValidationReportEntry::getId)
				.filter(changedPropertyNames::contains)
				.collect(Collectors.toMap(Function.identity(),
						propertyId -> getPropertyDomain(models, Models.createPropertyPath(propertyId))));
	}

	private static String getPropertyDomain(Models models, Path propertySelector) {
		Object node = propertySelector.walk(models);
		return ((ModelProperty) node).getDomain();
	}

	private static DeploymentValidationReport filterReport(DeploymentValidationReport report, Set<Path> pathsToDeploy) {
		// TODO: this is a minor workaround proposed by Borislav Bonev which will be removed
		Set<String> nonDeployedNodesIds = pathsToDeploy.stream().map(Path::getValue).collect(Collectors.toSet());
		report.getNodes().removeIf(node -> !nonDeployedNodesIds.contains(node.getId()));
		return report;
	}

	@Override
	public DeploymentValidationReport deployChanges(ModelDeploymentRequest deploymentRequest) {
		if (deploymentRequest.isEmpty()) {
			LOGGER.info("Ignoring empty deployment request");
			return new DeploymentValidationReport();
		}

		List<String> modelsToDeploy = deploymentRequest.getModelsToDeploy();
		Long version = deploymentRequest.getVersion();

		LOGGER.info("Received request to deploy nodes: {}", modelsToDeploy);

		Set<Path> nonDeployedPaths = modelPersistence.getNonDeployedPaths(version);
		Models models = getModels();
		Set<Path> affectedPropertyDomainClasses = getNonDeployedPropertyDomains(models, nonDeployedPaths);
		Set<Path> pathsToDeploy = nonDeployedPaths.stream()
				.filter(path -> modelsToDeploy.contains(path.getValue()))
				.collect(Collectors.toSet());
		affectedPropertyDomainClasses.removeIf(path -> !modelsToDeploy.contains(path.getValue()));
		pathsToDeploy.addAll(affectedPropertyDomainClasses);

		DeploymentValidationReport report = deployChanges(pathsToDeploy, version);
		if (!report.isValid()) {
			LOGGER.warn("Tried to deploy invalid model changes: \nmodel errors: {} \ngeneric errors: {}", report.getFailedEntries(),
					report.getGenericErrors());
		}
		return report;
	}

	private DeploymentValidationReport validateChangesForDeploy(Set<Path> pathsToDeploy, long version) {
		DeployModelRequest deployRequest = createDeployModelRequest(pathsToDeploy, version);
		DeploymentValidationReport validationReport = modelDeployer.validate(deployRequest);
		validationReport.setVersion(version);
		return validationReport;
	}

	private DeploymentValidationReport deployChanges(Set<Path> pathsToDeploy, long version) {
		DeployModelRequest deployRequest = createDeployModelRequest(pathsToDeploy, version);
		return modelDeployer.deploy(deployRequest);
	}

	private DeployModelRequest createDeployModelRequest(Set<Path> pathsToDeploy, Long version) {
		DeployModelRequest deployRequest = new DeployModelRequest();
		deployRequest.setPathsToDeploy(pathToString(pathsToDeploy));
		deployRequest.setVersion(version);
		return deployRequest;
	}

	private static Set<String> pathToString(Set<Path> paths) {
		return paths.stream().map(Path::toString).collect(Collectors.toSet());
	}

	@SuppressWarnings("unchecked")
	private static <M extends ModelNode> List<M> collectModelNodes(M modelNode) {
		List<M> collected = new LinkedList<>();
		M currentNode = modelNode;

		while (currentNode != null) {
			collected.add(currentNode);
			currentNode = (M) currentNode.getParentReference();
		}

		return collected;
	}

	private static List<ModelDefinition> collectDefinitions(Models models, List<ModelClass> modelClasses) {
		Set<String> classIds = modelClasses.stream().map(ModelClass::getId).collect(Collectors.toSet());
		return models.getDefinitions()
				.values()
				.stream()
				.filter(definition -> classIds.contains(definition.getRdfType()))
				.collect(Collectors.toList());
	}

	private static List<ModelClass> collectClasses(Models models, List<ModelDefinition> modelDefinitions) {
		Set<String> definitionRdfTypes = modelDefinitions.stream()
				.filter(def -> StringUtils.isNotBlank(def.getRdfType()))
				.map(ModelDefinition::getRdfType)
				.collect(Collectors.toSet());

		List<ModelClass> definitionClasses = models.getClasses()
				.values()
				.stream()
				.filter(modelClass -> definitionRdfTypes.contains(modelClass.getId()))
				.collect(Collectors.toList());

		return definitionClasses.stream()
				.map(ModelManagementServiceImpl::collectModelNodes)
				.flatMap(Collection::stream)
				.filter(distinctByKey(ModelClass::getId))
				.collect(Collectors.toList());
	}

	private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		final Set<Object> seen = new HashSet<>();
		return t -> seen.add(keyExtractor.apply(t));
	}

}
