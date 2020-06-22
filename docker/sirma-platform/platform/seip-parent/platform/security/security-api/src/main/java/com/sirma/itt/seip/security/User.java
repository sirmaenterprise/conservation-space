package com.sirma.itt.seip.security;

import java.io.Serializable;
import java.util.Map;
import java.util.TimeZone;

import com.sirma.itt.seip.security.authentication.Authenticator;

/**
 * Identifies security identity. Represents an authenticated identity. It's produces from
 * {@link Authenticator#authenticate(com.sirma.itt.seip.security.authentication.AuthenticationContext)} method.
 *
 * @author BBonev
 */
public interface User extends Serializable {

	/**
	 * Gets the unique id of the user in the system. This is in database format id.
	 *
	 * @return the system id
	 */
	Serializable getSystemId();

	/**
	 * Gets the user identity id. This is the is used by the user for authentication. Includes the tenant id.
	 *
	 * @return the identity id
	 */
	String getIdentityId();

	/**
	 * Gets the tenant id.
	 *
	 * @return the tenant id
	 */
	String getTenantId();

	/**
	 * Gets the display name.
	 *
	 * @return the display name
	 */
	String getDisplayName();

	/**
	 * Gets the preferred user language.
	 *
	 * @return the language
	 */
	String getLanguage();

	/**
	 * Get the user time zone during the current request
	 *
	 * @return the time zone, should not be null.
	 */
	TimeZone getTimezone();

	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
	Map<String, Serializable> getProperties();

	/**
	 * Return the authentication ticket.
	 *
	 * @return ticket
	 */
	String getTicket();

	/**
	 * Gets the credentials.
	 *
	 * @return the credentials
	 */
	Object getCredentials();

	/**
	 * Checks if is active and could perform operations with data
	 *
	 * @return true, if is active
	 */
	boolean isActive();

	/**
	 * Specifies if user could perform login operation in the system. The user potentially could be active but to be
	 * forbidden from logging in. This is generally the case for internal system users.
	 * <p>
	 * The default implementation allow active users to log in
	 *
	 * @return true, if allowed to login
	 */
	default boolean canLogin() {
		return isActive();
	}
}
