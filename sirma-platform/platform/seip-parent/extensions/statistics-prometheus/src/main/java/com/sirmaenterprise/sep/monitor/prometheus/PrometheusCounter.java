package com.sirmaenterprise.sep.monitor.prometheus;

import com.sirma.itt.seip.monitor.StatCounter;

import io.prometheus.client.Gauge;

/**
 * {@link StatCounter} implementation to wrap Prometheus {@link Gauge}.
 *
 * @author BBonev
 */
public class PrometheusCounter implements StatCounter {

	private final Gauge gauge;

	/**
	 * Instantiate new counter to wram the given {@link Gauge}
	 *
	 * @param gauge
	 *            the gauge to wrap
	 */
	public PrometheusCounter(Gauge gauge) {
		this.gauge = gauge;
	}

	@Override
	public void increment() {
		gauge.inc();
	}

	@Override
	public void decrement() {
		gauge.dec();
	}

	@Override
	public void increment(int amount) {
		gauge.inc(amount);
	}

	@Override
	public void decrement(int amount) {
		gauge.dec(amount);
	}
}
