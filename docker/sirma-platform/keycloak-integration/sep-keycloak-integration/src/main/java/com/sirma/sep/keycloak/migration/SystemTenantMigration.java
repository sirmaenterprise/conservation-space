package com.sirma.sep.keycloak.migration;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;

import javax.inject.Inject;
import javax.ws.rs.NotAuthorizedException;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.resources.synchronization.RemoteUserStoreAdapterProxy;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.annotation.RunAsSystem;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.sep.keycloak.config.KeycloakConfiguration;
import com.sirma.sep.keycloak.util.KeycloakApiUtil;

/**
 * Migrates system tenant to Keycloak IdP.
 * <p>
 * Changes the password of systemadmin user to the one saved in relational db. That's because the system tenant is not
 * linked with LDAP anymore.
 * <p>
 * Changes idp configurations to Keycloak.
 *
 * @author smustafov
 */
public class SystemTenantMigration {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private KeycloakConfiguration keycloakConfiguration;

	@Inject
	private SecurityConfiguration securityConfiguration;

	@Inject
	private ConfigurationManagement configurationManagement;

	@Inject
	private RemoteUserStoreAdapterProxy userStoreAdapterProxy;

	private ClientProvider clientProvider = Keycloak::getInstance;

	@RunAsSystem(protectCurrentTenant = false)
	@Startup(phase = StartupPhase.BEFORE_APP_START)
	void migrate() {
		if (SecurityConfiguration.WSO_IDP.equals(securityConfiguration.getIdpProviderName().get())) {
			changeSystemAdminPasswordIfNeeded(securityConfiguration.getSystemAdminUsername(),
					securityConfiguration.getAdminUserPassword().get());

			changeIdpConfig();
		}
	}

	private void changeSystemAdminPasswordIfNeeded(String systemAdmin, String localPassword) {
		try {
			buildClient(systemAdmin, localPassword).serverInfo().getInfo();
		} catch (NotAuthorizedException e) {
			LOGGER.info("Changing the password of {}", systemAdmin);
			changeSystemAdminPassword(systemAdmin, localPassword);
		}
	}

	private Keycloak buildClient(String username, String password) {
		return clientProvider.provide(keycloakConfiguration.getKeycloakAddress().get(),
				keycloakConfiguration.getKeycloakSystemTenantId().get(), username, password,
				keycloakConfiguration.getKeycloakAdminClientId().get());
	}

	private void changeSystemAdminPassword(String systemAdmin, String localPassword) {
		Keycloak keycloakClient = buildClient(systemAdmin, "admin");
		RealmResource realmResource = keycloakClient.realm(keycloakConfiguration.getKeycloakSystemTenantId().get());
		UsersResource usersResource = realmResource.users();

		String systemAdminId = KeycloakApiUtil.retrieveUserId(usersResource, systemAdmin);

		CredentialRepresentation credential = new CredentialRepresentation();
		credential.setValue(localPassword);
		credential.setType(CredentialRepresentation.PASSWORD);
		credential.setTemporary(false);

		usersResource.get(systemAdminId).resetPassword(credential);
	}

	private void changeIdpConfig() {
		Configuration idpProviderConfig = new Configuration(securityConfiguration.getIdpProviderName().getName(),
				SecurityConfiguration.KEYCLOAK_IDP);
		Configuration syncProviderConfig = new Configuration(
				userStoreAdapterProxy.getSynchronizationProviderName().getName(), SecurityConfiguration.KEYCLOAK_IDP);

		configurationManagement.updateSystemConfigurations(Arrays.asList(idpProviderConfig, syncProviderConfig));

		LOGGER.info("Changed idp configurations to {}", SecurityConfiguration.KEYCLOAK_IDP);
	}

	void setClientProvider(ClientProvider clientProvider) {
		this.clientProvider = clientProvider;
	}

	@FunctionalInterface
	interface ClientProvider {
		Keycloak provide(String address, String tenantId, String username, String password, String clientId);
	}

}
