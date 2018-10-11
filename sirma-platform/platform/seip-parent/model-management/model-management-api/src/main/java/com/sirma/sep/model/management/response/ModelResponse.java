package com.sirma.sep.model.management.response;

import com.sirma.sep.model.management.ModelClass;
import com.sirma.sep.model.management.ModelDefinition;

import java.util.List;

/**
 * Holds the corresponding models for given models request.
 *
 * @author Mihail Radkov
 */
public class ModelResponse {

	private List<ModelClass> classes;

	private List<ModelDefinition> definitions;

	public List<ModelClass> getClasses() {
		return classes;
	}

	public ModelResponse setClasses(List<ModelClass> classes) {
		this.classes = classes;
		return this;
	}

	public List<ModelDefinition> getDefinitions() {
		return definitions;
	}

	public ModelResponse setDefinitions(List<ModelDefinition> definitions) {
		this.definitions = definitions;
		return this;
	}
}
