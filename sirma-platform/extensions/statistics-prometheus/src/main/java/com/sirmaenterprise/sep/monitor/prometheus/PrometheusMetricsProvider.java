package com.sirmaenterprise.sep.monitor.prometheus;

import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.monitor.StatisticsProvider;

/**
 * Provider for Prometheus statistics provider.
 *
 * @author BBonev
 */
public class PrometheusMetricsProvider implements StatisticsProvider {

	@Override
	public Statistics provide() {
		// for now nothing special is needed
		// so we can use a single instance
		return PrometheusStatistics.INSTANCE;
	}

}
