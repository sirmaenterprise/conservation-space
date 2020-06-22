package com.sirma.sep.model;

import java.util.List;

/**
 * Carries the parameters needed for a model export.
 * 
 * @author Vilizar Tsonev
 */
public class ModelExportRequest {

	private boolean allTemplates;

	private boolean allDefinitions;

	private List<String> templates;

	private List<String> definitions;

	public boolean isAllTemplates() {
		return allTemplates;
	}

	public void setAllTemplates(boolean allTemplates) {
		this.allTemplates = allTemplates;
	}

	public boolean isAllDefinitions() {
		return allDefinitions;
	}

	public void setAllDefinitions(boolean allDefinitions) {
		this.allDefinitions = allDefinitions;
	}

	public List<String> getTemplates() {
		return templates;
	}

	public void setTemplates(List<String> templateIds) {
		this.templates = templateIds;
	}

	public List<String> getDefinitions() {
		return definitions;
	}

	public void setDefinitions(List<String> definitionIds) {
		this.definitions = definitionIds;
	}
}
