package com.sirma.itt.seip.monitor;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import com.sirma.itt.seip.monitor.annotations.Monitored;

/**
 * Interceptor handling metrics defined on service methods.
 *
 * @author yasko
 */
@Monitored
@Interceptor
@Priority(javax.interceptor.Interceptor.Priority.APPLICATION)
public class MetricsInterceptor implements Serializable {
	private static final long serialVersionUID = 4521266477414065417L;

	@Inject
	private Statistics stats;

	@Inject
	private MetricDefinitionsCache cache;

	/**
	 * Intercepts calls to methods annotated with {@link Monitored} and updates
	 * the defined metrics before and after the method execution.
	 *
	 * @param ctx
	 *            Invocation context.
	 * @return next method invocation
	 * @throws Exception
	 */
	@AroundInvoke
	public Object trackMetrics(InvocationContext ctx) throws Exception {
		List<Metric> metrics = cache.getMetrics(ctx.getMethod());

		try {
			metrics.forEach(stats::track);
			return ctx.proceed();
		} finally {
			metrics.forEach(stats::end);
		}
	}
}
