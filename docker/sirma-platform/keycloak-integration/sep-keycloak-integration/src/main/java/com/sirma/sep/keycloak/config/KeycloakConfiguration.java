package com.sirma.sep.keycloak.config;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

/**
 * Keycloak IdP configurations.
 *
 * @author smustafov
 */
@ApplicationScoped
public class KeycloakConfiguration {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "keycloak.address", defaultValue = "http://keycloak:8080/auth",
			sensitive = true, system = true, label = "Address of Keycloak IdP in format: <protocol>://<host>:<port>/auth")
	private ConfigurationProperty<String> keycloakAddress;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "keycloak.admin.client.id", defaultValue = "admin-cli", sensitive = true,
			system = true, label = "Id of the admin-cli client which has tenant management permissions. Default value: admin-cli")
	private ConfigurationProperty<String> keycloakAdminClientId;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "keycloak.system.tenant.id", defaultValue = "master", sensitive = true,
			system = true, label = "Id of the system tenant in Keycloak. Default value: master")
	private ConfigurationProperty<String> keycloakSystemTenantId;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "keycloak.user.role", defaultValue = "user", sensitive = true,
			system = true, label = "Default role that all users will have in order to authenticate. Default value: user")
	private ConfigurationProperty<String> keycloakUserRole;

	public ConfigurationProperty<String> getKeycloakAddress() {
		return keycloakAddress;
	}

	public ConfigurationProperty<String> getKeycloakAdminClientId() {
		return keycloakAdminClientId;
	}

	public ConfigurationProperty<String> getKeycloakSystemTenantId() {
		return keycloakSystemTenantId;
	}

	public ConfigurationProperty<String> getKeycloakUserRole() {
		return keycloakUserRole;
	}

}
