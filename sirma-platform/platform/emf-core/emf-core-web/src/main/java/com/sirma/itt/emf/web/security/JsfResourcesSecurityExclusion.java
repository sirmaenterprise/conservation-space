package com.sirma.itt.emf.web.security;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.configuration.SecurityExclusion;

/**
 * Security exclusion for JSF resources.
 *
 * @author Adrian Mitev
 */
@Extension(target = SecurityExclusion.TARGET_NAME, order = 0.1)
public class JsfResourcesSecurityExclusion implements SecurityExclusion {

	@Override
	public boolean isForExclusion(String path) {
		return path.startsWith("/javax.faces.resource") || path.startsWith("/rfRes");
	}

}
