package com.sirma.itt.seip.instance.actions.relations;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map.Entry;
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
 * Action implementation that can execute the {@code removeRelation} operation.
 *
 * @author BBonev
 */
@Extension(target = Action.TARGET_NAME, order = 70)
public class RemoveRelationAction implements Action<RemoveRelationRequest> {

	@Inject
	private InstanceTypeResolver resolver;

	@Inject
	private DomainInstanceService instanceService;

	@Inject
	private DictionaryService dictionaryService;

	@Override
	public String getName() {
		return RemoveRelationRequest.OPERATION_NAME;
	}

	@Override
	public Object perform(RemoveRelationRequest request) {
		if (request == null || isEmpty(request.getRelations())) {
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

			String propertyName = definitionModel
					.findField(PropertyDefinition.hasName(property).or(PropertyDefinition.hasUri(property)))
						.map(PropertyDefinition::getName)
						.orElse(property);

			removeRelation(instance, propertyName, entry.getValue());
		}

		// return new instance data. when the instance is serialized the added relations should be returned as well
		Options.DISABLE_AUDIT_LOG.enable();
		Instance newInstance = instanceService.save(InstanceSaveContext.create(instance, request.toOperation()));
		Options.DISABLE_AUDIT_LOG.disable();
		return newInstance;
	}

	private static void removeRelation(Instance source, String relationId, Set<String> linkTo) {
		Serializable oldValue = source.remove(relationId);
		if (oldValue instanceof Collection) {
			Collection<?> collection = (Collection<?>) oldValue;

			if (collection.removeAll(linkTo)) {
				// if we had values to remove at all that matches the one we need to remove
				// and if the collection is not empty we should return the remaining data back the instance
				if (!collection.isEmpty()) {
					source.add(relationId, oldValue);
				}
			} else {
				// if the collection was no modified we should return it back to the instance
				source.add(relationId, oldValue);
			}
		} else if (!(oldValue instanceof String && linkTo.contains(oldValue))) {
			// if the single value does not match any of the requested for removal we should put it back
			source.add(relationId, oldValue);
		}
	}
}
