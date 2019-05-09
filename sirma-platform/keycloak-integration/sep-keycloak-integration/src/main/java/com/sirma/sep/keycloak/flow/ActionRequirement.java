package com.sirma.sep.keycloak.flow;

/**
 * Defines whether or not, an action or authenticator will execute in an authentication flow.
 *
 * @author smustafov
 */
public enum ActionRequirement {

	/**
	 * This authentication execution must execute successfully.
	 * If the user doesn't have that type of authentication mechanism configured and there is a required action
	 * associated with that authentication type, then a required action will be attached to that account.
	 * For example, if two factor authentication is added as required, users that don't have an 2FA setup will be
	 * asked to do so.
	 */
	REQUIRED,

	/**
	 * If the user has the authentication type configured, it will be executed. Otherwise, it will be ignored.
	 */
	OPTIONAL,

	/**
	 * This means that at least one alternative authentication type must execute successfully at that level of the flow.
	 */
	ALTERNATIVE,

	/**
	 * If disabled, the authentication type is not executed.
	 */
	DISABLED

}
