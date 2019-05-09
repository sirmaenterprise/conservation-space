package com.sirma.itt.seip.rest.filters.metrics;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import com.sirma.itt.seip.monitor.Metric;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.monitor.annotations.Monitored;

/**
 * Handles custom metrics for rest services configured via the {@link Monitored}
 * annotation.
 *
 * @author yasko
 */
public class CustomMetricsFilter implements ContainerRequestFilter, ContainerResponseFilter {

	private Statistics stats;
	private List<Metric> metrics;

	public CustomMetricsFilter(Statistics stats, List<Metric> metrics) {
		this.stats = stats;
		this.metrics = metrics;
	}

	@Override
	public void filter(ContainerRequestContext req) throws IOException {
		for (Metric metric : metrics) {
			stats.track(metric);
		}
	}

	@Override
	public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
		for (Metric metric : metrics) {
			stats.end(metric);
		}
	}
}
