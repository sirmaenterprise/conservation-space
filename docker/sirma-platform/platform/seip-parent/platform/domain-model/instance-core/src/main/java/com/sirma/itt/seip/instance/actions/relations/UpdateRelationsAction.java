package com.sirma.itt.seip.instance.actions.relations;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

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
	public void validate(UpdateRelationsRequest request) {
		super.validate(request);
		Instance instance = request.getTargetReference().toInstance();

		Set<String> relationsToAdd = request.getLinksToBeAdded()
				.stream()
				.map(UpdateRelationData::getLinkId)
				.collect(Collectors.toSet());
		verifyRelationsAreDefined(instance, relationsToAdd);

		Set<String> relationsToRemove = request.getLinksToBeRemoved()
				.stream()
				.map(UpdateRelationData::getLinkId)
				.collect(Collectors.toSet());
		verifyRelationsAreDefined(instance, relationsToRemove);

	}

	@Override
	public Instance performAction(UpdateRelationsRequest request) {
		Instance instance = request.getTargetReference().toInstance();
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
			definitionModel.getField(linkId)
					.ifPresent(propertyDefinition -> addRelation(instance, propertyDefinition.getName(),
							updateRelationData.getInstances(), false, propertyDefinition.isMultiValued()));
		}
	}

	private void removeRelations(Instance instance, Collection<UpdateRelationData> data) {
		DefinitionModel definitionModel = definitionService.getInstanceDefinition(instance);
		for (UpdateRelationData updateRelationData : data) {
			String linkId = updateRelationData.getLinkId();
			definitionModel.getField(linkId).ifPresent(propertyDefinition ->
					removeRelation(instance, propertyDefinition.getName(), updateRelationData.getInstances()));
		}
	}
}
