package com.sirmaenterprise.sep.monitor.prometheus;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import com.sirma.itt.seip.monitor.StatCounter;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.monitor.StatisticsImplementation;
import com.sirma.itt.seip.time.TimeTracker;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

/**
 * Statistics implementation that uses Prometheus client api for statistics collection
 *
 * @author BBonev
 */
@StatisticsImplementation
public class PrometheusStatistics implements Statistics {

	public static final Statistics INSTANCE = new PrometheusStatistics();

	private Map<String, Counter> counters = new ConcurrentHashMap<>(128);
	private Map<String, Histogram> histograms = new ConcurrentHashMap<>(128);
	private Map<String, Gauge> gauges = new ConcurrentHashMap<>(128);

	@Override
	public boolean areStatisticsEnabled() {
		return true;
	}

	@Override
	public void updateMeter(Class<?> caller, String functionName) {
		counters.computeIfAbsent(functionName, key -> Counter.build(key, key).register()).inc();
	}

	@Override
	public TimeTracker createTimeStatistics(Class<?> caller, String functionName) {
		Histogram histogram = histograms.computeIfAbsent(functionName, key -> Histogram.build(key, key).register());
		return new PrometheusTimer(histogram);
	}

	@Override
	public void registerHealthCheck(String systemName, Callable<Boolean> checker) {
		// Prometheus does not support health checks
	}

	@Override
	public void logTrend(Class<?> caller, String functionName, Object value) {
		if (value instanceof Number) {
			Histogram histogram = histograms.computeIfAbsent(functionName, key -> Histogram.build(key, key).register());
			histogram.observe(((Number) value).doubleValue());
		}
	}

	@Override
	public StatCounter getCounter(Class<?> caller, String functionName) {
		Gauge gauge = gauges.computeIfAbsent(functionName, key -> Gauge.build(key, key).register());
		return new PrometheusCounter(gauge);
	}
}
