package com.sirma.sep.model.management;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.sep.model.management.definition.DefinitionModelConverter;
import com.sirma.sep.model.management.definition.DefinitionsProvider;
import com.sirma.sep.model.management.hierarchy.ModelHierarchyBuilder;
import com.sirma.sep.model.management.meta.ModelMetaInfoProvider;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;
import com.sirma.sep.model.management.semantic.SemanticModelConverter;

/**
 * Service that produces {@link Models} by fetching the semantic hierarchy and converts classes and properties into their
 * corresponding API objects {@link ModelClass} and {@link ModelProperty} along with converting non compiled {@link
 * com.sirma.itt.seip.domain.definition.GenericDefinition} into {@link ModelDefinition}. Finally it constructs a hierarchy of the models
 * <p>
 * The produced {@link Models} up to date with the application's runtime semantic and definition models.
 *
 * @author Mihail Radkov
 */
@Singleton
public final class ModelBuilder {

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private DefinitionsProvider definitionsProvider;

	@Inject
	private ModelMetaInfoProvider modelMetaInfoProvider;

	@Inject
	private SemanticModelConverter semanticModelConverter;

	@Inject
	private DefinitionModelConverter definitionModelConverter;

	/**
	 * Builds and populates {@link Models} from the runtime semantic and definition models.
	 *
	 * @return freshly produced {@link Models}
	 */
	public Models buildModels() {
		Models models = new Models();

		ModelsMetaInfo modelsMetaInfo = modelMetaInfoProvider.getModelsMetaInfo();
		models.setModelsMetaInfo(modelsMetaInfo);

		ClassInstance rootClass = semanticDefinitionService.getRootClass();

		List<ClassInstance> flatClassHierarchy = getFlatClassHierarchy(rootClass);

		models.setClasses(
				semanticModelConverter.convertModelClasses(flatClassHierarchy, modelsMetaInfo));

		Stream<PropertyInstance> allPropertiesStream = Stream.concat(semanticDefinitionService.getProperties().stream(),
				semanticDefinitionService.getRelations().stream());
		models.setProperties(
				semanticModelConverter.convertModelProperties(allPropertiesStream, modelsMetaInfo));

		models.setDefinitions(
				definitionModelConverter.convertModelDefinitions(definitionsProvider.getUnprocessedDefinitions(), modelsMetaInfo));

		models.setModelHierarchy(ModelHierarchyBuilder.buildHierarchy(models.getClasses(), models.getDefinitions()));

		return models;
	}

	public Models copyModels(Models models) {
		Models copy = models.createCopy();
		semanticModelConverter.linkClasses(copy.getClasses());
		definitionModelConverter.linkDefinitions(copy.getDefinitions());
		// Hierarchy must be explicitly built to use actual model references
		copy.setModelHierarchy(ModelHierarchyBuilder.buildHierarchy(copy.getClasses(), copy.getDefinitions()));
		return copy;
	}

	private List<ClassInstance> getFlatClassHierarchy(ClassInstance currentClass) {
		return flattenClassHierarchy(currentClass).collect(Collectors.toList());
	}

	private static Stream<ClassInstance> flattenClassHierarchy(ClassInstance currentClass) {
		return Stream.concat(Stream.of(currentClass),
				currentClass.getSubClasses().values().stream().flatMap(ModelBuilder::flattenClassHierarchy));
	}
}
