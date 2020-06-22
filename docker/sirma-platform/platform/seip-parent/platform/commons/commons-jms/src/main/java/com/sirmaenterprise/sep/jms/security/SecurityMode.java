package com.sirmaenterprise.sep.jms.security;

/**
 * Defines the different security mode when sending/receiving messages. This defines how the security context should be
 * initialized when the message is received.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 07/06/2017
 */
public enum SecurityMode {
	/**
	 * Defines the default behaviour. The security context will be initialized using the active security context and
	 * user details. It will be as the user that sends the message executes the consumer.
	 */
	DEFAULT,
	/**
	 * The security context should be initialized in system tenant mode. The context will include the original
	 * request id if available. This is default behaviour if no security context is available when message is received
	 */
	SYSTEM,
	/**
	 * The security context will be initialized for the given tenant but from the name of the system with tenant
	 * admin permissions. The context will include the original request id if available. This is default behaviour if
	 * only tenant identifier is available when message is received
	 */
	TENANT_ADMIN
}
