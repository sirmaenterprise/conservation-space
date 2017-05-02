/**
 *
 */
package com.sirma.itt.seip.resources.favorites;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.resources.event.UserAssociationAddedEvent;

/**
 * Event fired to notify that the given user added some instance to his favourites.
 *
 * @author A. Kunchev
 */
@Documentation("Event fired to notify that the given user added some instance to his favourites.")
public class FavouriteAddedEvent extends UserAssociationAddedEvent {

	/**
	 * Instantiates add to favourites event.
	 *
	 * @param user
	 *            reference of user instance
	 * @param to
	 *            reference of the instance, which was added to the user favourites
	 */
	public FavouriteAddedEvent(InstanceReference user, InstanceReference to) {
		super(user, to);
	}

}
