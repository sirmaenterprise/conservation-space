package com.sirma.itt.seip.instance.actions.relations;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.rest.BadRequestException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Action implementation that can execute the {@code addRelation} operation.
 *
 * @author BBonev
 */
@Extension(target = Action.TARGET_NAME, order = 60)
public class AddRelationAction implements Action<AddRelationRequest> {

	@Inject
	private InstanceTypeResolver resolver;

	@Inject
	private DomainInstanceService instanceService;

	@Inject
	private DictionaryService dictionaryService;

	@Override
	public String getName() {
		return AddRelationRequest.OPERATION_NAME;
	}

	@Override
	public Object perform(AddRelationRequest request) {
		if (request == null) {
			throw new BadRequestException("Invalid request");
		}

		InstanceReference source = request.getTargetReference();
		if (source == null) {
			source = resolver.resolveReference(request.getTargetId()).orElseThrow(
					() -> new BadRequestException("Could not load target instance"));
		}
		Instance instance = source.toInstance();
		DefinitionModel definitionModel = dictionaryService.getInstanceDefinition(instance);

		for (Entry<String, Set<String>> entry : request.getRelations().entrySet()) {
			String property = entry.getKey();

			Optional<PropertyDefinition> relationDefinition = definitionModel
					.findField(PropertyDefinition.hasName(property).or(PropertyDefinition.hasUri(property)));

			String propName = relationDefinition.map(PropertyDefinition::getName).orElse(property);
			// if the field is not found in the model then we should not place restrictions to the relation data
			Boolean isMultivalue = relationDefinition.map(PropertyDefinition::isMultiValued).orElse(Boolean.TRUE);

			addRelation(instance, propName, entry.getValue(), request, isMultivalue.booleanValue());
		}

		// return new instance data. when the instance is serialized the added relations should be returned as well
		Options.DISABLE_AUDIT_LOG.enable();
		Instance newInstance = instanceService.save(InstanceSaveContext.create(instance, request.toOperation()));
		Options.DISABLE_AUDIT_LOG.disable();
		return newInstance;
	}

	private static void addRelation(Instance source, String relationId, Set<String> newRelations,
			AddRelationRequest request, boolean isRelationMultivalue) {

		Collection<String> references = source.getAsCollection(relationId, HashSet::new);
		if (request.isRemoveExisting() || !isRelationMultivalue) {
			references.clear();
		}
		references.addAll(newRelations);
		if (references.size() == 1 && !isRelationMultivalue) {
			// for single value property add it as single value
			// if not done, this cause problems in other parts of the application where for non multi value fields
			// there is a collection (expressions, scripts)
			source.add(relationId, references.iterator().next());
		} else {
			source.add(relationId, (Serializable) references);
		}
	}

}
