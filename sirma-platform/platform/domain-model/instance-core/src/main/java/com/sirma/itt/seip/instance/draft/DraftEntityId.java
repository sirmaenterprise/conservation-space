package com.sirma.itt.seip.instance.draft;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.sirma.itt.seip.util.EqualsHelper;

/**
 * The DraftEntityId is the primary key id entity for draft objects
 */
@Embeddable
public class DraftEntityId implements Serializable {

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (instanceId == null ? 0 : instanceId.hashCode());
		result = prime * result + (userId == null ? 0 : userId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DraftEntityId)) {
			return false;
		}
		DraftEntityId other = (DraftEntityId) obj;
		if (!EqualsHelper.nullSafeEquals(instanceId, other.instanceId)) {
			return false;
		}
		return EqualsHelper.nullSafeEquals(userId, other.userId);
	}

}
