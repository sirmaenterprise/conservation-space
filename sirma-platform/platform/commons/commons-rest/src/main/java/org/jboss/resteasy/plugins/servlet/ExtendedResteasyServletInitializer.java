package org.jboss.resteasy.plugins.servlet;

import javax.servlet.annotation.HandlesTypes;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;

/**
 * Initialize Resteasy with additional hints for ignored packages
 * 
 * @author bbanchev
 */
@HandlesTypes({ Application.class, Path.class, Provider.class })
public class ExtendedResteasyServletInitializer extends ResteasyServletInitializer {
 
	/**
	 * Add additional packages to exclude during Resteasy resources scan. Method should be invoked in prior of
	 * initializing JAX-RS
	 * 
	 * @param pkgName
	 *            the package to exclude
	 */
	public static void addIgnoredPackage(String pkgName) {
		ignoredPackages.add(pkgName);
	}

}
