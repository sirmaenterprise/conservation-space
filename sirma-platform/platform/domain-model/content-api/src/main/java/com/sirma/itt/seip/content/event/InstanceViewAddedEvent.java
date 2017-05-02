package com.sirma.itt.seip.content.event;

import java.io.Serializable;

import com.sirma.itt.seip.content.Content;

/**
 * Event fired to notify for new instance view being added. The event provides access to parsed instance of the view.
 * Note that changes to this view will not be reflected to the actual content. To modify the view before save add an
 * extension implementation of {@link com.sirma.itt.seip.content.InstanceViewPreProcessor}.
 *
 * @author BBonev
 */
public class InstanceViewAddedEvent extends InstanceViewEvent {

	/**
	 * Instantiates a new instance view added event.
	 *
	 * @param owner the owner
	 * @param newView the new view
	 */
	public InstanceViewAddedEvent(Serializable owner, Content newView) {
		super(owner, newView);
	}
}
