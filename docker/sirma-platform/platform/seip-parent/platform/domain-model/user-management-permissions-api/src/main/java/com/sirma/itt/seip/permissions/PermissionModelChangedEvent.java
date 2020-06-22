package com.sirma.itt.seip.permissions;

import java.util.Collection;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * Event that is fired on change of permission model or of the assignments to an instance
 *
 * @author bbanchev
 */
@Documentation("Fired on model/assigment change. Current PermissionModel and added/removed/updated roles are provided as arguments")
public class PermissionModelChangedEvent extends AbstractInstanceEvent<InstanceReference> {

	private final Collection<PermissionAssignmentChange> changesSet;

	/**
	 * Constructs new event with the given params.
	 *
	 * @param instance
	 *            is the instance updated during this change
	 * @param changesSet
	 *            is the permission change set - set of changed resourcerole entities with information how exactly model
	 *            is changed
	 */
	public PermissionModelChangedEvent(InstanceReference instance, Collection<PermissionAssignmentChange> changesSet) {
		super(instance);
		this.changesSet = changesSet;
	}

	/**
	 * Getter method for changeSet.
	 *
	 * @return the changeSet
	 */
	public Collection<PermissionAssignmentChange> getChangesSet() {
		return changesSet;
	}

	@Override
	public String toString() {
		return "PermissionModelChangedEvent [changesSet=" + changesSet + ", instance=" + instance + "]";
	}

}
