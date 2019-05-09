package com.sirma.itt.seip.instance.actions.relations;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Action implementation that can execute the {@code addRelation} operation.
 *
 * @author BBonev
 */
@Extension(target = Action.TARGET_NAME, order = 60)
public class AddRelationAction extends AbstractRelationAction<AddRelationRequest> {

	@Override
	public String getName() {
		return AddRelationRequest.OPERATION_NAME;
	}

	@Override
	public void validate(AddRelationRequest request) {
		super.validate(request);
		verifyRelationsAreDefined(request.getTargetReference().toInstance(), request.getRelations().keySet());
	}

	@Override
	public Instance performAction(AddRelationRequest request) {
		// check instance permissions
		Instance instance = request.getTargetReference().toInstance();
		DefinitionModel definitionModel = definitionService.getInstanceDefinition(instance);

		for (Entry<String, Set<String>> entry : request.getRelations().entrySet()) {
			String property = entry.getKey();

			Optional<PropertyDefinition> relationDefinition = definitionModel.getField(property);

			String propName = relationDefinition.map(PropertyDefinition::getName).orElse(property);
			// if the field is not found in the model then we should not place restrictions to the relation data
			Boolean isMultiValue = relationDefinition.map(PropertyDefinition::isMultiValued).orElse(Boolean.TRUE);

			addRelation(instance, propName, entry.getValue(), request.isRemoveExisting(), isMultiValue);
		}
		return instance;
	}
}
