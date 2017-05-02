/**
 *
 */
package com.sirma.itt.seip.resources.downloads;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.resources.event.UserAssociationAddedEvent;

/**
 * Event fired to notify that the given user added some instance to his downloads.
 *
 * @author A. Kunchev
 */
@Documentation("Event fired to notify that the given user added some instance to his downloads.")
public class AddedToDownloadsEvent extends UserAssociationAddedEvent {

	/**
	 * Instantiates add to downloads event.
	 *
	 * @param user
	 *            reference of user instance
	 * @param to
	 *            reference of the instance, which was added to the user downloads
	 */
	public AddedToDownloadsEvent(InstanceReference user, InstanceReference to) {
		super(user, to);
	}

}
