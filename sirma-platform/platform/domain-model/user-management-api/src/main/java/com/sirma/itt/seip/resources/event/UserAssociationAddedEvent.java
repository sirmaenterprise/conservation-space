/**
 *
 */
package com.sirma.itt.seip.resources.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.InstanceReference;

/**
 * Event fired to notify that the given user was associated with some other resource. This association is usually done
 * by some relation between the user and the resource.
 *
 * @author A. Kunchev
 */
@Documentation("Event fired to notify that the given user was associated with some other resource. This association"
		+ " is usually done by some relation between the user and the resource.")
public class UserAssociationAddedEvent extends UserAssociationEvent {

	/**
	 * Instantiates user association added event.
	 *
	 * @param user
	 *            user instance reference
	 * @param to
	 *            instance reference of the resource
	 */
	public UserAssociationAddedEvent(InstanceReference user, InstanceReference to) {
		super(user, to);
	}

}
