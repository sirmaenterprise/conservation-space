package com.sirma.sep.model.management.hierarchy;

import java.util.List;

/**
 * Hierarchy node for a semantic class. Contains the definition sub types.
 *
 * @author Mihail Radkov
 */
public class ModelHierarchyClass extends ModelHierarchyNode {

	private List<ModelHierarchyDefinition> subTypes;

	public List<ModelHierarchyDefinition> getSubTypes() {
		return subTypes;
	}

	public void setSubTypes(List<ModelHierarchyDefinition> subTypes) {
		this.subTypes = subTypes;
	}
}
