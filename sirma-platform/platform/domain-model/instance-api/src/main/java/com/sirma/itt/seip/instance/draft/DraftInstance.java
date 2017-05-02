package com.sirma.itt.seip.instance.draft;

import java.util.Date;

/**
 * The DraftInstance is a model class representing the draft bean loaded from db.
 *
 * @author bbanchev
 */
public class DraftInstance {

	private String instanceId;

	private String draftContentId;

	private String creator;

	private Date createdOn;

	/**
	 * Gets the instance id.
	 *
	 * @return the instance id
	 */
	public String getInstanceId() {
		return instanceId;
	}

	/**
	 * Sets the instance id.
	 *
	 * @param instanceId
	 *            the new instance id
	 */
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	/**
	 * Gets the draft content.
	 *
	 * @return the draft content id
	 */
	public String getDraftContentId() {
		return draftContentId;
	}

	/**
	 * Sets the draft content.
	 *
	 * @param draftContentId
	 *            the new draft content id
	 */
	public void setDraftContentId(String draftContentId) {
		this.draftContentId = draftContentId;
	}

	/**
	 * @return the creator
	 */
	public String getCreator() {
		return creator;
	}

	/**
	 * @param creator
	 *            the creator to set
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}

	/**
	 * @return the createdOn
	 */
	public Date getCreatedOn() {
		return createdOn;
	}

	/**
	 * @param createdOn
	 *            the createdOn to set
	 */
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

}
