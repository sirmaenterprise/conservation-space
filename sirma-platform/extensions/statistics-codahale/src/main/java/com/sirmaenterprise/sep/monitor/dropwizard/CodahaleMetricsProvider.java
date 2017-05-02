package com.sirmaenterprise.sep.monitor.dropwizard;

import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.monitor.StatisticsProvider;

/**
 * Provider used to create {@link CodahaleMetrics} instances.
 *
 * @author BBonev
 */
public class CodahaleMetricsProvider implements StatisticsProvider {

	@Override
	public Statistics provide() {
		// for now nothing special is needed
		// so we can just create an instance and return it.
		return new CodahaleMetrics();
	}

}
