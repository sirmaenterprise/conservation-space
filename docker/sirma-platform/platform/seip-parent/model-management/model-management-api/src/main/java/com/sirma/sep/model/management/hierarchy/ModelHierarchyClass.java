package com.sirma.sep.model.management.hierarchy;

import java.util.LinkedList;
import java.util.List;

import com.sirma.sep.model.management.ModelClass;

/**
 * Hierarchy node proxying a {@link ModelClass}. Contains list of related {@link ModelHierarchyDefinition} as sub types.
 *
 * @author Mihail Radkov
 */
public class ModelHierarchyClass extends ModelHierarchyNode<ModelClass> {

	private List<ModelHierarchyDefinition> subTypes;

	public ModelHierarchyClass(ModelClass modelClass) {
		super(modelClass);
	}

	public List<ModelHierarchyDefinition> getSubTypes() {
		if (subTypes == null) {
			subTypes = new LinkedList<>();
		}
		return subTypes;
	}

	public void setSubTypes(List<ModelHierarchyDefinition> subTypes) {
		this.subTypes = subTypes;
	}
}
