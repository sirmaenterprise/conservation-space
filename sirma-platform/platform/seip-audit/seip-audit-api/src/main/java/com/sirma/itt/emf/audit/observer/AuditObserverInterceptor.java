package com.sirma.itt.emf.audit.observer;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import com.sirma.itt.seip.configuration.Options;

/**
 * Intercepts method calls in methods, observing audit events and checks if the audit log is enabled before proceeding.
 *
 * @author nvelkov
 */
@Auditable
@Interceptor
public class AuditObserverInterceptor {

	/**
	 * Manage context.
	 *
	 * @param ctx
	 *            the context
	 * @return the resulting object
	 * @throws Exception
	 *             when something goes wrong with the context's proceed method.
	 */
	@AroundInvoke
	@SuppressWarnings("static-method")
	public Object manageContext(final InvocationContext ctx) throws Exception { // NOSONAR
		if (!Options.DISABLE_AUDIT_LOG.isEnabled()) {
			return ctx.proceed();
		}
		return null;
	}
}
