package com.sirma.sep.model.management.deploy.definition.steps;

import java.util.List;

import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.sep.model.management.ModelDefinition;
import com.sirma.sep.model.management.Models;
import com.sirma.sep.model.management.deploy.ModelChangeSetExtension;
import com.sirma.sep.model.management.deploy.definition.DefinitionDeploymentContext;

/**
 * Data transfer object supplied to {@link DefinitionChangeSetStep#handle(DefinitionChangeSetPayload)}.
 *
 * @author Mihail Radkov
 */
public class DefinitionChangeSetPayload {
	private final Models models;
	private final ModelDefinition definition;
	private final GenericDefinition genericDefinition;
	private final DefinitionDeploymentContext context;
	private final List<ModelChangeSetExtension> changes;

	/**
	 * Instantiates the DTO with the provided models and changes.
	 *
	 * @param models the current models instance that is being deployed
	 * @param definition the model definition that have been updated
	 * @param genericDefinition the generic definition to update
	 * @param context change set context
	 * @param changes the list of all change made to {@link #definition}
	 */
	public DefinitionChangeSetPayload(Models models, ModelDefinition definition,
			GenericDefinition genericDefinition, DefinitionDeploymentContext context,
			List<ModelChangeSetExtension> changes) {
		this.models = models;
		this.definition = definition;
		this.genericDefinition = genericDefinition;
		this.context = context;
		this.changes = changes;
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

	public DefinitionDeploymentContext getContext() {
		return context;
	}

	public List<ModelChangeSetExtension> getChanges() {
		return changes;
	}

	public ModelChangeSetExtension getFirstChange() {
		return getChanges().get(0);
	}

	public ModelChangeSetExtension getLastChange() {
		return getChanges().get(getChanges().size() - 1);
	}
}
