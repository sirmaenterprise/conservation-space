package com.sirma.itt.cmf.beans.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.sirma.itt.emf.security.model.User;

/**
 * The DraftInstance is a model class representing the draft bean loaded from db.
 * 
 * @author bbanchev
 */
public class DraftInstance {

	/** The instance id. */
	private String instanceId;

	/** The created on. */
	private Date createdOn;
	/** The user. */
	private User creator;
	/** The draft content. */
	private String draftContent;

	/** The draft properties. */
	private Map<String, Serializable> draftProperties = new HashMap<>();

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
	 * Gets the created on.
	 * 
	 * @return the created on
	 */
	public Date getCreatedOn() {
		return createdOn;
	}

	/**
	 * Sets the created on.
	 * 
	 * @param createdOn
	 *            the new created on
	 */
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	/**
	 * Gets the draft content.
	 * 
	 * @return the draft content
	 */
	public String getDraftContent() {
		return draftContent;
	}

	/**
	 * Sets the draft content.
	 * 
	 * @param draftContent
	 *            the new draft content
	 */
	public void setDraftContent(String draftContent) {
		this.draftContent = draftContent;
	}

	/**
	 * Gets the draft properties.
	 * 
	 * @return the draft properties
	 */
	public Map<String, Serializable> getDraftProperties() {
		return draftProperties;
	}

	/**
	 * Sets the draft properties.
	 * 
	 * @param draftProperties
	 *            the draft properties
	 */
	public void setDraftProperties(Map<String, Serializable> draftProperties) {
		this.draftProperties = draftProperties;
	}

	/**
	 * Getter method for creator.
	 * 
	 * @return the creator
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * Setter method for creator.
	 * 
	 * @param creator
	 *            the creator to set
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

}
