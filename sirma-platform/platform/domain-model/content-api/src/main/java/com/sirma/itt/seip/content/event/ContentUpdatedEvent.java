package com.sirma.itt.seip.content.event;

import java.io.Serializable;

import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;

/**
 * Event fired to notify for new version of the generic content. The event provides access to the old and the new
 * version of the content.
 *
 * @author BBonev
 */
public class ContentUpdatedEvent extends ContentEvent {

	/** The old content. */
	private final ContentInfo oldContent;

	/**
	 * Instantiates a new content updated event.
	 *
	 * @param owner
	 *            the owner
	 * @param modifiedContent
	 *            the modified content
	 * @param oldContent
	 *            the old content
	 */
	public ContentUpdatedEvent(Serializable owner, Content modifiedContent, ContentInfo oldContent) {
		super(owner, modifiedContent);
		this.oldContent = oldContent;
	}

	/**
	 * Gets the old content.
	 *
	 * @return the old content
	 */
	public ContentInfo getOldContent() {
		return oldContent;
	}

}
