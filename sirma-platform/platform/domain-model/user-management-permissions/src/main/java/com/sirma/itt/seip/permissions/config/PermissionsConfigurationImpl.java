package com.sirma.itt.seip.permissions.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;

/**
 * Implementation of {@link PermissionsConfiguration}.
 *
 *
 * @author siliev
 */
@Singleton
public class PermissionsConfigurationImpl implements PermissionsConfiguration {

	private static final long serialVersionUID = 6688590601243957464L;

	@ConfigurationPropertyDefinition(defaultValue = ActionTypeConstants.CREATE, system = true, type = String.class, label = "Action that the user has to have in Case when assinged a task")
	private static final String ACTION = "permissions.dynamic.assignrole.onmissingaction";

	@Inject
	@Configuration(ACTION)
	private ConfigurationProperty<String> actionValue;

	@Override
	public ConfigurationProperty<String> getDynamicPermissionThresholdAction() {
		return actionValue;
	}

}
