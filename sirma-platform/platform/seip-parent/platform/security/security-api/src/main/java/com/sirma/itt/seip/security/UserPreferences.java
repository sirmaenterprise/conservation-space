package com.sirma.itt.seip.security;

import java.util.TimeZone;

import com.sirma.itt.seip.configuration.ConfigurationProperty;

/**
 * The UserPreferences gives/updates the custom configuration for any user
 *
 * @author bbanchev
 */
public interface UserPreferences {

	/**
	 * Gets the user language.
	 *
	 * @return the language
	 */
	String getLanguage();

	/**
	 * Gets the language.
	 *
	 * @param user the user
	 * @return the languange
	 */
	String getLanguage(User user);

	/**
	 * Get the time zone for the currently logged in user based on his/her preferences.
	 *
	 * @return the time zone of the user
	 */
	TimeZone getUserTimezone();

	/**
	 * Gets the session timeout.
	 *
	 * @return the session timeout
	 */
	Integer getSessionTimeout();

	/**
	 * Maximum number of recently used objects to store.
	 *
	 * @return Configured maximum of recently objects to store (25 by default).
	 */
	Integer getRecentObjectsSize();

	/**
	 * Returns the session timeout period {@link ConfigurationProperty}.
	 *
	 * @return the session timeout period {@link ConfigurationProperty}
	 */
	ConfigurationProperty<Integer> getSessionTimeoutPeriodConfig();

}
