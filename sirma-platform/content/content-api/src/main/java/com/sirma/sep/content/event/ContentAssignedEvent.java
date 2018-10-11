package com.sirma.sep.content.event;

import java.io.Serializable;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.event.EmfEvent;

/**
 * Event fired to notify that instance has been assigned to a content.
 *
 * @author BBonev
 */
@Documentation("Event fired to notify that instance has been assigned to a content.")
public class ContentAssignedEvent implements EmfEvent {

	private final Serializable instanceId;
	private final String contentId;

	/**
	 * Instantiates a new content assigned event.
	 *
	 * @param instanceId
	 *            the instance id
	 * @param contentId
	 *            the content id
	 */
	public ContentAssignedEvent(Serializable instanceId, String contentId) {
		this.instanceId = instanceId;
		this.contentId = contentId;
	}

	/**
	 * @return the instanceId that is being assigned to
	 */
	public Serializable getInstanceId() {
		return instanceId;
	}

	/**
	 * @return the contentId that was assigned to instance
	 */
	public String getContentId() {
		return contentId;
	}
}
