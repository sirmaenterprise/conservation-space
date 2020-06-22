package com.sirmaenterprise.sep.monitor.prometheus;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.monitor.Metric;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.monitor.annotations.StatisticsImplementation;
import com.sirma.itt.seip.security.context.SecurityContext;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.Histogram.Timer;

/**
 * Statistics implementation that uses Prometheus client api for statistics collection.
 *
 * @author BBonev
 * @author yasko
 */
@Singleton
@StatisticsImplementation
public class PrometheusStatistics implements Statistics {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String METRIC_NS = "sep";
	private static final String[] LABELS = new String[] { "tenant" };

	private static final String ACTION_INC = "inc";
	private static final String ACTION_DEC = "dec";
	private static final String ACTION_START_TIMER = "start-timer";
	private static final String ACTION_OBSERVE_DURATION = "observe-duration";
	private static final String ACTION_OBSERVE = "observe";

	private Map<String, Counter> counters = new ConcurrentHashMap<>(128);
	private Map<String, Histogram> histograms = new ConcurrentHashMap<>(128);
	private Map<String, Gauge> gauges = new ConcurrentHashMap<>(128);

	private ThreadLocal<Map<String, Number>> values = ThreadLocal.withInitial(HashMap::new);
	private ThreadLocal<Map<String, Timer>> timers = ThreadLocal.withInitial(HashMap::new);

	@Inject
	private SecurityContext securityContext;

	@Override
	public void track(Metric def) {
		switch (def.type()) {
		case HISTOGRAM:
			break;
		case TIMER:
			handleHistogram(def, ACTION_START_TIMER);
			break;
		case GAUGE:
			handleGauge(def, ACTION_INC);
			break;
		case COUNTER:
			handleCounter(def);
			break;
		default:
			LOGGER.warn("unsupported metric type: {}", def.type());
			break;
		}
	}

	@Override
	public Number value(String metric) {
		return values.get().getOrDefault(metric, 0);
	}

	@Override
	public void value(String metric, Number value) {
		values.get().put(metric, value);
	}

	@Override
	public void end(Metric def) {
		switch (def.type()) {
		case HISTOGRAM:
			handleHistogram(def, ACTION_OBSERVE);
			break;
		case TIMER:
			handleHistogram(def, ACTION_OBSERVE_DURATION);
			break;
		case GAUGE:
			handleGauge(def, ACTION_DEC);
			break;
		case COUNTER:
			// counters only go up
			break;
		default:
			LOGGER.warn("unsupported metric type: {}", def.type());
			break;
		}
	}

	private void handleCounter(Metric ctx) {
		Counter counter = counters.computeIfAbsent(ctx.name(), name -> Counter.build()
					.name(ctx.name())
					.namespace(METRIC_NS)
					.labelNames(LABELS)
					.help(ctx.description())
				.register());
		counter.labels(labels()).inc();
	}

	private void handleGauge(Metric def, String action) {
		switch (action) {
		case ACTION_INC:
			createGauge(def).labels(labels()).inc();
			break;
		case ACTION_DEC:
			createGauge(def).labels(labels()).dec();
			break;
		default:
			LOGGER.warn("unsupported metric acton {} for type {}", action, def.type());
			break;
		}
	}

	private void handleHistogram(Metric def, String action) {
		switch (action) {
		case ACTION_START_TIMER:
			timers.get().put(def.name(), createHistogram(def).labels(labels()).startTimer());
			break;
		case ACTION_OBSERVE_DURATION:
			Timer timer = timers.get().get(def.name());
			if (timer != null) {
				timer.observeDuration();
			}
			break;
		case ACTION_OBSERVE:
			createHistogram(def).labels(labels()).observe(value(def.name()).doubleValue());
			break;
		default:
			LOGGER.warn("unsupported metric acton {} for type {}", action, def.type());
			break;
		}
	}

	private Histogram createHistogram(Metric def) {
		return histograms.computeIfAbsent(def.name(), name -> Histogram.build()
					.name(name)
					.namespace(METRIC_NS)
					.labelNames(LABELS)
					.help(def.description())
				.register());
	}

	private Gauge createGauge(Metric def) {
		return gauges.computeIfAbsent(def.name(), name -> Gauge.build()
					.name(def.name())
					.namespace(METRIC_NS)
					.labelNames(LABELS)
					.help(def.description())
				.register());
	}

	private String[] labels() {
		return new String[]{ securityContext.getCurrentTenantId() };
	}
}
