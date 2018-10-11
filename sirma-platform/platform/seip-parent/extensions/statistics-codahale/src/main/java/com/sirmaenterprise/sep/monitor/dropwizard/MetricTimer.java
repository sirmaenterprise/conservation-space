package com.sirmaenterprise.sep.monitor.dropwizard;

import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Extension of the {@link TimeTracker} to enable statistics collection.
 *
 * @author BBonev
 */
public class MetricTimer extends TimeTracker {
	private static final long serialVersionUID = -8059599764708174277L;
	private Timer timer;
	private Context time;

	/**
	 * Instantiates a new metric timer with the given {@link Timer} object
	 *
	 * @param timer
	 *            the timer
	 */
	public MetricTimer(Timer timer) {
		this.timer = timer;
	}

	@Override
	public TimeTracker begin() {
		super.begin();
		if (timer != null && time == null) {
			time = timer.time();
		}
		return this;
	}

	@Override
	public long stop() {
		long stop = super.stop();
		if (isEnded() && time != null) {
			time.stop();
			time = null;
		}
		return stop;
	}

}
