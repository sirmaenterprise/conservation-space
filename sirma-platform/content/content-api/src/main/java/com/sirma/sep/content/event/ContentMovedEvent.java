package com.sirma.sep.content.event;

import com.sirma.itt.seip.event.EmfEvent;

/**
 * Fired when content is moved between content stores
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/05/2018
 */
public class ContentMovedEvent implements EmfEvent {
	private final String contentId;
	private final String destinationStoreName;

	public ContentMovedEvent(String contentId, String destinationStoreName) {
		this.contentId = contentId;
		this.destinationStoreName = destinationStoreName;
	}

	public String getContentId() {
		return contentId;
	}

	public String getDestinationStoreName() {
		return destinationStoreName;
	}
}
