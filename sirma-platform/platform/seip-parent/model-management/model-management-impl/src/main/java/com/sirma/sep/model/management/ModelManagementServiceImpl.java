package com.sirma.sep.model.management;

import com.sirma.itt.seip.concurrent.locks.ContextualReadWriteLock;
import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.codelist.event.ResetCodelistEvent;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.sep.model.ModelImportCompleted;
import com.sirma.sep.model.ModelNode;
import com.sirma.sep.model.management.definition.DefinitionsProvider;
import com.sirma.sep.model.management.hierarchy.ModelHierarchyClass;
import com.sirma.sep.model.management.meta.ModelMetaInfoProvider;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;
import com.sirma.sep.model.management.response.ModelResponse;
import com.sirma.sep.model.management.definition.DefinitionModelConverter;
import com.sirma.sep.model.management.hierarchy.ModelHierarchyBuilder;
import com.sirma.sep.model.management.semantic.SemanticModelConverter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link ModelManagementService} that fetches the semantic hierarchy and converts classes and properties into their
 * corresponding API objects {@link ModelClass} and {@link ModelProperty} along with converting {@link
 * com.sirma.itt.seip.domain.definition.GenericDefinition} into {@link ModelDefinition}. Finally it constructs a hierarchy of the models.
 * <p>
 * These models can be used to view and manage the system's ontology, definitions and code lists through a single point that aggregates
 * them.
 * <p>
 * The service hold the calculated models in a {@link Contextual} {@link Models} to enable multi tenant caching.
 *
 * @author Mihail radkov
 */
@Singleton
public class ModelManagementServiceImpl implements ModelManagementService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private DefinitionsProvider definitionsProvider;

	@Inject
	private ModelMetaInfoProvider modelMetaInfoProvider;

	@Inject
	private ContextualReadWriteLock modelsLock;

	@Inject
	private Contextual<Models> modelsContext;

	@Inject
	private SemanticModelConverter semanticModelConverter;

	@Inject
	private DefinitionModelConverter definitionModelConverter;

	/**
	 * Recalculates the models on {@link ModelImportCompleted}.
	 *
	 * @param modelImportCompleted event notifying that the model import is completed
	 */
	void onModelsImport(@Observes ModelImportCompleted modelImportCompleted) {
		if (modelsContext.isSet()) {
			LOGGER.debug("Triggered models recalculation after model import completion.");
			calculateModels();
		}
	}

	/**
	 * Recalculates the models on {@link ResetCodelistEvent}.
	 *
	 * @param resetCodelistEvent event notifying that the code lists are reset
	 */
	void onCodeListsReload(@Observes ResetCodelistEvent resetCodelistEvent) {
		if (modelsContext.isSet()) {
			LOGGER.debug("Triggered models recalculation after code lists resetting.");
			calculateModels();
		}
	}

	@Override
	public List<ModelHierarchyClass> getModelHierarchy() {
		computeModelsIfAbsent();
		return modelsContext.getContextValue().getModelHierarchy();
	}

	@Override
	public ModelsMetaInfo getMetaInfo() {
		computeModelsIfAbsent();
		return modelsContext.getContextValue().getModelsMetaInfo();
	}

	@Override
	public List<ModelProperty> getProperties() {
		computeModelsIfAbsent();
		return new LinkedList(modelsContext.getContextValue().getProperties().values());
	}

	@Override
	public ModelResponse getModel(String id) {
		computeModelsIfAbsent();

		Models models = modelsContext.getContextValue();
		ModelResponse response = new ModelResponse();

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

	private void computeModelsIfAbsent() {
		if (!modelsContext.isSet() || modelsContext.getContextValue().isEmpty()) {
			calculateModels();
		}
	}

	private void calculateModels() {
		try {
			TimeTracker timeTracker = TimeTracker.createAndStart();

			modelsLock.writeLock().lock();

			Models models = new Models();

			models.setModelsMetaInfo(modelMetaInfoProvider.getModelsMetaInfo());

			ClassInstance rootClass = semanticDefinitionService.getRootClass();

			List<ClassInstance> flatClassHierarchy = getFlatClassHierarchy(rootClass);

			models.setClasses(
					semanticModelConverter.convertModelClasses(flatClassHierarchy, models.getModelsMetaInfo().getSemanticsMapping()));

			Stream<PropertyInstance> allPropertiesStream = Stream.concat(semanticDefinitionService.getProperties().stream(),
					semanticDefinitionService.getRelations().stream());
			models.setProperties(
					semanticModelConverter.convertModelProperties(allPropertiesStream, models.getModelsMetaInfo().getProperties()));

			models.setDefinitions(definitionModelConverter.convertModelDefinitions(definitionsProvider.getUnprocessedDefinitions(),
					models.getModelsMetaInfo()));

			models.setModelHierarchy(ModelHierarchyBuilder.buildHierarchy(models.getClasses(), models.getDefinitions()));

			modelsContext.replaceContextValue(models);

			LOGGER.debug("Models calculation took {} ms", timeTracker.stop());
		} finally {
			modelsLock.writeLock().unlock();
		}
	}

	private List<ClassInstance> getFlatClassHierarchy(ClassInstance currentClass) {
		return flattenClassHierarchy(currentClass).collect(Collectors.toList());
	}

	private static Stream<ClassInstance> flattenClassHierarchy(ClassInstance currentClass) {
		return Stream.concat(Stream.of(currentClass),
				currentClass.getSubClasses().values().stream().flatMap(ModelManagementServiceImpl::flattenClassHierarchy));
	}

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

	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		final Set<Object> seen = new HashSet<>();
		return t -> seen.add(keyExtractor.apply(t));
	}

}
