package com.sirma.itt.seip.instance.actions.relations;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.util.Map.Entry;
import java.util.Set;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;

/**
 * Action implementation that can execute the {@code removeRelation} operation.
 *
 * @author BBonev
 */
@Extension(target = Action.TARGET_NAME, order = 70)
public class RemoveRelationAction extends AbstractRelationAction<RemoveRelationRequest> {

	@Override
	public String getName() {
		return RemoveRelationRequest.OPERATION_NAME;
	}

	@Override
	public void validate(RemoveRelationRequest request) {
		super.validate(request);
		verifyRelationsAreDefined(request.getTargetReference().toInstance(), request.getRelations().keySet());
	}

	@Override
	public Instance performAction(RemoveRelationRequest request) {
		if (isEmpty(request.getRelations())) {
			throw new BadRequestException("Invalid request");
		}

		Instance instance = request.getTargetReference().toInstance();
		DefinitionModel definitionModel = definitionService.getInstanceDefinition(instance);

		for (Entry<String, Set<String>> entry : request.getRelations().entrySet()) {
			String property = entry.getKey();
			Set<String> relations = entry.getValue();
			definitionModel
					.getField(property)
					.ifPresent(propertyDefinition -> removeRelation(instance, propertyDefinition.getName(), relations));
		}
		return instance;
	}
}
