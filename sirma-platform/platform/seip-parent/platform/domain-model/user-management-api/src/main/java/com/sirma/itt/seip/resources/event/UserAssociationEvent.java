/**
 *
 */
package com.sirma.itt.seip.resources.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.EmfEvent;

/**
 * Base event class, used for events fired to notify for some operation related to user associations.
 *
 * @author A. Kunchev
 */
@Documentation("Base event class, used for events fired to notify for some operation related to user associations.")
public abstract class UserAssociationEvent implements EmfEvent {

	private InstanceReference user;

	private InstanceReference to;

	/**
	 * Instantiates user association event.
	 *
	 * @param user
	 *            user reference of user instance
	 * @param to
	 *            instance reference of the resource
	 */
	public UserAssociationEvent(InstanceReference user, InstanceReference to) {
		this.user = user;
		this.to = to;
	}

	/**
	 * @return the user
	 */
	public InstanceReference getUser() {
		return user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(InstanceReference user) {
		this.user = user;
	}

	/**
	 * @return the to
	 */
	public InstanceReference getTo() {
		return to;
	}

	/**
	 * @param to
	 *            the to to set
	 */
	public void setTo(InstanceReference to) {
		this.to = to;
	}

}
