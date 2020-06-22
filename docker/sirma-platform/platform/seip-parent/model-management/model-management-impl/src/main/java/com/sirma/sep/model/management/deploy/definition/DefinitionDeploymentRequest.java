package com.sirma.sep.model.management.deploy.definition;

import java.util.List;

import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.sep.model.management.ModelDefinition;
import com.sirma.sep.model.management.Models;
import com.sirma.sep.model.management.operation.ModelChangeSetInfo;

/**
 * Data transfer object for applying {@link ModelChangeSetInfo}
 *
 * @author Mihail Radkov
 */
class DefinitionDeploymentRequest {

	private final Models models;

	private final ModelDefinition definition;

	private final GenericDefinition genericDefinition;

	private final List<ModelChangeSetInfo> changes;

	private final DefinitionDeploymentContext context;

	/**
	 * Instantiates a new DTO with the provided models and changes.
	 *
	 * @param models the deployed models instance
	 * @param definition the model definition that have been updated
	 * @param genericDefinition the generic definition that should be updated with the changes
	 * @param changes the model changes
	 * @param context change set context
	 */
	public DefinitionDeploymentRequest(Models models, ModelDefinition definition, GenericDefinition genericDefinition,
			List<ModelChangeSetInfo> changes, DefinitionDeploymentContext context) {
		this.models = models;
		this.definition = definition;
		this.genericDefinition = genericDefinition;
		this.changes = changes;
		this.context = context;
	}

	public Models getModels() {
		return models;
	}

	public ModelDefinition getDefinition() {
		return definition;
	}

	public GenericDefinition getGenericDefinition() {
		return genericDefinition;
	}

	public List<ModelChangeSetInfo> getChanges() {
		return changes;
	}

	public DefinitionDeploymentContext getContext() {
		return context;
	}
}
