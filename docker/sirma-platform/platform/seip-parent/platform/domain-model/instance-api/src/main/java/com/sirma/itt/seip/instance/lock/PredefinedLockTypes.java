package com.sirma.itt.seip.instance.lock;

/**
 * Contains predefined types of lock that are used in the system.
 *
 * @author A. Kunchev
 * @see LockService
 */
public enum PredefinedLockTypes {

	/**
	 * Should be used when the resource is locked by the system. This type is checked, when delete operation is executed
	 * to prevent deleting of instances that are being processed, because it may cause data inconsistency.
	 */
	SYSTEM("$system$");

	private final String type;

	private PredefinedLockTypes(final String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
