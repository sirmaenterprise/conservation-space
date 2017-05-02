package com.sirma.itt.seip.rest.filters.metrics;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Filter for rest endpoints that monitors various metrics. The metric names are as follows:
 * <ul>
 * <li>rate for the &lt;accessed tenant&gt;_rest_&lt;http method&gt;_&lt;given path&gt;_rate
 * <li>timer for the &lt;accessed tenant&gt;_rest_&lt;http method&gt;_&lt;given path&gt;_time
 * <li>failed request system_tenant_rest_&lt;http method&gt;_&lt;given path&gt;_fail - these are requests that failed by
 * one or another reason.
 * </ul>
 * There are limitations for the used characters in the path. All invalid chars will be replaced with
 * <code>'_'</code><br>
 * Chars that match the given pattern will be replaced: <code>[/\.\-\[\]:\{\}\s]</code>
 * <p>
 * Metrics collection could be disabled per request if it contains query parameter {@link RequestParams#KEY_TEST_MODE}
 *
 * @author BBonev
 */
@Priority(Priorities.USER)
public class MetricsRequestFilter implements ContainerRequestFilter, ContainerResponseFilter {

	private static final String REST = "_rest_";
	private static final Pattern METRICS_NAME_PATTER = Pattern.compile("[/\\.\\-\\[\\]:\\{\\}\\s]");
	private final String path;
	private final String failedPath;
	private Statistics statistics;
	private SecurityContext securityContext;

	/**
	 * Use thread local as request cannot be executed on other threads (unless async processing is used?)<br>
	 * We store the time tracker for the request as we cannot safely pass it the response filter method.
	 */
	private static final ThreadLocal<TimeTracker> TIME = new ThreadLocal<>();

	/**
	 * Instantiate new filter for the given path
	 *
	 * @param path
	 *            the path to assign to monitor
	 * @param statistics
	 *            the statistics objects builder
	 * @param securityContext
	 *            the security context for the invocation
	 */
	public MetricsRequestFilter(String path, Statistics statistics, SecurityContext securityContext) {
		this.path = escape(path);
		failedPath = escape(SecurityContext.SYSTEM_TENANT) + REST + "%s_" + this.path + "_fail";
		this.statistics = statistics;
		this.securityContext = securityContext;
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		// We probably need complete solution for this: like this to be added in the request object
		if (isTestMode(requestContext)) {
			// the request is a test so we will ignore it from the statistics
			// this could be used for skip health check request for examples from the other request
			// or performance tests on production site
			return;
		}
		String tenantPath = new StringBuilder(64)
				.append(escape(securityContext.getCurrentTenantId()))
					.append(REST)
					.append(requestContext.getMethod().toLowerCase())
					.append("_")
					.append(path)
					.toString();

		// create per tenant counter for the endpoint
		statistics.updateMeter(null, tenantPath + "_rate");
		// track the request duration and throughput based on the tenant scope
		TIME.set(statistics.createTimeStatistics(null, tenantPath + "_time").begin());
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		if (isTestMode(requestContext)) {
			return;
		}
		TimeTracker tracker = TIME.get();
		if (tracker != null) {
			// this happens for request that are not valid so they do not get notified to beginning of the request
			tracker.stop();
		}
		if (responseContext.getStatus() >= 400) {
			// this is not tracked per tenant as we not always have the tenant information here
			// for example when not authenticated request is rejected
			statistics.updateMeter(null, String.format(failedPath, requestContext.getMethod().toLowerCase()));
		}
	}

	/**
	 * @return the escaped path identifier that is monitored from the current filter
	 */
	public String getPath() {
		return path;
	}

	private static String escape(String path) {
		return METRICS_NAME_PATTER.matcher(path).replaceAll("_");
	}

	private static boolean isTestMode(ContainerRequestContext requestContext) {
		return requestContext.getUriInfo().getQueryParameters().containsKey(RequestParams.KEY_DISABLE_METRICS);
	}

	@Override
	public String toString() {
		return getPath();
	}
}
