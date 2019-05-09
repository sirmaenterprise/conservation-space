package com.sirma.sep.keycloak.ldap;

import static com.sirma.sep.keycloak.ldap.LdapConstants.GROUPS_DN;
import static com.sirma.sep.keycloak.ldap.LdapConstants.LDAP_MAPPER_PROVIDER_TYPE;
import static com.sirma.sep.keycloak.ldap.LdapConstants.LDAP_PROVIDER_ID;
import static com.sirma.sep.keycloak.ldap.LdapConstants.LDAP_PROVIDER_TYPE;

import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ComponentsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.sep.keycloak.exception.KeycloakClientException;
import com.sirma.sep.keycloak.util.KeycloakApiUtil;

/**
 * Keycloak LDAP provider that can create LDAP provider for a particular tenant.
 *
 * @author smustafov
 */
@ApplicationScoped
public class KeycloakLdapProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	static final String TENANT_PLACEHOLDER = "{tenantId}";
	static final String TRIGGER_FULL_SYNC = "triggerFullSync";
	static final String FED_TO_KEYCLOAK = "fedToKeycloak";

	@Inject
	private LdapConfiguration ldapConfiguration;

	@Inject
	private KeycloakLdapModelRetriever ldapModelRetriever;

	/**
	 * Creates LDAP provider for the given tenant. It consists of several steps:
	 * <ol>
	 * <li>creates ldap component for the realm</li>
	 * <li>removes keycloak default user attribute mappings</li>
	 * <li>creates a mapper for LDAP groups</li>
	 * <li>creates user attribute mappings defined for SEP</li>
	 * <li>runs synchronization from LDAP to Keycloak</li>
	 * </ol>
	 *
	 * @param keycloakClient the keycloak client
	 * @param tenantId       the tenant id for which LDAP provider that will be created
	 */
	public void createLdapProvider(Keycloak keycloakClient, String tenantId) {
		RealmResource realmResource = keycloakClient.realm(tenantId);

		// 1. create ldap component for the realm
		String ldapComponentId = createLdapComponent(realmResource, tenantId);

		// 2. remove default user attribute mappings
		removeDefaultMappings(realmResource, ldapComponentId);

		// 3. create a mapper for LDAP groups
		String groupMapperId = createGroupMapping(realmResource, tenantId, ldapComponentId);

		// 4. create user attribute mappings defined for SEP
		createUserAttributeMappings(realmResource, ldapComponentId);

		syncFromLDAP(realmResource, ldapComponentId, groupMapperId);
	}

	private String createLdapComponent(RealmResource realmResource, String tenantId) {
		ComponentRepresentation ldapComponent = ldapConfiguration.getLdapComponent();
		String usersDn = ldapComponent.getConfig().getFirst(LdapConstants.USERS_DN);
		if (!usersDn.contains(TENANT_PLACEHOLDER)) {
			throw new KeycloakClientException("No tenant placeholder found in users dn config");
		}
		ldapComponent.getConfig().putSingle(LdapConstants.USERS_DN, usersDn.replace(TENANT_PLACEHOLDER, tenantId));
		ldapComponent.getConfig().putSingle(LdapConstants.VALIDATE_PASSWORD_POLICY, Boolean.TRUE.toString());

		Response response = realmResource.components().add(ldapComponent);
		String ldapComponentId = KeycloakApiUtil.getCreatedId(response);
		// flush
		response.close();

		return ldapComponentId;
	}

	private void removeDefaultMappings(RealmResource realmResource, String ldapComponentId) {
		ComponentsResource componentsResource = realmResource.components();
		List<ComponentRepresentation> defaultMappers = componentsResource
				.query(ldapComponentId, LDAP_MAPPER_PROVIDER_TYPE);

		for (ComponentRepresentation defaultMapper : defaultMappers) {
			componentsResource.component(defaultMapper.getId()).remove();
		}
	}

	private String createGroupMapping(RealmResource realmResource, String tenantId, String ldapComponentId) {
		ComponentRepresentation ldapGroupMapperSubComponent = ldapConfiguration.getGroupMapperComponent();
		ldapGroupMapperSubComponent.setParentId(ldapComponentId);

		String groupsDn = ldapGroupMapperSubComponent.getConfig().getFirst(GROUPS_DN);
		if (!groupsDn.contains(TENANT_PLACEHOLDER)) {
			throw new KeycloakClientException("No tenant placeholder found in groups dn config");
		}
		ldapGroupMapperSubComponent.getConfig().putSingle(GROUPS_DN, groupsDn.replace(TENANT_PLACEHOLDER, tenantId));

		Response response = realmResource.components().add(ldapGroupMapperSubComponent);
		String mapperId = KeycloakApiUtil.getCreatedId(response);
		// flush, if not flushed creating of user attribute mappings is very slow
		response.close();
		return mapperId;
	}

	private void createUserAttributeMappings(RealmResource realmResource, String ldapComponentId) {
		ComponentsResource componentsResource = realmResource.components();
		List<ComponentRepresentation> mappers = ldapModelRetriever.retrieve(ldapComponentId);

		TimeTracker timeTracker = TimeTracker.createAndStart();
		for (ComponentRepresentation mapper : mappers) {
			componentsResource.add(mapper).close();
		}
		LOGGER.info("Finished adding model mappers in {} seconds", timeTracker.stopInSeconds());
	}

	private void syncFromLDAP(RealmResource realmResource, String ldapComponentId, String groupMapperId) {
		realmResource.userStorage().syncUsers(ldapComponentId, TRIGGER_FULL_SYNC);
		realmResource.userStorage().syncMapperData(ldapComponentId, groupMapperId, FED_TO_KEYCLOAK);
	}

	/**
	 * Deletes LDAP provider for the given tenant. Also deletes all data for the tenant from the underlying LDAP.
	 *
	 * @param keycloakClient the keycloak client
	 * @param tenantId       the tenant id for which the LDAP will be deleted
	 */
	public void deleteLdapProvider(Keycloak keycloakClient, String tenantId) {
		RealmResource realm = keycloakClient.realm(tenantId);
		ComponentsResource components = realm.components();

		List<ComponentRepresentation> ldapComponents = components.query(tenantId, LDAP_PROVIDER_TYPE, LDAP_PROVIDER_ID);
		if (CollectionUtils.isEmpty(ldapComponents)) {
			LOGGER.warn("Ldap provider not found. Probably not created in first place. Skipping it");
			return;
		}

		components.component(ldapComponents.get(0).getId()).remove();
	}

}
