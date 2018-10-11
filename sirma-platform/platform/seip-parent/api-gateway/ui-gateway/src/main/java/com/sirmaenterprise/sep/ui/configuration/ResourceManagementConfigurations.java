package com.sirmaenterprise.sep.ui.configuration;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.resources.ResourceProperties;

/**
 * Configurations specific for the user and group management. Used by the UI in the user and group management tabs of
 * the administration page.
 *
 * @author smustafov
 */
@ApplicationScoped
public class ResourceManagementConfigurations {

	private static final String DEFAULT_USER_PROPERTIES = "{\"columns\":[\"" + ResourceProperties.USER_ID + "\",\""
			+ ResourceProperties.FIRST_NAME + "\", \"" + ResourceProperties.LAST_NAME + "\", \""
			+ ResourceProperties.EMAIL + "\"]}";

	private static final String DEFAULT_GROUP_PROPERTIES = "{\"columns\":[\"" + ResourceProperties.HAS_MEMBER + "\"]}";

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "user.management.user.properties", sensitive = true, defaultValue = DEFAULT_USER_PROPERTIES, label = "Properties of users that to be shown in the user management page")
	private ConfigurationProperty<Set<String>> userProperties;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "group.management.group.properties", sensitive = true, defaultValue = DEFAULT_GROUP_PROPERTIES, label = "Properties of groups that to be shown in the group management page")
	private ConfigurationProperty<Set<String>> groupProperties;

	/**
	 * Getter method for userProperties.
	 *
	 * @return the userProperties
	 */
	public ConfigurationProperty<Set<String>> getUserProperties() {
		return userProperties;
	}

	/**
	 * Getter method for groupProperties.
	 *
	 * @return the groupProperties
	 */
	public ConfigurationProperty<Set<String>> getGroupProperties() {
		return groupProperties;
	}

}
