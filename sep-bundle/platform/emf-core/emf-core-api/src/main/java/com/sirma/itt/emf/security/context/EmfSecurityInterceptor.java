package com.sirma.itt.emf.security.context;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Callable;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.util.ReflectionUtils;

/**
 * Interceptor for managing and updating secure context taken from the current authentication
 * session.
 *
 * @author BBonev
 */
@Secure
@Interceptor
public class EmfSecurityInterceptor {

	private static Logger LOGGER = LoggerFactory.getLogger(EmfSecurityInterceptor.class);

	/** The authentication service. */
	@Inject
	private Instance<AuthenticationService> authenticationService;

	/** The Constant NO_MODIFICATION. */
	private static final boolean[] NO_MODIFICATION = new boolean[] { false, false };

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
	public Object manageSecurityContext(final InvocationContext ctx) throws Exception {
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
	public Object manageTimedSecurityContext(final InvocationContext ctx) throws Exception {
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
	private Object interceptInternal(final InvocationContext ctx) throws Exception {
		Secure secure = getAnnotation(ctx);
		// check for authentication if required and not executing as admin or system
		if (secure.runAsSystem()) {
			return SecurityContextManager.callAsSystem(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					return ctx.proceed();
				}
			});
		}
		boolean[] updatedContext = updateAuthenticationContext(secure.runAsAdmin());
		try {
			return ctx.proceed();
		} finally {
			restoreAuthenticationContext(updatedContext);
		}
	}

	/**
	 * Gets the annotation from the execution context
	 *
	 * @param ctx
	 *            the source context
	 * @return the annotation
	 */
	private Secure getAnnotation(InvocationContext ctx) {
		Secure annotation = ctx.getMethod().getAnnotation(Secure.class);
		if (annotation == null) {
			annotation = ReflectionUtils.getAnnotationFromWeldBean(ctx.getTarget(), Secure.class);
			// annotation = ctx.getTarget().getClass().getAnnotation(Secure.class);
		}
		return annotation;
	}

	/**
	 * Update authentication context.
	 *
	 * @param runAsAdmin
	 *            to executes the current context as admin
	 * @return the <code>true</code> if the context was updated and need to be restored and
	 *         <code>false</code> if the context only need to be cleared
	 */
	protected boolean[] updateAuthenticationContext(boolean runAsAdmin) {
		// if the security context was set from other call or from outside then
		// we ignore the request
		boolean isSystemUser = false;
		if (SecurityContextManager.getFullAuthentication() != null) {
			isSystemUser = SecurityContextManager.getFullAuthentication()
					.equals(SecurityContextManager.getSystemUser());
			if (isSystemUser && LOGGER.isTraceEnabled()) {
				LOGGER.trace("\n======================\nAlready logged in as "
						+ SecurityContextManager.getFullAuthentication().getId()
						+ "\n======================");
			}
			if (!isSystemUser) {
				return NO_MODIFICATION;
			}
		}

		User user = SecurityContextManager.getAdminUser();
		AuthenticationService service = getAuthenticationService();
		if (service == null) {
			if (isSystemUser) {
				return NO_MODIFICATION;
			}
			if (!runAsAdmin) {
				LOGGER.warn("No authentication service available!");
				return NO_MODIFICATION;
			}
			LOGGER.warn("No authentication service available but RunAsAdmin: enabled. Continue");
		} else {
			user = service.getCurrentUser();
			if (user == null) {
				if (!runAsAdmin) {
					LOGGER.warn("No authenticated user available!");
					return NO_MODIFICATION;
				}
				LOGGER.warn("No authenticated user available but RunAsAdmin: enabled. Continue");
			}
		}
		User fullAuthentication = SecurityContextManager.getFullAuthentication();
		// if we have some authentication we push into the stack so we can restore it later
		if (fullAuthentication != null) {
			if (isSystemUser) {
				LOGGER.debug("Overrding current System user with " + user.getId());
			}

			// FIXME: This should be removed when the problem (CMF-7840) is resolved
			if (isSystemUser && LOGGER.isTraceEnabled()) {
				PrintWriter writer = new PrintWriter(new StringWriter(1024));
				// {
				// @Override
				// public void println(Object x) {
				// String string = String.valueOf(x);
				// // if (string.contains("sirma")) {
				// super.println(string);
				// // }
				// }
				// };
				new Exception().fillInStackTrace().printStackTrace(writer);
				LOGGER.trace("\n======================\nCurrent user is System. Passing through @Secure again with currently logged in user ["
						+ fullAuthentication.getId()
						+ "] and stack\n"
						+ writer.toString()
						+ "\n======================\n");
			}

			SecurityContextManager.pushAuthentication();
		}

		SecurityContextManager.authenticateFullyAs(user);
		// copy the effective user authentications
		if (service != null) {
			User effectiveAuthentication = service.getEffectiveAuthentication();
			if (effectiveAuthentication != null) {
				SecurityContextManager.authenticateAs(effectiveAuthentication);
			}
		} else if (runAsAdmin) {
			SecurityContextManager.authenticateAs(SecurityContextManager.getAdminUser());
		}
		return new boolean[] { true, fullAuthentication != null };
	}

	/**
	 * Restore authentication context.
	 *
	 * @param updatedContext
	 *            if <code>true</code> the context will be restored otherwise will be cleared
	 */
	protected void restoreAuthenticationContext(boolean[] updatedContext) {
		// if we have modified the context then we restore it properly otherwise we do nothing
		if (updatedContext[0]) {
			// restore authentication
			if (updatedContext[1]) {
				SecurityContextManager.popAuthentication();
			} else {
				// clear context
				SecurityContextManager.clearCurrentSecurityContext();
			}
		}
	}

	/**
	 * Getter method for authenticationService.
	 *
	 * @return the authenticationService
	 */
	public AuthenticationService getAuthenticationService() {
		if (authenticationService.isAmbiguous() || authenticationService.isUnsatisfied()) {
			return null;
		}
		try {
			AuthenticationService service = authenticationService.get();
			// force the service to be lookup
			service.getCurrentUser();
			return service;
		} catch (ContextNotActiveException e) {
			LOGGER.trace("No active session context!");
			return null;
		}
	}
}
