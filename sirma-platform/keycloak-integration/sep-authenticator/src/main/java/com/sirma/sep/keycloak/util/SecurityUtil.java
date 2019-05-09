package com.sirma.sep.keycloak.util;

/**
 * Contains utility methods for security.
 *
 * @author smustafov
 */
public class SecurityUtil {

	public static final char TENANT_ID_SEPARATOR = '@';

	/**
	 * Gets the user and tenant from full userId.
	 *
	 * @param username the username in format user@domain
	 * @return the user and tenant as first/second
	 */
	public static String[] getUserAndTenant(String username) {
		if (hasTenant(username)) {
			return username.split(Character.toString(TENANT_ID_SEPARATOR));
		}
		return new String[] { username, null };
	}

	private static boolean hasTenant(String username) {
		return (username != null) && (username.indexOf(TENANT_ID_SEPARATOR) > -1);
	}

	private SecurityUtil() {
		// utility
	}

}
