package com.sirma.itt.seip.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS {@link Application}.
 *
 * @author yasko
 */
@ApplicationPath(Activator.ROOT_PATH)
public class Activator extends Application {

	/**
	 * JAX-RS root path.
	 */
	public static final String ROOT_PATH = "/api";

	/** current api version **/
	public static final String API_VERSION = "1.0";
}
