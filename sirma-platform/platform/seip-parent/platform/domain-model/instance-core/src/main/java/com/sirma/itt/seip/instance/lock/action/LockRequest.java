package com.sirma.itt.seip.instance.lock.action;

import com.sirma.itt.seip.instance.actions.ActionRequest;

/**
 * Used for lock action. Can be extended with additional information for the operation execution, if needed.
 *
 * @author A. Kunchev
 */
public class LockRequest extends ActionRequest {

	private static final long serialVersionUID = -2195162687142339236L;

	public static final String LOCK = "lock";

	/** Used for information about the lock reason, if it is locked by user operation, or it is locked for edit, etc. */
	private String lockType;

	@Override
	public String getOperation() {
		return LOCK;
	}

	@Override
	public String getUserOperation() {
		return LOCK;
	}

	/**
	 * @return the lockType
	 */
	public String getLockType() {
		return lockType;
	}

	/**
	 * @param lockType
	 *            the lockType to set
	 */
	public void setLockType(String lockType) {
		this.lockType = lockType;
	}

}
