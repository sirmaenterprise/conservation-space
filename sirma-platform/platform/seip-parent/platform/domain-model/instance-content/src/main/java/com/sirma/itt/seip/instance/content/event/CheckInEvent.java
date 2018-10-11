package com.sirma.itt.seip.instance.content.event;

import java.io.Serializable;

import com.sirma.sep.content.Content;
import com.sirma.sep.content.event.ContentEvent;

/**
 * Notifies that new content has been checked-in and will be updated in the semantics.
 *
 * @author Vilizar Tsonev
 */
public class CheckInEvent extends ContentEvent {

	/**
	 * Constructs the event.
	 *
	 * @param owner
	 *            is the owner instance
	 * @param checkedInContent
	 *            is the content for check-in
	 */
	public CheckInEvent(Serializable owner, Content checkedInContent) {
		super(owner, checkedInContent);
	}

}
