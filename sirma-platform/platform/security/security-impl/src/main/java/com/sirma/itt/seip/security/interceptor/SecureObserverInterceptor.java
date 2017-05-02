/**
 *
 */
package com.sirma.itt.seip.security.interceptor;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import com.sirma.itt.seip.security.annotation.SecureObserver;
import com.sirma.itt.seip.security.util.SecureEvent;

/**
 * Intercetor that checks the intercepted method for {@link SecureEvent} object to execute method in that context.
 *
 * @author BBonev
 */
@Interceptor
@SecureObserver
@Priority(Interceptor.Priority.APPLICATION - 100)
public class SecureObserverInterceptor {

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
	@SuppressWarnings("static-method")
	public Object manageSecurityContext(final InvocationContext ctx) throws Exception { // NOSONAR
		Object[] parameters = ctx.getParameters();
		if (parameters == null || parameters.length == 0) {
			return ctx.proceed();
		}
		for (int i = 0; i < parameters.length; i++) {
			Object parameter = parameters[i];
			if (parameter instanceof SecureEvent) {
				SecureEvent secureEvent = (SecureEvent) parameter;
				return secureEvent.call(ctx::proceed);
			}
		}
		return ctx.proceed();
	}

}
