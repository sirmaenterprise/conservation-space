package com.sirma.sep.model.management.hierarchy;

import java.util.Map;

import com.sirma.sep.model.ModelNode;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Basic model hierarchy node proxying {@link ModelNode}.
 *
 * @author Mihail Radkov
 */
public abstract class ModelHierarchyNode<M extends ModelNode> {

	@JsonIgnore
	protected final M modelNode;

	public ModelHierarchyNode(M modelNode) {
		this.modelNode = modelNode;
	}

	public String getId() {
		return modelNode.getId();
	}

	public String getParentId() {
		return modelNode.getParent();
	}

	public Map<String, String> getLabels() {
		return modelNode.getLabels();
	}
}
