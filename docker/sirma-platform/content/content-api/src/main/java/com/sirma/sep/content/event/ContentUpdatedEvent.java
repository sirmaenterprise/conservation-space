package com.sirma.sep.content.event;

import java.io.Serializable;

import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;

/**
 * Event fired to notify for new version of the generic content. The event provides access to the
 * old and the new version of the content.
 *
 * @author BBonev
 */
public class ContentUpdatedEvent extends ContentEvent {

	private final ContentInfo oldContent;

	private final ContentInfo newContent;

	/**
	 * Instantiates a new content updated event.
	 *
	 * @param owner
	 *            the owner
	 * @param modifiedContent
	 *            the modified content
	 * @param oldContent
	 *            the old content
	 * @param newContent
	 *            the new content
	 */
	public ContentUpdatedEvent(Serializable owner, Content modifiedContent, ContentInfo oldContent,
			ContentInfo newContent) {
		super(owner, modifiedContent);
		this.oldContent = oldContent;
		this.newContent = newContent;
	}

	/**
	 * Gets the old content.
	 *
	 * @return the old content
	 */
	public ContentInfo getOldContent() {
		return oldContent;
	}

	/**
	 * Returns the new content.
	 * 
	 * @return the newContent
	 */
	public ContentInfo getNewContent() {
		return newContent;
	}

}
