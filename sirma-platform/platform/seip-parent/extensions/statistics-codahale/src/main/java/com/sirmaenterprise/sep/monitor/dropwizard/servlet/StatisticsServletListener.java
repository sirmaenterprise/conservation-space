package com.sirmaenterprise.sep.monitor.dropwizard.servlet;

import javax.servlet.annotation.WebListener;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet.ContextListener;
import com.sirmaenterprise.sep.monitor.dropwizard.CodahaleMetrics;

/**
 * Register context listener for the metrics registry
 *
 * @author BBonev
 */
@WebListener
public class StatisticsServletListener extends ContextListener {

	@Override
	protected MetricRegistry getMetricRegistry() {
		return CodahaleMetrics.getMetrics();
	}
}
