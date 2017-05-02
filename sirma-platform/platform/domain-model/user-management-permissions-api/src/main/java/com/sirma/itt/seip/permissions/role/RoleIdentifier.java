package com.sirma.itt.seip.permissions.role;

import java.io.Serializable;

/**
 * The RoleIdentifier is base class for any role registered in the system
 */
public interface RoleIdentifier extends Serializable, Comparable<RoleIdentifier> {

	/**
	 * Gets the actual role identifier.
	 *
	 * @return the identifier
	 */
	String getIdentifier();

	/**
	 * Get the priority for this role. Higher value means higher role as administrator
	 *
	 * @return some number
	 */
	int getGlobalPriority();

	/**
	 * Defines if the current role is internal or external
	 *
	 * @return <code>true</code> if the role is internal and should not be returned to the user interface
	 */
	boolean isInternal();

	/**
	 * Defines if a user has the specified role he/she has a read permissions
	 *
	 * @return <code>true</code> if read is allowed
	 */
	boolean canRead();

	/**
	 * Defines if a user has the specified role he/she has a write permissions
	 *
	 * @return <code>true</code> if write is allowed
	 */
	boolean canWrite();

	/**
	 * Identifies if the user is added by user or if the role is internally or via XML defined
	 *
	 * @return <code>true</code> if defined by the user via the interface.
	 */
	boolean isUserDefined();

	/**
	 * Sort roles by their global priority
	 */
	@Override
	default int compareTo(RoleIdentifier o) {
		if (o == null) {
			return -1;
		}
		return Integer.compare(getGlobalPriority(), o.getGlobalPriority());
	}
}
