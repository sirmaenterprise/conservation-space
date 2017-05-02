package com.sirma.itt.emf.audit;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import com.sirma.itt.seip.annotation.DisableAudit;
import com.sirma.itt.seip.configuration.Options;

/**
 * Capsulates the mechanism for disabling the audit log for all logic in methods annotated with {@link DisableAudit}.
 *
 * @author Mihail Radkov
 */
@DisableAudit
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class DisableAuditInterceptor {

	/**
	 * Manages the invocation context around method timeout. Disables the auditing before the method and after that
	 * enables it.
	 *
	 * @param ctx
	 *            - the context
	 * @return the resulting object
	 * @throws Exception
	 *             when something goes wrong with the context's proceed method.
	 */
	@AroundTimeout
	public Object manageTimeoutContext(final InvocationContext ctx) throws Exception {
		return manageContext(ctx);
	}

	/**
	 * Manages the invocation context around method invoking. Disables the auditing before the method and after that
	 * enables it.
	 *
	 * @param ctx
	 *            - the context
	 * @return the resulting object
	 * @throws Exception
	 *             when something goes wrong with the context's proceed method.
	 */
	@AroundInvoke
	public Object manageContext(final InvocationContext ctx) throws Exception {
		try {
			Options.DISABLE_AUDIT_LOG.enable();
			return ctx.proceed();
		} finally {
			Options.DISABLE_AUDIT_LOG.disable();
		}
	}
}
