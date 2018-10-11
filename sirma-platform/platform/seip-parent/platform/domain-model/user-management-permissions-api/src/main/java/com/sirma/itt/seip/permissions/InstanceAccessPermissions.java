package com.sirma.itt.seip.permissions;

/**
 * Used to store read and write permissions for instances. Contains convenient method for different access permissions
 * building. Could be extended if needed.
 *
 * @author A. Kunchev
 */
public enum InstanceAccessPermissions {

	/** Shows that there are no read or write permissions. */
	NO_ACCESS,

	/** Shows that there are only read permissions. */
	CAN_READ,

	/** Shows that there are read and write permissions. Equivalent to full access. */
	CAN_WRITE;

	/**
	 * Checks if the given access permissions allows read access.
	 *
	 * @param accessPermissions
	 *            to check
	 * @return <code>true</code> if the given enum value is non <code>null</code> and is is not {@link #NO_ACCESS}
	 */
	public static boolean canRead(InstanceAccessPermissions accessPermissions) {
		return accessPermissions != null && accessPermissions != NO_ACCESS;
	}

	/**
	 * Checks if the given access permissions allows write access.
	 *
	 * @param accessPermissions
	 *            to check
	 * @return <code>true</code> if the given enum value is non <code>null</code> and is equal to {@link #CAN_WRITE}
	 */
	public static boolean canWrite(InstanceAccessPermissions accessPermissions) {
		return accessPermissions != null && accessPermissions == CAN_WRITE;
	}
}
