package com.sirma.itt.seip.domain.instance;

/**
 * Means to lock an object and provide data for the user locked it.
 *
 * @author BBonev
 */
public interface Lockable {

	/**
	 * Checks if is locked.
	 *
	 * @return true, if is locked
	 */
	boolean isLocked();

	/**
	 * Gets the locked by.
	 *
	 * @return the locked by
	 */
	String getLockedBy();

	/**
	 * Sets the locked by.
	 *
	 * @param lockedBy
	 *            the new locked by
	 */
	void setLockedBy(String lockedBy);
}
