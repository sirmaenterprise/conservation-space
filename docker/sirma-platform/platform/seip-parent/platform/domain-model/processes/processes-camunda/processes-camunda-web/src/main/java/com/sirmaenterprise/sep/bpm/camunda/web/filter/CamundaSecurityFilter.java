package com.sirmaenterprise.sep.bpm.camunda.web.filter;

import java.io.IOException;

import javax.servlet.DispatcherType;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;

import org.camunda.bpm.webapp.impl.security.filter.util.FilterRules;

/***
 * The Class {@link CamundaSecurityFilter} registers the provided configuration and delegates execution to default
 * implementation.
 * 
 * @author bbanchev
 */
@WebFilter(dispatcherTypes = { DispatcherType.REQUEST }, urlPatterns = { "/app/*", "/api/engine/*", "/api/cockpit/*",
		"/api/tasklist/*", "/api/admin/*", "/api/welcome/*" })
public class CamundaSecurityFilter extends org.camunda.bpm.webapp.impl.security.filter.SecurityFilter {

	@Override
	protected void loadFilterRules(FilterConfig filterConfig) throws ServletException {
		try {
			filterRules = FilterRules.load(CamundaSecurityFilter.class.getResourceAsStream("securityFilterRules.json"));
		} catch (IOException e) {
			throw new ServletException(e);
		}
	}

}
