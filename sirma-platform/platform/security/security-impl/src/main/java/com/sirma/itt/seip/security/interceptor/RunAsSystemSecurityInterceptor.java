package com.sirma.itt.seip.security.interceptor;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import com.sirma.itt.seip.security.annotation.RunAsSystem;
import com.sirma.itt.seip.security.context.ContextualExecutor;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Interceptor for managing and updating secure context taken from the current authentication session.
 *
 * @author BBonev
 */
@Priority(Interceptor.Priority.APPLICATION)
@RunAsSystem
@Interceptor
public class RunAsSystemSecurityInterceptor {

	@Inject
	private SecurityContextManager contextManager;

	/**
	 * Manage security context.
	 *
	 * @param ctx
	 *            the ctx
	 * @return the object
	 * @throws Exception
	 *             the exception
	 */
	@AroundInvoke
	public Object manageSecurityContext(final InvocationContext ctx) throws Exception { // NOSONAR
		return interceptInternal(ctx);
	}

	/**
	 * Manage timed security context. Called on timer events
	 *
	 * @param ctx
	 *            the ctx
	 * @return the object
	 * @throws Exception
	 *             the exception
	 */
	@AroundTimeout
	public Object manageTimedSecurityContext(final InvocationContext ctx) throws Exception { // NOSONAR
		return interceptInternal(ctx);
	}

	/**
	 * The actual implementation for the interception.
	 *
	 * @param ctx
	 *            the ctx
	 * @return the object
	 * @throws Exception
	 *             the exception
	 */
	private Object interceptInternal(final InvocationContext ctx) throws Exception { // NOSONAR
		SecurityContext securityContext = contextManager.getCurrentContext();
		boolean protectCurrentTenant = ctx.getMethod().getAnnotation(RunAsSystem.class).protectCurrentTenant();
		ContextualExecutor caller;
		if (!securityContext.isActive() || !protectCurrentTenant || securityContext.isSystemTenant()) {
			caller = contextManager.executeAsSystemAdmin();
		} else {
			caller = contextManager.executeAsSystem();
		}
		return caller.callable(ctx::proceed);
	}
}
