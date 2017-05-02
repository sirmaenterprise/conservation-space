package com.sirma.itt.seip.rest.utils;

/**
 * Api version headers.
 * @author yasko
 *
 */
public final class Versions {
	private Versions() {
		// utility
	}
	
	/** legacy v1 */
	public static final String V1_JSON = "application/vnd.seip.v1+json";
	
	/** version 2 **/
	public static final String V2_JSON = "application/vnd.seip.v2+json";
	
	/** latest version **/
	public static final String LATEST_JSON = V2_JSON;
}
