package com.sirmaenterprise.sep.monitor.prometheus.servlet;

import javax.servlet.annotation.WebServlet;

import io.prometheus.client.exporter.MetricsServlet;

/**
 * Servlet that will be called by Prometheus server for statistics collecting
 *
 * @author BBonev
 */
@WebServlet(PrometheusMetricsServlet.PATH)
public class PrometheusMetricsServlet extends MetricsServlet {

	private static final long serialVersionUID = -6278713367134052100L;

	public static final String PATH = "/prometheus";
}
