package com.sirmaenterprise.sep.bpm.camunda.security;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.Activator;
import com.sirma.itt.seip.rest.utils.ApiForwarder;
import com.sirma.itt.seip.security.configuration.SecurityExclusion;

/**
 * Extended RestApiSecurityExclusion from platform to include api paths for camunda engine.
 * 
 * @author bbanchev
 */
@Extension(target = SecurityExclusion.TARGET_NAME, order = 0.4, priority = 1)
public class CamundaRestApiSecurityExclusion implements SecurityExclusion {

	@Override
	public boolean isForExclusion(String path) {
		return checkApiPath(path);
	}

	private static boolean checkApiPath(String path) {
		return (path.startsWith(Activator.ROOT_PATH) && !isCamundaApiPath(path))
				|| path.startsWith(ApiForwarder.FORWARD_PATH);
	}

	private static boolean isCamundaApiPath(String path) {
		return path.startsWith("/api/admin") || path.startsWith("/api/engine") || path.startsWith("/api/cockpit") //NOSONAR
				|| path.startsWith("/api/tasklist") || path.startsWith("/api/welcome");
	}
}