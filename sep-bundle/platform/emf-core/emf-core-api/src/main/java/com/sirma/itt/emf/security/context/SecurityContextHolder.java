package com.sirma.itt.emf.security.context;


/**
 * Thread local holder for the security context.
 *
 * @author BBonev
 */
public class SecurityContextHolder {

	/** The context holder. */
	private static ThreadLocal<SecurityContext> contextHolder = new ThreadLocal<>();

	/**
	 * Sets the current context.
	 *
	 * @param context
	 *            the new context
	 */
	public static void setContext(SecurityContext context) {
		contextHolder.set(context);
	}

	/**
	 * Gets the current context.
	 * 
	 * @return the context
	 */
	public static SecurityContext getContext() {
		return contextHolder.get();
	}
}
