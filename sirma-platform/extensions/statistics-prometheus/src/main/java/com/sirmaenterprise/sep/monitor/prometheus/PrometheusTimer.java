package com.sirmaenterprise.sep.monitor.prometheus;

import com.sirma.itt.seip.time.TimeTracker;

import io.prometheus.client.Histogram;

/**
 * {@link TimeTracker} extension that provides Prometheus histogram timer integration.
 *
 * @author BBonev
 */
public class PrometheusTimer extends TimeTracker {
	private static final long serialVersionUID = -8508786629151438743L;

	private final Histogram histogram;
	private Histogram.Timer timer;

	/**
	 * Instantiate new timer for the given histogram instance
	 * 
	 * @param histogram
	 *            the histogram to be wrapped
	 */
	public PrometheusTimer(Histogram histogram) {
		this.histogram = histogram;
	}

	@Override
	public TimeTracker begin() {
		super.begin();
		if (histogram != null && timer == null) {
			timer = histogram.startTimer();
		}
		return this;
	}

	@Override
	public long stop() {
		long stop = super.stop();
		if (isEnded() && timer != null) {
			timer.observeDuration();
			timer = null;
		}
		return stop;
	}
}
