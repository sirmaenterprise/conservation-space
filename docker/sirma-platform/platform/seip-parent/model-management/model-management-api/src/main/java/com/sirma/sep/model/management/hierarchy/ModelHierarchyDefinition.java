package com.sirma.sep.model.management.hierarchy;

import com.sirma.sep.model.management.ModelDefinition;

/**
 * Model hierarchy node proxying {@link ModelDefinition}.
 *
 * @author Mihail Radkov
 */
public class ModelHierarchyDefinition extends ModelHierarchyNode<ModelDefinition> {

	public ModelHierarchyDefinition(ModelDefinition modelDefinition) {
		super(modelDefinition);
	}

	public boolean isAbstract() {
		return modelNode.isAbstract();
	}

}
