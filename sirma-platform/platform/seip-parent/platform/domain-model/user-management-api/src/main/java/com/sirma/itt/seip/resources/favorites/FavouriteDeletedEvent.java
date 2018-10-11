/**
 *
 */
package com.sirma.itt.seip.resources.favorites;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.resources.event.UserAssociationDeletedEvent;

/**
 * Event fired to notify that the given user removed some instance from his favourites.
 *
 * @author A. Kunchev
 */
@Documentation("Event fired to notify that the given user removed some instance from his favourites.")
public class FavouriteDeletedEvent extends UserAssociationDeletedEvent {

	/**
	 * Instantiates remove from favourites event.
	 *
	 * @param user
	 *            reference of user instance
	 * @param to
	 *            reference of the instance, which was removed from the user favourites
	 */
	public FavouriteDeletedEvent(InstanceReference user, InstanceReference to) {
		super(user, to);
	}

}
