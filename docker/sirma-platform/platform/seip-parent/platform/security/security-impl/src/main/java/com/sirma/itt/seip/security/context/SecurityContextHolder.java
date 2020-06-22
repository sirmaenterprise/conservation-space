package com.sirma.itt.seip.security.context;

import java.io.Serializable;
import java.util.Objects;

import com.sirma.itt.seip.context.Config;
import com.sirma.itt.seip.context.RuntimeContext;

/**
 * Thread local holder for the security context. The holder supports stack operations via the methods
 * {@link #pushContext(SecurityContext)} and {@link #popContext()}.
 *
 * @author BBonev
 */
class SecurityContextHolder {

	/** The context holder. */
	private static final Config CONTEXT_STORE = RuntimeContext.createConfig("$SECURITY_CONTEXT_STORE$", false);

	/**
	 * Instantiates a new security context holder.
	 */
	private SecurityContextHolder() {
		// utility class
	}

	/**
	 * Push the given security context to the stack.
	 *
	 * @param context
	 *            the context
	 */
	public static void pushContext(SecurityContext context) {
		Objects.requireNonNull(context, "Cannot push null security context!");
		CONTEXT_STORE.set(new ContextWrapper(context));
	}

	/**
	 * Pop a context from the stack. Returns the top element of the stack and removes it. If the stack is empty
	 * <code>null</code> will be returned.
	 *
	 * @return the security context
	 */
	public static SecurityContext popContext() {
		SecurityContext securityContext = getContext();
		CONTEXT_STORE.clear();
		return securityContext;
	}

	/**
	 * Sets the current context by clearing any existing contexts
	 *
	 * @param context
	 *            the new context
	 */
	public static void setContext(SecurityContext context) {
		clear();
		if (context != null) {
			CONTEXT_STORE.set(new ContextWrapper(context));
		}
	}

	/**
	 * Gets the current context.
	 *
	 * @return the context
	 */
	public static SecurityContext getContext() {
		ContextWrapper wrapper = (ContextWrapper) CONTEXT_STORE.get();
		return wrapper == null ? null : wrapper.context;
	}

	/**
	 * Clear all pushed contexts
	 */
	public static void clear() {
		while (CONTEXT_STORE.isSet()) {
			CONTEXT_STORE.clear();
		}
	}

	/**
	 * Checks if is sets the.
	 *
	 * @return true, if is sets the
	 */
	public static boolean isSet() {
		return CONTEXT_STORE.isSet();
	}

	/**
	 * Wrapper object to protect the stored context from easy access.
	 *
	 * @author BBonev
	 */
	private static class ContextWrapper implements Serializable {
		private static final long serialVersionUID = 607551291158881601L;
		final SecurityContext context;

		/**
		 * Instantiates a new context wrapper.
		 *
		 * @param context
		 *            the context
		 */
		public ContextWrapper(SecurityContext context) {
			this.context = context;
		}
	}
}
