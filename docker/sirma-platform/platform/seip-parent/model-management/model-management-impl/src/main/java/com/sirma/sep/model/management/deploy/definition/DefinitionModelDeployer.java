package com.sirma.sep.model.management.deploy.definition;

import static com.sirma.sep.model.management.deploy.ModelDeploymentUtils.getIds;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.validation.ValidationMessage;
import com.sirma.itt.seip.domain.validation.ValidationReport;
import com.sirma.sep.model.management.ModelDefinition;
import com.sirma.sep.model.management.Path;
import com.sirma.sep.model.management.codelists.CodeListsProvider;
import com.sirma.sep.model.management.definition.DefinitionsProvider;
import com.sirma.sep.model.management.definition.export.GenericDefinitionConverter;
import com.sirma.sep.model.management.deploy.DeploymentModels;
import com.sirma.sep.model.management.operation.ModelChangeSetInfo;
import com.sirma.sep.model.management.persistence.ModelChangesDao;

/**
 * Service responsible for deploying definition model to be used by the rest of the system.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 10/08/2018
 */
public class DefinitionModelDeployer {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private ModelChangesDao modelChangesDao;
	// TODO: Should be renamed to something like DefinitionChangeSetPreprocessor
	@Inject
	private DefinitionChangeSetManager definitionsChangeSetManager;
	@Inject
	private DefinitionsProvider definitionsProvider;
	@Inject
	private CodeListsProvider codeListsProvider;
	@Inject
	private GenericDefinitionConverter genericDefinitionConverter;

	/**
	 * Performs a validation of the given model definitions as it's going to be deployed to the end user.
	 * This should be a equal to compiled definition instance.
	 *
	 * @param definitionPaths the paths to the model definitions to validate
	 * @param deploymentModels model wrapper that can provide information about the model node and of it's changes
	 * @return validation report carrying any validation message errors or warnings
	 */
	public ValidationReport validateDefinitions(List<Path> definitionPaths, DeploymentModels deploymentModels) {
		if (CollectionUtils.isEmpty(definitionPaths)) {
			return ValidationReport.valid();
		}

		List<String> definitionIds = getIds(definitionPaths);
		LOGGER.debug("Validating definitions {}", definitionIds);

		Map<Path, List<ModelChangeSetInfo>> definitionToChangesMap = mapDefinitionChanges(definitionPaths, deploymentModels);

		Map<String, GenericDefinition> unprocessedDefinitions = loadUnprocessedDefinitions(definitionIds);

		DefinitionDeploymentContext context = new DefinitionDeploymentContext();

		ValidationReport validationReport = new ValidationReport();

		applyDefinitionChanges(deploymentModels, definitionToChangesMap, unprocessedDefinitions, context, validateRequest -> {
			List<ValidationMessage> errors = definitionsChangeSetManager.validate(validateRequest);
			validationReport.addMessages(errors);
		});

		ValidationReport definitionValidationReport = definitionsProvider.validateDefinitions(unprocessedDefinitions.values());
		validationReport.merge(definitionValidationReport);

		return validationReport;
	}

	/**
	 * Deploy changes to the given definition nodes.
	 *
	 * @param definitions the definitions to deploy
	 * @param deploymentModels model wrapper that can provide information about the model node and of it's changes
	 */
	public void deployDefinitions(List<Path> definitions, DeploymentModels deploymentModels) {
		if (CollectionUtils.isEmpty(definitions)) {
			return;
		}

		List<String> definitionIds = getIds(definitions);
		LOGGER.info("Received request to deploy definitions {}", definitionIds);

		Map<Path, List<ModelChangeSetInfo>> definitionToChangesMap = mapDefinitionChanges(definitions, deploymentModels);

		Map<String, GenericDefinition> unprocessedDefinitions = loadUnprocessedDefinitions(definitionIds);

		DefinitionDeploymentContext context = new DefinitionDeploymentContext();

		applyDefinitionChanges(deploymentModels, definitionToChangesMap, unprocessedDefinitions, context,
				definitionsChangeSetManager::apply);

		// Code lists should be reloaded before importing the definitions
		if (context.hasUpdatedCodelists()) {
			codeListsProvider.reloadCodelists();
		}

		definitionsProvider.updateDefinitions(unprocessedDefinitions.values());

		// Mark all changes (even skipped during aggregation) as deployed
		markDefinitionChangesAsDeployed(definitionToChangesMap.values());
	}

	private static Map<Path, List<ModelChangeSetInfo>> mapDefinitionChanges(List<Path> definitions, DeploymentModels deploymentModels) {
		Map<Path, List<ModelChangeSetInfo>> definitionToChangesMap = new LinkedHashMap<>();
		definitions.forEach(definitionPath -> {
			List<ModelChangeSetInfo> notAppliedChanges = deploymentModels.getDeployableChangesFor(definitionPath);
			if (!notAppliedChanges.isEmpty()) {
				definitionToChangesMap.put(definitionPath.cutOffTail(), notAppliedChanges);
			} else {
				LOGGER.warn("There are no un-deployed changes for definition {}", definitionPath.getValue());
			}
		});
		return definitionToChangesMap;
	}

	private Map<String, GenericDefinition> loadUnprocessedDefinitions(List<String> definitionIds) {
		List<GenericDefinition> unprocessedDefinitions = definitionsProvider.getUnprocessedDefinitions(definitionIds);
		return unprocessedDefinitions.stream().collect(Collectors.toMap(GenericDefinition::getIdentifier, Function.identity()));
	}

	private void applyDefinitionChanges(DeploymentModels deploymentModels,
			Map<Path, List<ModelChangeSetInfo>> definitionToChangesMap,
			Map<String, GenericDefinition> unprocessedDefinitions, DefinitionDeploymentContext context,
			Consumer<DefinitionDeploymentRequest> preProcessConsumer) {
		definitionToChangesMap.forEach((path, changes) -> {
			ModelDefinition modelDefinition = deploymentModels.resolveNode(path);
			GenericDefinition unprocessedDefinition = unprocessedDefinitions.get(path.getValue());
			if (unprocessedDefinition != null) {
				DefinitionDeploymentRequest request = new DefinitionDeploymentRequest(deploymentModels.getModels(),
						modelDefinition, unprocessedDefinition, changes, context);
				preProcessConsumer.accept(request);
				genericDefinitionConverter.copyToDefinition(modelDefinition, (GenericDefinitionImpl) unprocessedDefinition);
			} else {
				LOGGER.warn("Couldn't load unprocessed definition for definition model {}", modelDefinition.getId());
			}
		});
	}

	private void markDefinitionChangesAsDeployed(Collection<List<ModelChangeSetInfo>> changes) {
		Set<Long> changeSetIds = changes.stream()
				.flatMap(Collection::stream)
				.map(ModelChangeSetInfo::getIndex)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		modelChangesDao.markAsDeployed(changeSetIds);
	}

}
