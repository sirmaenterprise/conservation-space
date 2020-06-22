/**
 *
 */
package com.sirma.itt.seip.resources.downloads;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.resources.event.UserAssociationDeletedEvent;

/**
 * Event fired to notify that the given user removed some instance from his downloads.
 *
 * @author A. Kunchev
 */
@Documentation(" Event fired to notify that the given user removed some instance from his downloads.")
public class DeletedFromDownloadsEvent extends UserAssociationDeletedEvent {

	/**
	 * Instantiates remove from downloads event.
	 *
	 * @param user
	 *            reference of user instance
	 * @param to
	 *            reference of the instance, which was removed from the user downloads
	 */
	public DeletedFromDownloadsEvent(InstanceReference user, InstanceReference to) {
		super(user, to);
	}

}
