package com.sirma.sep.keycloak.tenant;

import static com.sirma.sep.keycloak.ClientProperties.OIDC_LOGIN_PROTOCOL;
import static com.sirma.sep.keycloak.ClientProperties.PROTOCOL_MAPPER_ACCESS_TOKEN;
import static com.sirma.sep.keycloak.ClientProperties.PROTOCOL_MAPPER_CLAIM_NAME;
import static com.sirma.sep.keycloak.ClientProperties.PROTOCOL_MAPPER_ID_TOKEN;
import static com.sirma.sep.keycloak.ClientProperties.PROTOCOL_MAPPER_SCRIPT;
import static com.sirma.sep.keycloak.ClientProperties.PROTOCOL_MAPPER_USER_ATTRIBUTE;
import static com.sirma.sep.keycloak.ClientProperties.PROTOCOL_MAPPER_USER_INFO_TOKEN;
import static com.sirma.sep.keycloak.ClientProperties.PROTOCOL_MAPPER_VALUE_TYPE;
import static com.sirma.sep.keycloak.ClientProperties.SCRIPT_PROTOCOL_MAPPER;
import static com.sirma.sep.keycloak.ClientProperties.SEP_BACKEND_CLIENT_ID;
import static com.sirma.sep.keycloak.ClientProperties.SEP_EAI_CLIENT_ID;
import static com.sirma.sep.keycloak.ClientProperties.SEP_UI_CLIENT_ID;
import static com.sirma.sep.keycloak.ClientProperties.TENANT_MAPPER_NAME;
import static com.sirma.sep.keycloak.ClientProperties.USERNAME_CLAIM_NAME;
import static com.sirma.sep.keycloak.ClientProperties.USERNAME_MAPPER_NAME;
import static com.sirma.sep.keycloak.ClientProperties.USER_MODEL_PROPERTY_MAPPER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.AuthenticationManagementResource;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.RolesRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.tenant.provision.IdpTenantInfo;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.keycloak.ClientProperties;
import com.sirma.sep.keycloak.config.KeycloakConfiguration;
import com.sirma.sep.keycloak.exception.KeycloakClientException;
import com.sirma.sep.keycloak.flow.KeycloakAuthFlowConfigurator;
import com.sirma.sep.keycloak.ldap.KeycloakLdapProvider;
import com.sirma.sep.keycloak.producers.KeycloakClientProducer;
import com.sirma.sep.keycloak.util.KeycloakApiUtil;

/**
 * Tests for {@link KeycloakTenantProvisioning}.
 *
 * @author smustafov
 */
public class KeycloakTenantProvisioningTest {

	private static final String TENAN_ID = "sep.test";
	private static final String TENANT_DISPLAY_NAME = "tenant display name";
	private static final String TENANT_DESCRIPTION = "tenant description";

	@InjectMocks
	private KeycloakTenantProvisioning keycloakTenantProvisioning;

	@Mock
	private KeycloakLdapProvider keycloakLdapProvider;

	@Mock
	private KeycloakConfiguration keycloakConfiguration;

	@Mock
	private SystemConfiguration systemConfiguration;

	@Mock
	private KeycloakClientProducer keycloakClientProducer;

	@Mock
	private KeycloakMailConfigurator keycloakMailConfigurator;

	@Mock
	private Keycloak keycloakClient;

	@Mock
	private RealmsResource realmsResource;

	@Mock
	private RealmResource realmResource;

	@Mock
	private GroupsResource groupsResource;

	@Mock
	private UsersResource usersResource;

	@Mock
	private UserResource userResource;

	@Mock
	private ClientsResource clientsResource;

	@Mock
	private RoleScopeResource roleScopeResource;

