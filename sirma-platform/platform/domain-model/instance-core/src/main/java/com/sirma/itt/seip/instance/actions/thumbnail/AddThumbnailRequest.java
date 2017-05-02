package com.sirma.itt.seip.instance.actions.thumbnail;

import java.io.Serializable;

import com.sirma.itt.seip.instance.actions.ActionRequest;

/**
 * Contains data needed to execute 'add thumbnail' operation.
 *
 * @author A. Kunchev
 */
public class AddThumbnailRequest extends ActionRequest {

	private static final long serialVersionUID = -1558592671748057453L;

	protected static final String OPERATION_NAME = "addThumbnail";

	private Serializable thumbnailObjectId;

	/**
	 * @return the thumbnailObjectId
	 */
	public Serializable getThumbnailObjectId() {
		return thumbnailObjectId;
	}

	/**
	 * @param thumbnailObjectId
	 *            the thumbnailObjectId to set
	 */
	public void setThumbnailObjectId(Serializable thumbnailObjectId) {
		this.thumbnailObjectId = thumbnailObjectId;
	}

	@Override
	public String getOperation() {
		return OPERATION_NAME;
	}

}
