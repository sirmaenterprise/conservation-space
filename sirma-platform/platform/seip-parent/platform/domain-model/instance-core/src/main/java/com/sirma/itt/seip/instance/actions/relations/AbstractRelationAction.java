package com.sirma.itt.seip.instance.actions.relations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.instance.actions.ActionRequest;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;

/**
 * Abstract class for all relation operation. Contains helpful methods to interact with relations.
 *
 * @author Boyan Tonchev.
 */
public abstract class AbstractRelationAction<A extends ActionRequest> implements Action<A> {

	@Inject
	private DomainInstanceService instanceService;

	@Inject
	protected DefinitionService definitionService;

	@Override
	public void validate(A request) {
		if (request == null) {
			throw new BadRequestException("Invalid request");
		}
		Serializable instanceId = request.getTargetId();
		if (instanceId == null || StringUtils.isBlank((String) instanceId)) {
			throw new BadRequestException("Invalid request");
		}
	}

	@Override
	public Object perform(A request) {
		Instance instance = performAction(request);
		// Save of instance will trigger creation of new version.
		// We disabled audit log because move operation is already triggered/logged it.
		// return new instance data. when the instance is serialized the added relations should be returned as well
		return Options.DISABLE_AUDIT_LOG.wrap(
				() -> instanceService.save(InstanceSaveContext.create(instance, request.toOperation()))).get();
	}

	/**
	 * Real execution of action. After execution, returned instance will be saved.
	 *
	 * @param request the request to execute
	 * @return the result processed instance, should not be <code>null</code>.
	 */
	protected abstract Instance performAction(A request);

	/**
	 * Add relations <code>newRelations</code> to <code>instance</code>.
	 *
	 * @param instance - the instance of relation.
	 * @param propertyName - property name where <code>newRelations</code> have to be added.
	 * @param newRelations - set with target ids of relation.
	 * @param isRemoveExisting - if true all relation with <code>propertyName</code> will be removed before new one
	 *        added.
	 * @param isRelationMultiValue - have to be true if property of relation is single value.
	 */
	protected static void addRelation(Instance instance, String propertyName, Set<String> newRelations,
			boolean isRemoveExisting, boolean isRelationMultiValue) {

		Collection<String> references = instance.getAsCollection(propertyName, HashSet::new);
		if (isRemoveExisting || !isRelationMultiValue) {
			references.clear();
		}
		references.addAll(newRelations);
		if (references.size() == 1 && !isRelationMultiValue) {
			// for single value property add it as single value
			// if not done, this cause problems in other parts of the application where for non multi value fields
			// there is a collection (expressions, scripts)
			instance.add(propertyName, references.iterator().next());
		} else {
			// the code can handle only Lists and not Sets
			instance.add(propertyName, new ArrayList<>(references));
		}
	}

	/**
	 * Remove relations in property with <code>propertyName</code> between <code>instance</code> and instances from
	 * <code>linkTo</code>.
	 *
	 * @param instance - the instance of relation.
	 * @param propertyName - property name where <code>linkTo</code> have to be removed.
	 * @param linkTo set with instance ids which relations have to be removed.
	 */
	protected static void removeRelation(Instance instance, String propertyName, Set<String> linkTo) {
		Serializable oldValue = instance.remove(propertyName);
		if (oldValue instanceof Collection) {
			Collection<?> collection = (Collection<?>) oldValue;

			if (collection.removeAll(linkTo)) {
				// if we had values to remove at all that matches the one we need to remove
				// and if the collection is not empty we should return the remaining data back the instance
				if (!collection.isEmpty()) {
					instance.add(propertyName, oldValue);
				}
			} else {
				// if the collection was no modified we should return it back to the instance
				instance.add(propertyName, oldValue);
			}
		} else if (!(oldValue instanceof String && linkTo.contains(oldValue))) {
			// if the single value does not match any of the requested for removal we should put it back
			instance.add(propertyName, oldValue);
		}
	}

	protected Instance getInstance(Serializable instanceId) {
		return instanceService.loadInstance(instanceId.toString());
	}
}