	@Mock
	private AuthenticationManagementResource authManagement;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
		mockConfigurations();
		when(keycloakClientProducer.produceClient()).thenReturn(keycloakClient);
	}

	@Test
	public void should_CreateCorrectTenant() {
		withTenant(TENAN_ID);
		withClient(KeycloakTenantProvisioning.REALM_MANAGEMENT_CLIENT_ID, KeycloakTenantProvisioning.REALM_ADMIN_ROLE);
		withGroup("adminGroup", KeycloakTenantProvisioning.REALM_MANAGEMENT_CLIENT_ID);
		withUser("admin");

		RealmRepresentation realmRepresentation = new RealmRepresentation();
		mockAuthManagement(realmRepresentation);

		keycloakTenantProvisioning.provision(
				createTenantInfo(TENAN_ID, "admin@" + TENAN_ID, "pass", "admin@mail.com", "System", "Administrator"));

		verifyTenant(TENAN_ID);
		assertEquals(KeycloakAuthFlowConfigurator.SEP_AUTH_FLOW, realmRepresentation.getBrowserFlow());
	}

	private void mockAuthManagement(RealmRepresentation realmRepresentation) {
		when(authManagement.createFlow(any())).thenReturn(
				Response.created(URI.create("http://idp/auth/flows/" + KeycloakAuthFlowConfigurator.SEP_AUTH_FLOW))
						.build());

		when(realmResource.toRepresentation()).thenReturn(realmRepresentation);
	}

	@Test
	public void should_ReadErrorMessageFromResponse_When_CreateFails() {
		withTenant(TENAN_ID);
		withClient(KeycloakTenantProvisioning.REALM_MANAGEMENT_CLIENT_ID, KeycloakTenantProvisioning.REALM_ADMIN_ROLE);
		withGroup("adminGroup", KeycloakTenantProvisioning.REALM_MANAGEMENT_CLIENT_ID);
		withUser("admin");
		mockAuthManagement(new RealmRepresentation());
		mockErrorResponse(KeycloakApiUtil.ERROR_DESCRIPTION_KEY, "invalid password");

		try {
			keycloakTenantProvisioning.provision(
					createTenantInfo(TENAN_ID, "admin@" + TENAN_ID, "pass", "admin@mail.com", "System",
							"Administrator"));
		} catch (KeycloakClientException e) {
			assertEquals("invalid password", e.getMessage());
		}
	}

	@Test(expected = KeycloakClientException.class)
	public void should_ThrowException_When_IdpOperationFails() {
		doThrow(NotFoundException.class).when(keycloakClient).realm(anyString());

		keycloakTenantProvisioning.provision(createTenantInfo(TENAN_ID, "admin@" + TENAN_ID, "pass", null, null, null));
	}

	@Test
	public void should_CorrectlyDeleteTenant() {
		withTenant(TENAN_ID);

		keycloakTenantProvisioning.delete(TENAN_ID);

		verify(keycloakLdapProvider).deleteLdapProvider(keycloakClient, TENAN_ID);
		verify(realmResource).remove();
	}

	@Test(expected = KeycloakClientException.class)
	public void delete_Should_ThrowException_When_IdpOperationFails() {
		withTenant(TENAN_ID);
		doThrow(InternalServerErrorException.class).when(keycloakLdapProvider)
				.deleteLdapProvider(keycloakClient, TENAN_ID);

		keycloakTenantProvisioning.delete(TENAN_ID);
	}

	@Test
	public void isApplicable_ShouldReturnTrue_When_IdpIsKeycloak() {
		assertTrue(keycloakTenantProvisioning.isApplicable(KeycloakTenantProvisioning.NAME));
	}

	@Test
	public void isApplicable_ShouldReturnFalse_When_IdpIsNotKeycloak() {
		assertFalse(keycloakTenantProvisioning.isApplicable("other"));
	}

	private void withTenant(String tenantId) {
		when(keycloakClient.realms()).thenReturn(realmsResource);

		when(keycloakClient.realm(tenantId)).thenReturn(realmResource);

		when(realmResource.groups()).thenReturn(groupsResource);
		when(realmResource.users()).thenReturn(usersResource);
		when(realmResource.clients()).thenReturn(clientsResource);

		when(realmResource.flows()).thenReturn(authManagement);
	}

	private void withClient(String clientId, String role) {
		ClientRepresentation clientRepresentation = new ClientRepresentation();
		clientRepresentation.setId(clientId);
		when(clientsResource.findByClientId(clientId)).thenReturn(Collections.singletonList(clientRepresentation));

		withClientRole(clientId, role);
	}

	private void withClientRole(String clientId, String role) {
		ClientResource clientResource = mock(ClientResource.class);

		RolesResource rolesResource = mock(RolesResource.class);

		RoleResource roleResource = mock(RoleResource.class);
		when(roleResource.toRepresentation()).thenReturn(new RoleRepresentation());

		when(rolesResource.get(role)).thenReturn(roleResource);

		when(clientResource.roles()).thenReturn(rolesResource);

		when(clientsResource.get(clientId)).thenReturn(clientResource);
	}

	private void withGroup(String groupId, String clientId) {
		when(groupsResource.add(any(GroupRepresentation.class)))
				.thenReturn(Response.created(URI.create("http://idp/auth/groups/" + groupId)).build());

		GroupResource groupResource = mock(GroupResource.class);

		RoleMappingResource groupRoles = mock(RoleMappingResource.class);

		when(groupRoles.clientLevel(clientId)).thenReturn(roleScopeResource);

		when(groupResource.roles()).thenReturn(groupRoles);

		when(groupsResource.group(groupId)).thenReturn(groupResource);
	}

	private void withUser(String userId) {
		when(usersResource.create(any(UserRepresentation.class)))
				.thenReturn(Response.created(URI.create("http://idp/auth/users/" + userId)).build());

		when(usersResource.get(userId)).thenReturn(userResource);
	}

	private void mockConfigurations() {
		when(keycloakConfiguration.getKeycloakUserRole()).thenReturn(new ConfigurationPropertyMock<>("user"));
		when(systemConfiguration.getUi2Url()).thenReturn(new ConfigurationPropertyMock<>("http://ui2:port/"));
	}

	private IdpTenantInfo createTenantInfo(String tenantId, String adminUsername, String adminPassword, String mail,
			String firstName, String lastName) {
		IdpTenantInfo tenantInfo = new IdpTenantInfo();
		tenantInfo.setTenantId(tenantId);
		tenantInfo.setTenantDescription(TENANT_DESCRIPTION);
		tenantInfo.setTenantDisplayName(TENANT_DISPLAY_NAME);
		tenantInfo.setAdminUsername(adminUsername);
		tenantInfo.setAdminPassword(adminPassword);
		tenantInfo.setAdminMail(mail);
		tenantInfo.setAdminFirstName(firstName);
		tenantInfo.setAdminLastName(lastName);
		return tenantInfo;
	}

	private void verifyTenant(String tenantId) {
		verifyRealm(tenantId);

		verify(keycloakLdapProvider).createLdapProvider(keycloakClient, tenantId);

		verifyAdminGroup();

		verifyAdminUser();

		verify(keycloakMailConfigurator).configureSmtpServer(any(RealmRepresentation.class));
	}

	private void verifyRealm(String tenantId) {
		ArgumentCaptor<RealmRepresentation> argumentCaptor = ArgumentCaptor.forClass(RealmRepresentation.class);
		verify(keycloakClient.realms()).create(argumentCaptor.capture());

		RealmRepresentation realm = argumentCaptor.getValue();

		assertEquals(tenantId, realm.getId());
		assertEquals(tenantId, realm.getRealm());
		assertEquals(TENANT_DISPLAY_NAME, realm.getDisplayName());
		assertEquals(TENANT_DESCRIPTION, realm.getDisplayNameHtml());
		assertEquals(ClientProperties.ONE_WEEK_IN_SECONDS, realm.getSsoSessionMaxLifespan().intValue());
		assertEquals(ClientProperties.SEP_THEME, realm.getLoginTheme());
		assertEquals(ClientProperties.SEP_THEME, realm.getEmailTheme());
		assertEquals(Arrays.asList("jboss-logging", "jms"), realm.getEventsListeners());
		assertEquals(KeycloakTenantProvisioning.DEFAULT_POLICY, realm.getPasswordPolicy());
		assertTrue(realm.isEnabled());
		assertTrue(realm.isDuplicateEmailsAllowed());
		assertTrue(realm.isRememberMe());
		assertTrue(realm.isResetPasswordAllowed());
		assertFalse(realm.isRegistrationAllowed());
		assertFalse(realm.isVerifyEmail());
		assertFalse(realm.isEditUsernameAllowed());

		verifyRoles(realm);
		verifyClients(realm);
	}

	private void verifyRoles(RealmRepresentation realm) {
		RolesRepresentation roles = realm.getRoles();
		assertNull(roles.getClient());
		List<RoleRepresentation> realmRoles = roles.getRealm();
		assertEquals("Should have only one role", 1, realmRoles.size());
		assertEquals("user", realmRoles.get(0).getName());

		List<String> defaultRoles = realm.getDefaultRoles();
		assertFalse(defaultRoles.isEmpty());
		assertEquals("user", defaultRoles.get(0));
	}

	private void verifyClients(RealmRepresentation realm) {
		List<ClientRepresentation> clients = realm.getClients();
		assertEquals(3, clients.size());

		ClientRepresentation backendClient = clients.get(0);
		assertEquals(SEP_BACKEND_CLIENT_ID, backendClient.getClientId());
		assertEquals(OIDC_LOGIN_PROTOCOL, backendClient.getProtocol());
		assertTrue(backendClient.isEnabled());
		assertTrue(backendClient.isBearerOnly());
		assertFalse(backendClient.isPublicClient());

		ClientRepresentation uiClient = clients.get(1);
		assertEquals(SEP_UI_CLIENT_ID, uiClient.getClientId());
		assertEquals(OIDC_LOGIN_PROTOCOL, uiClient.getProtocol());
		assertEquals("http://ui2:port/", uiClient.getBaseUrl());
		assertEquals(Collections.singletonList("http://ui2:port/*"), uiClient.getRedirectUris());
		assertEquals(Collections.singletonList("+"), uiClient.getWebOrigins());
		assertTrue(uiClient.isEnabled());
		assertTrue(uiClient.isPublicClient());
		assertTrue(uiClient.isStandardFlowEnabled());
		assertTrue(uiClient.isDirectAccessGrantsEnabled());
		assertFalse(uiClient.isFullScopeAllowed());
		assertFalse(uiClient.isBearerOnly());
		verifyProtocolMappers(uiClient);

		verifyEaiClient(clients.get(2));
	}

	private void verifyProtocolMappers(ClientRepresentation client) {
		List<ProtocolMapperRepresentation> protocolMappers = client.getProtocolMappers();
		assertEquals(2, protocolMappers.size());

		ProtocolMapperRepresentation tenantProtocolMapper = protocolMappers.get(0);
		Map<String, String> tenantProtocolMapperConfig = tenantProtocolMapper.getConfig();
		assertFalse(tenantProtocolMapper.isConsentRequired());
		assertEquals(TENANT_MAPPER_NAME, tenantProtocolMapper.getName());
		assertEquals(OIDC_LOGIN_PROTOCOL, tenantProtocolMapper.getProtocol());
		assertEquals(SCRIPT_PROTOCOL_MAPPER, tenantProtocolMapper.getProtocolMapper());
		assertEquals(TENANT_MAPPER_NAME, tenantProtocolMapperConfig.get(PROTOCOL_MAPPER_CLAIM_NAME));
		assertEquals("true", tenantProtocolMapperConfig.get(PROTOCOL_MAPPER_ACCESS_TOKEN));
		assertEquals("true", tenantProtocolMapperConfig.get(PROTOCOL_MAPPER_USER_INFO_TOKEN));
		assertEquals("true", tenantProtocolMapperConfig.get(PROTOCOL_MAPPER_ID_TOKEN));
		assertEquals("String", tenantProtocolMapperConfig.get(PROTOCOL_MAPPER_VALUE_TYPE));
		assertEquals("realm.getId();", tenantProtocolMapperConfig.get(PROTOCOL_MAPPER_SCRIPT));

		ProtocolMapperRepresentation usernameProtocolMapper = protocolMappers.get(1);
		Map<String, String> usernameProtocolMapperConfig = usernameProtocolMapper.getConfig();
		assertFalse(usernameProtocolMapper.isConsentRequired());
		assertEquals(USERNAME_MAPPER_NAME, usernameProtocolMapper.getName());
		assertEquals(OIDC_LOGIN_PROTOCOL, usernameProtocolMapper.getProtocol());
		assertEquals(USER_MODEL_PROPERTY_MAPPER, usernameProtocolMapper.getProtocolMapper());
		assertEquals(USERNAME_CLAIM_NAME, usernameProtocolMapperConfig.get(PROTOCOL_MAPPER_CLAIM_NAME));
		assertEquals("true", usernameProtocolMapperConfig.get(PROTOCOL_MAPPER_ACCESS_TOKEN));
		assertEquals("true", usernameProtocolMapperConfig.get(PROTOCOL_MAPPER_USER_INFO_TOKEN));
		assertEquals("true", usernameProtocolMapperConfig.get(PROTOCOL_MAPPER_ID_TOKEN));
		assertEquals("String", usernameProtocolMapperConfig.get(PROTOCOL_MAPPER_VALUE_TYPE));
		assertEquals(USERNAME_MAPPER_NAME, usernameProtocolMapperConfig.get(PROTOCOL_MAPPER_USER_ATTRIBUTE));
	}

	private void verifyEaiClient(ClientRepresentation client) {
		assertEquals(SEP_EAI_CLIENT_ID, client.getClientId());
		assertEquals(OIDC_LOGIN_PROTOCOL, client.getProtocol());
		assertEquals(Collections.singletonList("http://localhost:*"), client.getRedirectUris());
		assertTrue(client.isEnabled());
		assertTrue(client.isPublicClient());
		assertTrue(client.isStandardFlowEnabled());
		assertFalse(client.isFullScopeAllowed());
		assertFalse(client.isBearerOnly());
		verifyProtocolMappers(client);
	}

	private void verifyAdminGroup() {
		ArgumentCaptor<GroupRepresentation> groupCaptor = ArgumentCaptor.forClass(GroupRepresentation.class);
		verify(groupsResource).add(groupCaptor.capture());
		GroupRepresentation group = groupCaptor.getValue();
		assertEquals("admin", group.getName());

		ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);
		verify(roleScopeResource).add(argumentCaptor.capture());
		List<RoleRepresentation> roles = argumentCaptor.getValue();
		assertEquals(1, roles.size());
	}

	private void verifyAdminUser() {
		ArgumentCaptor<UserRepresentation> argumentCaptor = ArgumentCaptor.forClass(UserRepresentation.class);
		verify(usersResource).create(argumentCaptor.capture());

		UserRepresentation user = argumentCaptor.getValue();
		assertEquals("admin", user.getUsername());
		assertEquals("admin@mail.com", user.getEmail());
		assertEquals("System", user.getFirstName());
		assertEquals("Administrator", user.getLastName());
		assertTrue(user.isEnabled());

		verify(userResource).joinGroup("adminGroup");

		ArgumentCaptor<CredentialRepresentation> credentialCaptor = ArgumentCaptor
				.forClass(CredentialRepresentation.class);
		verify(userResource).resetPassword(credentialCaptor.capture());
		CredentialRepresentation passwordCredential = credentialCaptor.getValue();
		assertEquals("pass", passwordCredential.getValue());
		assertEquals(CredentialRepresentation.PASSWORD, passwordCredential.getType());
		assertFalse(passwordCredential.isTemporary());
	}

	private void mockErrorResponse(String errorKey, String errorMessage) {
		Response response = mock(Response.class);
		when(response.getStatusInfo()).thenReturn(Response.Status.BAD_REQUEST);
		when(response.getStatus()).thenReturn(Response.Status.BAD_REQUEST.getStatusCode());
		when(response.readEntity(String.class)).thenReturn("{\"" + errorKey + "\":\"" + errorMessage + "\"}");
		doThrow(new BadRequestException(response)).when(userResource)
				.resetPassword(any(CredentialRepresentation.class));
	}

}
