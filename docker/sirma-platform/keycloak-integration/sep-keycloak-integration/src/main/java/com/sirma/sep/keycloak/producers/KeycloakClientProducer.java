package com.sirma.sep.keycloak.producers;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.util.SecurityUtil;
import com.sirma.sep.keycloak.config.KeycloakConfiguration;

/**
 * Keycloak integration component that produces beans for Keycloak REST API client.
 * This is a singleton which stores cache with one client per tenant. The tenant is resolved from the security context.
 * <p>
 * The keycloak client manages expired tokens itself. The token is refreshed if its expired just before sending requests
 * to the idp.
 *
 * @author smustafov
 */
@Singleton
public class KeycloakClientProducer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private KeycloakConfiguration keycloakConfiguration;

	@Inject
	private SecurityConfiguration securityConfiguration;

	@Inject
	private SecurityContext securityContext;

	private Map<String, Keycloak> clientsCache = new ConcurrentHashMap<>();

	/**
	 * Produces {@link Keycloak} client.
	 *
	 * @return keycloak client
	 */
	public Keycloak produceClient() {
		return clientsCache.computeIfAbsent(securityContext.getCurrentTenantId(), this::initClient);
	}

	private Keycloak initClient(String tenantId) {
		String keycloakAddress = keycloakConfiguration.getKeycloakAddress().get();
		String adminClientId = keycloakConfiguration.getKeycloakAdminClientId().get();

		String adminUsername = securityConfiguration.getAdminUserName().getOrFail();
		String adminPassword = securityConfiguration.getAdminUserPassword().getOrFail();

		if (SecurityContext.isSystemTenant(tenantId)) {
			tenantId = keycloakConfiguration.getKeycloakSystemTenantId().get();
			adminUsername = securityConfiguration.getSystemAdminUsername();
		}

		adminUsername = SecurityUtil.getUserWithoutTenant(adminUsername);

		LOGGER.debug("Producing keycloak client: address={}, tenant={}, username={}, clientId={}", keycloakAddress,
				tenantId, adminUsername, adminClientId);

		return Keycloak.getInstance(keycloakAddress, tenantId, adminUsername, adminPassword, adminClientId);
	}

	/**
	 * Produces {@link RealmResource} which can be used for managing the current tenant in idp.
	 *
	 * @return the realm resource
	 */
	public RealmResource produceRealmResource() {
		String currentTenantId = securityContext.getCurrentTenantId();
		LOGGER.debug("Producing realm resource for {}", currentTenantId);
		return produceClient().realm(currentTenantId);
	}

	/**
	 * Produces {@link UsersResource} which can be used for managing the current tenant's users in idp.
	 *
	 * @return the users resource
	 */
	public UsersResource produceUsersResource() {
		return produceRealmResource().users();
	}

	/**
	 * Produces {@link GroupsResource} which can be used for managing the current tenant's groups in idp.
	 *
	 * @return the groups resource
	 */
	public GroupsResource produceGroupsResource() {
		return produceRealmResource().groups();
	}

}
