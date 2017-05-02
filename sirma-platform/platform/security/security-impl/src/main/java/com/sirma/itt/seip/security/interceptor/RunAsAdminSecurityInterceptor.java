package com.sirma.itt.seip.security.interceptor;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import com.sirma.itt.seip.security.annotation.RunAsAdmin;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Interceptor for managing and updating secure admin context taken from the current authentication session.
 *
 * @author smustafov
 */
@Priority(Interceptor.Priority.APPLICATION)
@RunAsAdmin
@Interceptor
public class RunAsAdminSecurityInterceptor {

	@Inject
	private SecurityContextManager contextManager;

	/**
	 * Manage security context as admin.
	 *
	 * @param ctx
	 *            the ctx
	 * @return the object
	 * @throws Exception
	 *             the exception
	 */
	@AroundInvoke
	@SuppressWarnings("squid:S00112")
	public Object manageSecurityContext(final InvocationContext ctx) throws Exception {
		return contextManager.executeAsAdmin().callable(ctx::proceed);
	}

}
