package com.sirma.itt.seip.instance.actions.relations;

import java.util.Collection;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Action implementation that can execute the {@code removeRelation} and {@code addRelation} operation.
 *
 * @author Boyan Tonchev.
 */
@Extension(target = Action.TARGET_NAME, order = 61)
public class UpdateRelationsAction extends AbstractRelationAction<UpdateRelationsRequest> {

	@Override
	public Instance performAction(UpdateRelationsRequest request) {
		Instance instance = getInstance(request.getTargetId());
		addRelations(instance, request.getLinksToBeAdded());
		removeRelations(instance, request.getLinksToBeRemoved());
		return instance;
	}

	@Override
	public String getName() {
		return UpdateRelationsRequest.OPERATION_NAME;
	}

	private void addRelations(Instance instance, Collection<UpdateRelationData> data) {
		DefinitionModel definitionModel = definitionService.getInstanceDefinition(instance);
		for (UpdateRelationData updateRelationData : data) {
			String linkId = updateRelationData.getLinkId();
			definitionModel.findField(PropertyDefinition.hasUri(linkId))
			.ifPresent(propertyDefinition -> addRelation(instance, propertyDefinition.getName(),
					updateRelationData.getInstances(), false,
					propertyDefinition.isMultiValued()));
		}
	}

	private void removeRelations(Instance instance, Collection<UpdateRelationData> data) {
		DefinitionModel definitionModel = definitionService.getInstanceDefinition(instance);
		for (UpdateRelationData updateRelationData : data) {
			String linkId = updateRelationData.getLinkId();
			definitionModel.findField(PropertyDefinition.hasUri(linkId))
			.ifPresent(propertyDefinition -> removeRelation(instance, propertyDefinition.getName(),
					updateRelationData.getInstances()));
		}
	}
}