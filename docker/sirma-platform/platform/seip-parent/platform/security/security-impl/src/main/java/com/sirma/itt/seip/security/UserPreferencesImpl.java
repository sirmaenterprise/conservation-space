package com.sirma.itt.seip.security;

import java.util.TimeZone;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Default {@link UserPreferences} implementation
 *
 * @author BBonev
 */
@ApplicationScoped
class UserPreferencesImpl implements UserPreferences {
	@Inject
	private SecurityContext securityContext;
	@Inject
	private SystemConfiguration systemConfigs;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "session.timeout.period", type = Integer.class, defaultValue = "30", label = "Define the session timeout time in minutes. The default value is 30.")
	private ConfigurationProperty<Integer> sessionTimeoutPeriod;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "user.recent.objects.size", type = Integer.class, defaultValue = "25", label = "Maximum number of recent objects to store.")
	private ConfigurationProperty<Integer> recentObjectsSize;

	/**
	 * Return the language for given user. If it set in the user info it is returned, if not the default system language
	 * {@link SecurityConfiguration#getSystemLanguage()} is returned. If user is null or not {@link User}, null is
	 * returned
	 *
	 * @return the language id
	 */
	@Override
	public String getLanguage() {
		User user = securityContext.getAuthenticated();
		if (user == null) {
			user = securityContext.getEffectiveAuthentication();
		}
		return getLanguage(user);
	}

	@Override
	public String getLanguage(User user) {
		if (user == null) {
			return systemConfigs.getSystemLanguage();
		}
		String language = user.getLanguage();
		if (language == null) {
			return systemConfigs.getSystemLanguage();
		}
		return language;
	}

	@Override
	public Integer getSessionTimeout() {
		return sessionTimeoutPeriod.get();
	}

	@Override
	public Integer getRecentObjectsSize() {
		return recentObjectsSize.get();
	}

	@Override
	public ConfigurationProperty<Integer> getSessionTimeoutPeriodConfig() {
		return sessionTimeoutPeriod;
	}

	@Override
	public TimeZone getUserTimezone() {
		// for now we does not support custom preferences
		// and will use the request time zone
		return securityContext.getAuthenticated().getTimezone();
	}
}
