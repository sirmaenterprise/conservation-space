/**
 *
 */
package com.sirma.itt.seip.resources.downloads;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.resources.event.UserAssociationDeletedEvent;

/**
 * Event fired to notify that the given user removed all instances from his downloads.
 *
 * @author A. Kunchev
 */
@Documentation("Event fired to notify that the given user removed all instances from his downloads.")
public class DeletedAllFromDownloadsEvent extends UserAssociationDeletedEvent {

	/**
	 * Instantiates remove all from downloads event.
	 *
	 * @param user
	 *            reference of user instance for, which will be removed all downloads
	 */
	public DeletedAllFromDownloadsEvent(InstanceReference user) {
		super(user, null);
	}

}
