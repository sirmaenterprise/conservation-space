/**
 *
 */
package com.sirma.itt.seip.tasks;

import com.sirma.itt.seip.security.exception.SecurityException;

/**
 * Defines the security execution context for the scheduler tasks. This defines with what permissions and scope is the
 * scheduled task.
 *
 * @author BBonev
 */
public enum RunAs {

	/**
	 * Default behavior if nothing is specified. The security context used for the execution will be the same as the one
	 * used when scheduled the operation. Note that an operation cannot be scheduled if no active security context is
	 * found!.
	 */
	DEFAULT,

	/**
	 * The execution context will be current user and with permissions of the current user.
	 */
	USER,

	/**
	 * The execution context will be current user if any with permissions as admin. This means that in the current user
	 * will be placed in the audit log but operations will executed as admin user. If not authenticated user is found
	 * then system user will be used.
	 */
	ADMIN,

	/**
	 * The execution context will be the system with system admin permissions. No tenant information will be accessible
	 * during this execution mode.
	 * <p>
	 * This is reserved for system operations and cannot be used for scheduling user operations. If an attempt to
	 * schedule for all tenants in tenant context a {@link SecurityException} will be thrown.
	 */
	SYSTEM,

	/**
	 * This is special case when system operation should be run for all tenants, if more then one is discovered.
	 * <p>
	 * This is reserved for system operations and cannot be used for scheduling user operations. If an attempt to
	 * schedule for all tenants in tenant context a {@link SecurityException} will be thrown.
	 */
	ALL_TENANTS;
}
