package com.sirmaenterprise.sep.models;

import java.util.List;

/**
 * Serves as a data holder for collections of imported models.
 * 
 * @author Vilizar Tsonev
 */
public class ImportedModelsInfo {

	List<ImportedDefinition> definitions;

	List<ImportedTemplate> templates;

	/**
	 * Constructs the ImportedModelsInfo.
	 * 
	 * @param definitions the list of imported definitions
	 * @param templates the list of imoprted templates
	 */
	public ImportedModelsInfo(List<ImportedDefinition> definitions, List<ImportedTemplate> templates) {
		super();
		this.definitions = definitions;
		this.templates = templates;
	}

	public List<ImportedDefinition> getDefinitions() {
		return definitions;
	}

	public void setDefinitions(List<ImportedDefinition> definitions) {
		this.definitions = definitions;
	}

	public List<ImportedTemplate> getTemplates() {
		return templates;
	}

	public void setTemplates(List<ImportedTemplate> templates) {
		this.templates = templates;
	}
}
