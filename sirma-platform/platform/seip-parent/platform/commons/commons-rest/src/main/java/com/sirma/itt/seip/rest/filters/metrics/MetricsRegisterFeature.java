package com.sirma.itt.seip.rest.filters.metrics;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Rest feature that will register metrics for endpoints
 *
 * @author BBonev
 */
@Provider
public class MetricsRegisterFeature implements DynamicFeature {

	@Inject
	private Statistics statistics;
	@Inject
	private SecurityContext securityContext;

	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext context) {
		String path = buildRequestPath(resourceInfo);
		if (path == null) {
			return;
		}
		context.register(new MetricsRequestFilter(path, statistics, securityContext));
	}

	private static String buildRequestPath(ResourceInfo resourceInfo) {
		StringBuilder path = new StringBuilder(50);
		Path mainPath = resourceInfo.getResourceClass().getAnnotation(Path.class);
		Path methodPath = resourceInfo.getResourceMethod().getAnnotation(Path.class);
		if (mainPath != null) {
			path.append(mainPath.value());
		}
		path.append('/');
		if (methodPath != null) {
			path.append(methodPath.value());
		}
		String fullPath = path.toString().replaceAll("/+", "/");
		if (fullPath.startsWith("/")) {
			fullPath = fullPath.substring(1);
		}
		if (fullPath.endsWith("/")) {
			fullPath = fullPath.substring(0, fullPath.length() - 1);
		}
		if (fullPath.isEmpty()) {
			return null;
		}
		return fullPath;
	}
}
