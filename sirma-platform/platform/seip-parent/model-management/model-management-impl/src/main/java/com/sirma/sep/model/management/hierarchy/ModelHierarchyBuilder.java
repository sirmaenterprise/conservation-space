package com.sirma.sep.model.management.hierarchy;

import com.sirma.sep.model.management.ModelClass;
import com.sirma.sep.model.management.ModelDefinition;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Constructs a hierarchy from the available {@link ModelClass} and {@link ModelDefinition}.
 * <p>
 * Each {@link ModelClass} includes its associated {@link ModelDefinition} mapped by their RDF type.
 *
 * @author Mihail Radkov
 */
public class ModelHierarchyBuilder {

	private ModelHierarchyBuilder() {
		// Prevents instantiation for this utility class
	}

	/**
	 * Builds a model hierarchy from the provided classes and definitions
	 *
	 * @param modelClasses the available semantic class models
	 * @param modelDefinitions the available definition models
	 * @return the built hierarchy
	 */
	public static List<ModelHierarchyClass> buildHierarchy(Map<String, ModelClass> modelClasses,
			Map<String, ModelDefinition> modelDefinitions) {
		Map<String, List<ModelDefinition>> rdfTypeToDefinition = modelDefinitions.values()
				.stream()
				.filter(d -> StringUtils.isNotBlank(d.getRdfType()))
				.collect(Collectors.groupingBy(ModelDefinition::getRdfType));

		return modelClasses.values().stream().map(modelClass -> {
			ModelHierarchyClass classNode = new ModelHierarchyClass();
			classNode.setId(modelClass.getId());
			classNode.setParentId(modelClass.getParent());
			classNode.setLabels(modelClass.getLabels());

			List<ModelDefinition> subTypes = rdfTypeToDefinition.getOrDefault(modelClass.getId(), Collections.emptyList());
			classNode.setSubTypes(subTypes.stream().map(ModelHierarchyBuilder::toDefinitionNode).collect(Collectors.toList()));

			return classNode;
		}).collect(Collectors.toList());
	}

	private static ModelHierarchyDefinition toDefinitionNode(ModelDefinition modelDefinition) {
		ModelHierarchyDefinition definitionNode = new ModelHierarchyDefinition();
		definitionNode.setId(modelDefinition.getId());
		definitionNode.setParentId(modelDefinition.getParent());
		definitionNode.setAbstract(modelDefinition.isAbstract());
		definitionNode.setLabels(modelDefinition.getLabels());
		return definitionNode;
	}
}
