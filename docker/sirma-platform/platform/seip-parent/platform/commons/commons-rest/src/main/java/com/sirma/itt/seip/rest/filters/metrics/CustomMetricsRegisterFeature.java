package com.sirma.itt.seip.rest.filters.metrics;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.monitor.Metric;
import com.sirma.itt.seip.monitor.Metric.Builder;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.monitor.annotations.MetricConfig;
import com.sirma.itt.seip.monitor.annotations.MetricDefinition;
import com.sirma.itt.seip.monitor.annotations.Monitored;

/**
 * Registers metrics for rest services defined using the {@link Monitored}
 * annotation.
 */
@Provider
public class CustomMetricsRegisterFeature implements DynamicFeature {

	@Inject
	private Statistics stats;

	@Override
	public void configure(ResourceInfo info, FeatureContext context) {
		// check for any custom metrics defined on the method
		Monitored monitored = info.getResourceMethod().getAnnotation(Monitored.class);
		if (monitored == null) {
			return;
		}

		List<Metric> metrics = new LinkedList<>();
		for (MetricDefinition def : monitored.value()) {
			Builder builder = Builder.newInstance(def.name(), def.type(), def.descr());
			for (MetricConfig config : def.configs()) {
				builder.config(config.key(), config.value());
			}

			metrics.add(builder.build());
		}

		if (!metrics.isEmpty()) {
			context.register(new CustomMetricsFilter(stats, metrics));
		}
	}
}
