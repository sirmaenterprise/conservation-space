package com.sirmaenterprise.sep.monitor.dropwizard.servlet;

import javax.servlet.annotation.WebListener;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet.ContextListener;
import com.sirmaenterprise.sep.monitor.dropwizard.CodahaleMetrics;

/**
 * Register a context listener for the health check servlet.
 *
 * @author BBonev
 */
@WebListener
public class HealthCheckServletListener extends ContextListener {

	@Override
	protected HealthCheckRegistry getHealthCheckRegistry() {
		return CodahaleMetrics.getHealthCheck();
	}

}
