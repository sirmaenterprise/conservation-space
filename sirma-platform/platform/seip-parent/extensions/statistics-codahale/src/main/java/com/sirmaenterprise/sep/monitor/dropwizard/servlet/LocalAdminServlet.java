package com.sirmaenterprise.sep.monitor.dropwizard.servlet;

import javax.servlet.annotation.WebServlet;

import com.codahale.metrics.servlets.AdminServlet;

/**
 * Registers the admin servlet.
 *
 * @author BBonev
 */
@WebServlet(name = "metrics", urlPatterns = "/metrics/*")
public class LocalAdminServlet extends AdminServlet {

	private static final long serialVersionUID = 6348962995157485201L;
}
