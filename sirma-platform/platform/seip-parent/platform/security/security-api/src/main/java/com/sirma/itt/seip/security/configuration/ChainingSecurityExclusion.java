/**
 *
 */
package com.sirma.itt.seip.security.configuration;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.plugin.ExtensionPoint;

/**
 * Provides convenient method for security exclusion checking.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class ChainingSecurityExclusion implements SecurityExclusion {

	@Inject
	@ExtensionPoint(SecurityExclusion.TARGET_NAME)
	private Iterable<SecurityExclusion> exclusions;

	@Override
	public boolean isForExclusion(String path) {
		for (SecurityExclusion current : exclusions) {
			if (current.isForExclusion(path)) {
				return true;
			}
		}
		return false;
	}

}
