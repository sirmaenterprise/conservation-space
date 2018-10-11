package com.sirma.sep.model.management.hierarchy;

import java.util.Map;

/**
 * Basic model hierarchy node holding information about a node and its parent if it has one.
 *
 * @author Mihail Radkov
 */
public abstract class ModelHierarchyNode {

	private String id;

	private String parentId;

	private Map<String, String> labels;

	public String getId() {
		return id;
	}

	public ModelHierarchyNode setId(String id) {
		this.id = id;
		return this;
	}

	public String getParentId() {
		return parentId;
	}

	public ModelHierarchyNode setParentId(String parentId) {
		this.parentId = parentId;
		return this;
	}

	public Map<String, String> getLabels() {
		return labels;
	}

	public void setLabels(Map<String, String> labels) {
		this.labels = labels;
	}
}
