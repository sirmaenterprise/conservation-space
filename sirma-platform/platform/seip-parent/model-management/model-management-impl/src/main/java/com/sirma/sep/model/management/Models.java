package com.sirma.sep.model.management;

import com.sirma.sep.model.management.hierarchy.ModelHierarchyClass;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;

import java.util.List;
import java.util.Map;

/**
 * Used to hold converted models - semantic class & property models, definition models and their hierarchy.
 *
 * @author Mihail Radkov
 */
public class Models {

	private Map<String, ModelClass> classes;

	private Map<String, ModelProperty> properties;

	private Map<String, ModelDefinition> definitions;

	private List<ModelHierarchyClass> modelHierarchy;

	private ModelsMetaInfo modelsMetaInfo;

	public Map<String, ModelClass> getClasses() {
		return classes;
	}

	public Map<String, ModelProperty> getProperties() {
		return properties;
	}

	public Map<String, ModelDefinition> getDefinitions() {
		return definitions;
	}

	public void setClasses(Map<String, ModelClass> classes) {
		this.classes = classes;
	}

	public void setProperties(Map<String, ModelProperty> properties) {
		this.properties = properties;
	}

	public void setDefinitions(Map<String, ModelDefinition> definitions) {
		this.definitions = definitions;
	}

	public List<ModelHierarchyClass> getModelHierarchy() {
		return modelHierarchy;
	}

	public void setModelHierarchy(List<ModelHierarchyClass> modelHierarchy) {
		this.modelHierarchy = modelHierarchy;
	}

	public ModelsMetaInfo getModelsMetaInfo() {
		return modelsMetaInfo;
	}

	public void setModelsMetaInfo(ModelsMetaInfo modelsMetaInfo) {
		this.modelsMetaInfo = modelsMetaInfo;
	}

	/**
	 * Determines if it is holding any models or not.
	 *
	 * @return true if there are no models or false otherwise
	 */
	public boolean isEmpty() {
		return classes == null || classes.size() < 1;
	}
}
