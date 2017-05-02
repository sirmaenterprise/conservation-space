package com.sirma.itt.seip.security.util;

import com.sirma.itt.seip.context.RuntimeContext;
import com.sirma.itt.seip.context.RuntimeContext.CurrentRuntimeConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.ContextNotActiveException;

/**
 * Base class for implementation of secure invocation objects. The implementation stores the security context at the
 * moment of creation of the object and then uses it to initialize security context before actual invocation.
 *
 * @author BBonev
 */
public abstract class SecureInvoker {
	protected final CurrentRuntimeConfiguration runtimeConfiguration;
	protected final SecurityContextManager securityContextManager;
	protected final SecurityContext securityContext;

	/**
	 * Instantiates a new secure invoker.
	 *
	 * @param securityContextManager
	 *            the security context manager
	 */
	public SecureInvoker(SecurityContextManager securityContextManager) {
		this.securityContextManager = securityContextManager;
		// transfer the current configuration to the executing thread
		runtimeConfiguration = RuntimeContext.getCurrentConfiguration();
		securityContext = securityContextManager.createTransferableContext();
		if (securityContext == null) {
			throw new ContextNotActiveException();
		}
	}

	/**
	 * Called after the actual invocation.
	 *
	 * @param oldConfiguration
	 *            the old configuration
	 */
	protected void afterCall(CurrentRuntimeConfiguration oldConfiguration) {
		try {
			// ensure both methods are called
			RuntimeContext.replaceConfiguration(oldConfiguration);
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	/**
	 * Called before actual invocation
	 *
	 * @return the current configuration
	 */
	protected CurrentRuntimeConfiguration beforeCall() {
		CurrentRuntimeConfiguration oldConfiguration = RuntimeContext.replaceConfiguration(runtimeConfiguration);
		securityContextManager.initializeFromContext(securityContext);
		return oldConfiguration;
	}

}