package com.sirma.sep.keycloak.ldap;

import static com.sirma.sep.keycloak.ldap.LdapConstants.LDAP_PROVIDER_ID;
import static com.sirma.sep.keycloak.ldap.LdapConstants.LDAP_PROVIDER_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ComponentResource;
import org.keycloak.admin.client.resource.ComponentsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserStorageProviderResource;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.sep.keycloak.exception.KeycloakClientException;

/**
 * Tests for {@link KeycloakLdapProvider}.
 *
 * @author smustafov
 */
public class KeycloakLdapProviderTest {

	@InjectMocks
	private KeycloakLdapProvider ldapProvider;

	@Mock
	private LdapConfiguration ldapConfiguration;

	@Spy
	private KeycloakLdapModelRetriever ldapModelRetriever;

	@Mock
	private Keycloak keycloakClient;

	@Mock
	private RealmResource realmResource;

	@Mock
	private ComponentsResource componentsResource;

	@Mock
	private ComponentResource componentResource;

	@Mock
	private UserStorageProviderResource storageProviderResource;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
		mockLdapConfiguration();
	}

	@Test
	public void create_Should_CorrectlyCreateLdapProvider() {
		withRealm("sep.test");

		ldapProvider.createLdapProvider(keycloakClient, "sep.test");

		verifyLdapProvider();
	}

	@Test(expected = KeycloakClientException.class)
	public void create_Should_ThrowException_When_UsersDnHaveNoTenantPlaceholder() {
		withRealm("sep.test");

		ComponentRepresentation ldapComponent = new ComponentRepresentation();
		MultivaluedHashMap<String, String> ldapConfig = new MultivaluedHashMap<>();
		ldapConfig.putSingle(LdapConstants.USERS_DN, "ou=users,ou=sep.test,dc=sirmaitt,dc=bg");
		ldapComponent.setConfig(ldapConfig);
		when(ldapConfiguration.getLdapComponent()).thenReturn(ldapComponent);

		ldapProvider.createLdapProvider(keycloakClient, "sep.test");
	}

	@Test(expected = KeycloakClientException.class)
	public void create_Should_ThrowException_When_GroupsDnHaveNoTenantPlaceholder() {
		withRealm("sep.test");

		ComponentRepresentation groupComponent = new ComponentRepresentation();
		MultivaluedHashMap<String, String> groupConfig = new MultivaluedHashMap<>();
		groupConfig.putSingle(LdapConstants.GROUPS_DN, "ou=groups,ou=sep.test,dc=sirmaitt,dc=bg");
		groupComponent.setConfig(groupConfig);
		when(ldapConfiguration.getGroupMapperComponent()).thenReturn(groupComponent);

		ldapProvider.createLdapProvider(keycloakClient, "sep.test");
	}

	@Test
	public void delete_Should_CorrectlyRemoveLdapProviderComponent() {
		withRealm("sep.test");
		ComponentRepresentation componentRepresentation = new ComponentRepresentation();
		componentRepresentation.setId("comId");
		withComponent("comId", Collections.singletonList(componentRepresentation));

		ldapProvider.deleteLdapProvider(keycloakClient, "sep.test");

		verify(componentResource).remove();
	}

	@Test
	public void delete_Should_DoNothing_When_LdapProviderMissing() {
		withRealm("sep.test");
		withComponent("comId", Collections.emptyList());

		ldapProvider.deleteLdapProvider(keycloakClient, "sep.test");

		verify(componentResource, never()).remove();
	}

	private void verifyLdapProvider() {
		ArgumentCaptor<ComponentRepresentation> argumentCaptor = ArgumentCaptor.forClass(ComponentRepresentation.class);
		// 1 for ldap component, 1 for group mapping, 11 for the default user attribute mappings
		verify(componentsResource, times(13)).add(argumentCaptor.capture());

		List<ComponentRepresentation> allComponents = argumentCaptor.getAllValues();
		assertEquals(13, allComponents.size());

		verifyLdapComponent(allComponents.get(0));
		verifyGroupMapping(allComponents.get(1));
		verifySync();
	}

	private void verifyLdapComponent(ComponentRepresentation componentRepresentation) {
		assertEquals(LdapConstants.LDAP_PROVIDER_ID, componentRepresentation.getName());
		assertEquals(LdapConstants.LDAP_PROVIDER_ID, componentRepresentation.getProviderId());
		assertEquals(LdapConstants.LDAP_PROVIDER_TYPE, componentRepresentation.getProviderType());

		MultivaluedHashMap<String, String> config = componentRepresentation.getConfig();
		assertFalse(config.isEmpty());
		assertEquals("ou=users,ou=sep.test,dc=sirmaitt,dc=bg", config.getFirst(LdapConstants.USERS_DN));
		assertEquals(Boolean.TRUE.toString(), config.getFirst(LdapConstants.VALIDATE_PASSWORD_POLICY));
	}

	private void verifyGroupMapping(ComponentRepresentation componentRepresentation) {
		assertEquals("ldapCompId", componentRepresentation.getParentId());
		assertEquals(LdapConstants.GROUP_MAPPER_ID, componentRepresentation.getName());
		assertEquals(LdapConstants.GROUP_MAPPER_ID, componentRepresentation.getProviderId());
		assertEquals(LdapConstants.LDAP_MAPPER_PROVIDER_TYPE, componentRepresentation.getProviderType());

		MultivaluedHashMap<String, String> config = componentRepresentation.getConfig();
		assertFalse(config.isEmpty());
		assertEquals("ou=groups,ou=sep.test,dc=sirmaitt,dc=bg", config.getFirst(LdapConstants.GROUPS_DN));
	}

	private void verifySync() {
		verify(storageProviderResource).syncUsers("ldapCompId", KeycloakLdapProvider.TRIGGER_FULL_SYNC);
		verify(storageProviderResource)
				.syncMapperData("ldapCompId", "ldapCompId", KeycloakLdapProvider.FED_TO_KEYCLOAK);
	}

	private void withRealm(String realmId) {
		when(keycloakClient.realm(realmId)).thenReturn(realmResource);
		when(realmResource.components()).thenReturn(componentsResource);

		when(componentsResource.add(any(ComponentRepresentation.class)))
				.thenReturn(Response.created(URI.create("http://idp/auth/components/ldapCompId")).build());

		when(realmResource.userStorage()).thenReturn(storageProviderResource);
	}

	private void withComponent(String componentId, List<ComponentRepresentation> components) {
		when(componentsResource.query(anyString(), eq(LDAP_PROVIDER_TYPE), eq(LDAP_PROVIDER_ID)))
				.thenReturn(components);
		when(componentsResource.component(componentId)).thenReturn(componentResource);
	}

	private void mockLdapConfiguration() {
		ComponentRepresentation ldapComponent = new ComponentRepresentation();
		ldapComponent.setName(LdapConstants.LDAP_PROVIDER_ID);
		ldapComponent.setProviderId(LdapConstants.LDAP_PROVIDER_ID);
		ldapComponent.setProviderType(LdapConstants.LDAP_PROVIDER_TYPE);

		MultivaluedHashMap<String, String> ldapConfig = new MultivaluedHashMap<>();
		ldapConfig.putSingle(LdapConstants.USERS_DN,
				"ou=users,ou=" + KeycloakLdapProvider.TENANT_PLACEHOLDER + ",dc=sirmaitt,dc=bg");
		ldapComponent.setConfig(ldapConfig);
		when(ldapConfiguration.getLdapComponent()).thenReturn(ldapComponent);

		ComponentRepresentation groupMapperComponent = new ComponentRepresentation();
		groupMapperComponent.setName(LdapConstants.GROUP_MAPPER_ID);
		groupMapperComponent.setProviderId(LdapConstants.GROUP_MAPPER_ID);
		groupMapperComponent.setProviderType(LdapConstants.LDAP_MAPPER_PROVIDER_TYPE);

		MultivaluedHashMap<String, String> groupsConfig = new MultivaluedHashMap<>();
		groupsConfig.putSingle(LdapConstants.GROUPS_DN,
				"ou=groups,ou=" + KeycloakLdapProvider.TENANT_PLACEHOLDER + ",dc=sirmaitt,dc=bg");
		groupMapperComponent.setConfig(groupsConfig);
		when(ldapConfiguration.getGroupMapperComponent()).thenReturn(groupMapperComponent);
	}

}
