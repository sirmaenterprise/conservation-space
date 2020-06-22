package com.sirma.itt.seip.monitor;

import com.sirma.itt.seip.annotation.NoOperation;

/**
 * Empty statistics object. This is default implementation. If the statistics module is not present.
 *
 * @author BBonev
 */
@NoOperation
public class NoOpStatistics implements Statistics {

	public static final NoOpStatistics INSTANCE = new NoOpStatistics();

	public NoOpStatistics() {
		// use a single instance
	}

	@Override
	public void track(Metric def) {
		// nothing to do
	}

	@Override
	public Number value(String metric) {
		// nothing to do
		return null;
	}

	@Override
	public void value(String metric, Number value) {
		// nothing to do
	}

	@Override
	public void end(Metric def) {
		// nothing to do
	}
}
