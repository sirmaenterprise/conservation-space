/**
 *
 */
package com.sirma.itt.seip.security.util;

/**
 * Event object that implements this interface will be executed for all active tenants if any only if run from the
 * system tenant context.
 * <p>
 * If event is fired from a tenant context then the event will be executed only in the current tenant context.
 * <p>
 * Note that firing event for multiple tenants may take long time if observers are synchronous.
 *
 * @author BBonev
 */
public interface SecureEventAllTenants extends SecureEvent {

	/**
	 * The event may specify if multi tenant invocation may happen in parallel. Default behavior is to be run in a
	 * sequence. Note that this is not a guarantee that the execution will be actually in parallel.
	 *
	 * @return true, if allowed to execute in parallel.
	 */
	default boolean allowParallel() {
		return false;
	}

	/**
	 * The method will be called to create a copy of the current event instance with all parameters needed for the event
	 * evaluation. The copy is needed in order to allow simultaneous executions. If the method returns <code>null</code>
	 * then the same instance will be used but asynchronous executions will not be possible.<br>
	 * Note that if parallel processing is required then the method should return non <code>null</code> event instance
	 * to be fired otherwise an exception will be thrown.<br>
	 * Note that if the event has some other pay load that may modify during event execution it should be copied also.
	 *
	 * @return copy of the current event or <code>null</code> to use the current event instance
	 */
	default SecureEvent copy() {
		return null;
	}
}
