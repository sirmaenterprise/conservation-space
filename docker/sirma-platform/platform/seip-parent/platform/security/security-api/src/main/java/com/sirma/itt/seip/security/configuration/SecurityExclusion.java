package com.sirma.itt.seip.security.configuration;

import com.sirma.itt.seip.plugin.Plugin;

/**
 * Extension that should be plugged when a resource (be it filter, servlet, etc.) has to be excluded from the generic
 * authentication filter for some reason. !!Note: it's highly recommended to provide alternative security mechanism.
 *
 * @author Adrian Mitev
 */
public interface SecurityExclusion extends Plugin {

	String TARGET_NAME = "SecurityExclusion";

	/**
	 * Checks if the path should be excluded from the generic authentication filter
	 *
	 * @param path
	 *            the path to check.
	 * @return true if the path should be excluded, false otherwise.
	 */
	boolean isForExclusion(String path);

}
