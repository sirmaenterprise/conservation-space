package com.sirma.sep.keycloak.migration;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.tenant.provision.IdpTenantInfo;
import com.sirma.sep.keycloak.producers.KeycloakClientProducer;
import com.sirma.sep.keycloak.session.SessionConfigChangeListener;
import com.sirma.sep.keycloak.tenant.KeycloakMailConfigurator;
import com.sirma.sep.keycloak.tenant.KeycloakTenantProvisioning;
import com.sirma.sep.keycloak.util.KeycloakApiUtil;

/**
 * Keycloak tenant migration implementation.
 * <p>
 * Extends {@link KeycloakTenantProvisioning} by changing the tenant provisioning for migration of tenants from WSO2 IDP:
 * - skips the creation of admin user and admin group, because they already are created in LDAP
 * - for admin group assigns realm admin role, so members of the group can have admin rights in Keycloak also
 * <p>
 * Besides the tenant provisioning also contains methods for various steps which are part of the migration process.
 *
 * @author smustafov
 */
@ApplicationScoped
public class KeycloakTenantMigration extends KeycloakTenantProvisioning {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private KeycloakClientProducer keycloakClientProducer;

	@Inject
	private KeycloakMailConfigurator keycloakMailConfigurator;

	/**
	 * Deactivates the given users in Keycloak from the current tenant. This is needed because Keycloak and LDAP are
	 * not linked which users are not allowed to login.
	 *
	 * @param users list of users to deactivate
	 */
	public void deactivateUsers(List<User> users) {
		RealmResource realmResource = keycloakClientProducer.produceRealmResource();
		UsersResource usersResource = realmResource.users();

		for (User user : users) {
			Optional<UserRepresentation> remoteUser = KeycloakApiUtil
					.getRemoteUser(usersResource, user.getIdentityId());

			if (remoteUser.isPresent()) {
				UserRepresentation userRepresentation = remoteUser.get();
				userRepresentation.setEnabled(false);
				usersResource.get(userRepresentation.getId()).update(userRepresentation);
				LOGGER.info("Deactivated user in keycloak: {}", user.getIdentityId());
			} else {
				LOGGER.info("Tried to deactivate non existing user: {}", user.getIdentityId());
			}
		}
	}

	/**
	 * Updates the session timeout value.
	 *
	 * @param timeout the session timeout value in minutes
	 */
	public void updateSessionConfig(int timeout) {
		SessionConfigChangeListener.updateSessionConfig(timeout, keycloakClientProducer.produceRealmResource());
	}

	/**
	 * Updates mail settings in keycloak with the ones configured for the current tenant.
	 */
	public void updateMailSettings() {
		RealmResource realmResource = keycloakClientProducer.produceRealmResource();
		RealmRepresentation realmRepresentation = realmResource.toRepresentation();

		keycloakMailConfigurator.configureSmtpServer(realmRepresentation);

		realmResource.update(realmRepresentation);
	}

	@Override
	protected String createAdminGroup(Keycloak keycloakClient, String tenantId) {
		// only assign admin role to the admin group, the group is already created in LDAP
		RealmResource realm = keycloakClient.realm(tenantId);
		String adminGroupId = KeycloakApiUtil.retrieveGroupId(realm.groups(), "admin");
		assignAdminRole(realm, adminGroupId);
		return null;
	}

	@Override
	protected void createAdminUser(Keycloak keycloakClient, IdpTenantInfo tenantInfo, String adminGroupId) {
		// already created in LDAP
	}
}
