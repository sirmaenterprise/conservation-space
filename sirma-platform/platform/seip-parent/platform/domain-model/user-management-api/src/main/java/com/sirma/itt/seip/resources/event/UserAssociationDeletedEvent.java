/**
 *
 */
package com.sirma.itt.seip.resources.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.InstanceReference;

/**
 * Event fired to notify that the given user deleted association with some resource. This association is usually some
 * relation between the user and the resource.
 *
 * @author A. Kunchev
 */
@Documentation("Event fired to notify that the given user deleted association with some resource. This association is"
		+ " usually some relation between the user and the resource.")
public class UserAssociationDeletedEvent extends UserAssociationEvent {

	/**
	 * Instantiates user association deleted event.
	 *
	 * @param user
	 *            user instance reference
	 * @param to
	 *            instance reference of the resource
	 */
	public UserAssociationDeletedEvent(InstanceReference user, InstanceReference to) {
		super(user, to);
	}

}
