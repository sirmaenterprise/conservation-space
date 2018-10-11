package com.sirma.itt.seip.resources;

import java.security.Principal;
import java.util.TimeZone;

/**
 * Interface identifying a user in the CMF system in order to calculate his roles and actions.
 *
 * @author BBonev
 */
public interface User extends Principal, Resource, com.sirma.itt.seip.security.User {

	/**
	 * Gets user tenant id.
	 *
	 * @return the tenant id
	 */
	@Override
	String getTenantId();

	/**
	 * Sets the tenant id.
	 *
	 * @param tenantId
	 *            the new tenant id
	 */
	void setTenantId(String tenantId);

	/**
	 * Gets the preferred user language.
	 *
	 * @return the language
	 */
	@Override
	String getLanguage();

	/**
	 * Set preferred user language
	 *
	 * @param language the language code to set
	 */
	void setLanguage(String language);

	/**
	 * Gets the main email address where the user could be contacted
	 *
	 * @return the email address
	 */
	@Override
	String getEmail();

	/**
	 * Updates the main user email address
	 *
	 * @param email the mail to set
	 */
	@Override
	void setEmail(String email);

	/**
	 * Sets the time preferred user time zone
	 *
	 * @param timezone to set
	 */
	void setTimezone(TimeZone timezone);
}
