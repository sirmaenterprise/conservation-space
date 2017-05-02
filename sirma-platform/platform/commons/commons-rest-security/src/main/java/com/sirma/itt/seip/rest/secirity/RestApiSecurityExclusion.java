package com.sirma.itt.seip.rest.secirity;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.Activator;
import com.sirma.itt.seip.rest.utils.ApiForwarder;
import com.sirma.itt.seip.security.configuration.SecurityExclusion;

/**
 * Exclusion for REST services.
 *
 * @author Adrian Mitev
 */
@Extension(target = SecurityExclusion.TARGET_NAME, order = 0.4)
public class RestApiSecurityExclusion implements SecurityExclusion {

	@Override
	public boolean isForExclusion(String path) {
		return path.startsWith(Activator.ROOT_PATH) || path.startsWith(ApiForwarder.FORWARD_PATH);
	}
}