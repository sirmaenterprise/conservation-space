package com.sirma.itt.seip.rest.filters.metrics;

import java.io.IOException;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.monitor.Metric;
import com.sirma.itt.seip.monitor.Metric.Builder;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;

/**
 * Filter for metrics common to all rest services.
 */
@Singleton
@Provider
@Priority(Priorities.USER)
public class CommonRestMetricsFilter implements ContainerRequestFilter, ContainerResponseFilter {

	private static final Metric HTTP_REQUEST_DURATION_SECONDS = Builder
			.timer("http_request_duration_seconds", "Http api request duration in seconds").build();
	private static final Metric HTTP_REQUESTS_IN_FLIGHT = Builder
			.gauge("http_requests_in_fight", "Http requests currently being processed (in-flight).").build();

	@Inject
	private Statistics statistics;

	@Override
	public void filter(ContainerRequestContext req) throws IOException {
		// We probably need complete solution for this: like this to be added in
		// the request object
		if (ignoreRequest(req)) {
			// the request is a test so we will ignore it from the statistics
			// this could be used for skip health check request for examples
			// from the other request
			// or performance tests on production site
			return;
		}

		statistics.track(HTTP_REQUEST_DURATION_SECONDS);
		statistics.track(HTTP_REQUESTS_IN_FLIGHT);
	}

	@Override
	public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
		if (ignoreRequest(req)) {
			return;
		}

		statistics.end(HTTP_REQUEST_DURATION_SECONDS);
		statistics.end(HTTP_REQUESTS_IN_FLIGHT);
	}

	private static boolean ignoreRequest(ContainerRequestContext req) {
		return req.getUriInfo().getQueryParameters().containsKey(RequestParams.KEY_DISABLE_METRICS);
	}
}
