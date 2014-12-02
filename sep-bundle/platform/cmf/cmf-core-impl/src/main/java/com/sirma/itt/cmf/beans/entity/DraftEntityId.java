package com.sirma.itt.cmf.beans.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * The DraftEntityId is the primary key id entity for draft objects
 */
@Embeddable
public class DraftEntityId implements Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -4631585819012361879L;

	/** The instance that this is draft for. */
	@Column(name = "instanceid", nullable = false)
	private String instanceId;
	/** The creator of draft. It is the resource primary id. */
	@Column(name = "userid", nullable = false)
	private String userId;

	/**
	 * Getter method for instanceId.
	 *
	 * @return the instanceId
	 */
	public String getInstanceId() {
		return instanceId;
	}

	/**
	 * Setter method for instanceId.
	 *
	 * @param instanceId
	 *            the instanceId to set
	 */
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	/**
	 * Getter method for userId.
	 *
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Setter method for userId.
	 *
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

}
