package com.sirmaenterprise.sep.bpm.camunda.web.filter;

import javax.servlet.DispatcherType;
import javax.servlet.annotation.WebFilter;

/**
 * {@link CamundaCacheControlFilter} registers a web filter with the default Camunda implementation.
 * 
 * @author bbanchev
 */
@WebFilter(dispatcherTypes = { DispatcherType.REQUEST }, urlPatterns = { "/api/engine/*", "/api/cockpit/*",
		"/api/tasklist/*", "/api/admin/*", "/api/welcome/*" })
public class CamundaCacheControlFilter extends org.camunda.bpm.engine.rest.filter.CacheControlFilter {
	// no custom implementation
}
