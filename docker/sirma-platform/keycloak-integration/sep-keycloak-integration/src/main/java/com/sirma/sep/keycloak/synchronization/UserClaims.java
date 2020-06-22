package com.sirma.sep.keycloak.synchronization;

/**
 * Constants for user claims (properties).
 *
 * @author smustafov
 */
public class UserClaims {

	private UserClaims() {
		// constants only
	}

	public static final String USERNAME = "username";
	public static final String EMAIL = "email";
	public static final String FIRST_NAME = "firstName";
	public static final String LAST_NAME = "lastName";
	public static final String IS_DISABLED = "isDisabled";
	public static final String CREATED_DATE = "createTimestamp";
	public static final String MODIFIED_DATE = "modifyTimestamp";
	public static final String COUNTRY = "country";
	public static final String STREET_ADDRESS = "streetAddress";
	public static final String MOBILE = "mobile";

}
