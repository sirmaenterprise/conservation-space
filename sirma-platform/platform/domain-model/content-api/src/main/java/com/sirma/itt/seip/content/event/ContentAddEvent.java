/**
 *
 */
package com.sirma.itt.seip.content.event;

import java.io.Serializable;

import com.sirma.itt.seip.content.Content;

/**
 * Event fired to notify that new content has been added and will be saved.
 *
 * @author BBonev
 */
public class ContentAddEvent extends ContentEvent {

	/**
	 * Instantiates a new content add event.
	 *
	 * @param owner
	 *            the owner
	 * @param newContent
	 *            the new content
	 */
	public ContentAddEvent(Serializable owner, Content newContent) {
		super(owner, newContent);
	}

}
