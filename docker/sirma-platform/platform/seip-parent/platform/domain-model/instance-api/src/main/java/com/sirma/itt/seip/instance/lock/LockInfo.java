package com.sirma.itt.seip.instance.lock;

import java.io.Serializable;
import java.util.Date;
import java.util.function.Predicate;

import com.sirma.itt.seip.domain.instance.InstanceReference;

/**
 * Represents a lock information for a single instance.
 *
 * @author BBonev
 */
public class LockInfo implements Serializable {

	private static final long serialVersionUID = -7199532773065164731L;

	private Serializable lockedBy;
	private Predicate<Serializable> lockedByMe = id -> false;
	private InstanceReference lockedInstance;
	private Date lockedOn;
	/**
	 * Provides some additional information about the lock.
	 */
	private String lockInfo;

	/**
	 * Instantiates a new lock info.
	 */
	public LockInfo() {
		// just default constructor needed for serialization by Kryo
	}

	/**
	 * Instantiates a new lock info.
	 *
	 * @param lockedInstance
	 *            the locked instance
	 * @param lockedBy
	 *            the locked by
	 * @param lockedOn
	 *            the locked on
	 * @param lockInfo
	 *            the lock type
	 * @param lockedByMe
	 *            the locked by me
	 */
	public LockInfo(InstanceReference lockedInstance, Serializable lockedBy, Date lockedOn, String lockInfo,
			Predicate<Serializable> lockedByMe) {
		this.lockedInstance = lockedInstance;
		this.lockedBy = lockedBy;
		this.lockedOn = lockedOn;
		this.lockInfo = lockInfo;
		this.lockedByMe = lockedByMe;
	}

	/**
	 * Returns the lock status for the current instance.
	 *
	 * @return true, if is locked
	 */
	public boolean isLocked() {
		return lockedBy != null;
	}

	/**
	 * Returns the user that holds the lock
	 *
	 * @return the lockedBy
	 */
	public Serializable getLockedBy() {
		return lockedBy;
	}

	/**
	 * Checks if the current user holds the lock
	 *
	 * @return <code>true</code> if locked by the current user.
	 */
	public boolean isLockedByMe() {
		return lockedByMe.test(getLockedBy());
	}

	/**
	 * Returns the instance reference for which this lock info represents.
	 *
	 * @return the locked instance reference
	 */
	public InstanceReference getLockedInstance() {
		return lockedInstance;
	}

	/**
	 * Returns the date when the instance was locked
	 *
	 * @return the locked on date or <code>null</code> if not locked
	 */
	public Date getLockedOn() {
		return lockedOn;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LockInfo [");
		if (lockedBy != null) {
			builder.append("lockedBy=").append(lockedBy).append(", ");
		}
		if (lockedInstance != null) {
			builder.append("lockedInstance=").append(lockedInstance.getId()).append(", ");
		}
		if (lockedOn != null) {
			builder.append("lockedOn=").append(lockedOn);
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Gets the lock info.
	 *
	 * @return the lockInfo
	 */
	public String getLockInfo() {
		return lockInfo;
	}
}
