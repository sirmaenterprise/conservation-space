package com.sirma.sep.model.management.hierarchy;

/**
 * Model hierarchy node representing a definition model and information about it.
 *
 * @author Mihail Radkov
 */
public class ModelHierarchyDefinition extends ModelHierarchyNode {

	private boolean isAbstract;

	public boolean isAbstract() {
		return isAbstract;
	}

	public void setAbstract(boolean anAbstract) {
		isAbstract = anAbstract;
	}

}
