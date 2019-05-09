package com.sirma.sep.content.event;

import java.io.Serializable;

import com.sirma.sep.content.ContentImport;

/**
 * Event fired to notify that content has been imported.
 *
 * @author A. Kunchev
 */
public class ContentImportedEvent extends ContentEvent {

	private final Serializable contentId;

	public ContentImportedEvent(Serializable contentId, ContentImport imported) {
		super(imported.getInstanceId(), imported);
		this.contentId = contentId;
	}

	public Serializable getContentId() {
		return contentId;
	}
}