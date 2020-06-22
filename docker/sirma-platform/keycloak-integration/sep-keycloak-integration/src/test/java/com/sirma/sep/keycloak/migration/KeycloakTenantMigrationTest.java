package com.sirma.sep.keycloak.migration;

import static com.sirma.sep.keycloak.tenant.KeycloakTenantProvisioning.REALM_ADMIN_ROLE;
import static com.sirma.sep.keycloak.tenant.KeycloakTenantProvisioning.REALM_MANAGEMENT_CLIENT_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.sep.keycloak.exception.KeycloakClientException;
import com.sirma.sep.keycloak.producers.KeycloakClientProducer;
import com.sirma.sep.keycloak.tenant.KeycloakMailConfigurator;

/**
 * Tests for {@link KeycloakTenantMigration}.
 *
 * @author smustafov
 */
public class KeycloakTenantMigrationTest {

	@InjectMocks
	private KeycloakTenantMigration migration;

	@Mock
	private KeycloakClientProducer keycloakClientProducer;

	@Mock
	private KeycloakMailConfigurator keycloakMailConfigurator;

	@Mock
	private Keycloak keycloakClient;

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

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
		when(keycloakClientProducer.produceRealmResource()).thenReturn(realmResource);
		when(keycloakClient.realm(anyString())).thenReturn(realmResource);
		when(realmResource.users()).thenReturn(usersResource);
		when(realmResource.groups()).thenReturn(groupsResource);
		when(realmResource.clients()).thenReturn(clientsResource);
	}

	@Test
	public void deactivateUsers_Should_DeactivateRemoteUsers() {
		mockRemoteUsers();

		migration.deactivateUsers(Arrays.asList(new EmfUser("regularuser@sep.test"), new EmfUser("user1@sep.test")));

		ArgumentCaptor<UserRepresentation> argumentCaptor = ArgumentCaptor.forClass(UserRepresentation.class);
		verify(userResource, times(2)).update(argumentCaptor.capture());

		argumentCaptor.getAllValues().forEach(user -> assertFalse(user.isEnabled()));
	}

	@Test
	public void deactivateUsers_Should_DoNothing_When_RemoteUserNotFound() {
		mockRemoteUsers();

		migration.deactivateUsers(Arrays.asList(new EmfUser("regularuser@sep.test"), new EmfUser("user2@sep.test")));

		verify(userResource).update(any());
	}

	@Test
	public void updateSessionConfig_Should_ProperlyUpdateRealm() {
		RealmRepresentation realmRepresentation = new RealmRepresentation();
		when(realmResource.toRepresentation()).thenReturn(realmRepresentation);

		migration.updateSessionConfig(30);

		assertEquals(Integer.valueOf(1800), realmRepresentation.getSsoSessionIdleTimeout());
	}

	@Test
	public void updateMailSettings_Should_ProperlyUpdateRealm() {
		RealmRepresentation realmRepresentation = new RealmRepresentation();
		when(realmResource.toRepresentation()).thenReturn(realmRepresentation);

		migration.updateMailSettings();

		verify(keycloakMailConfigurator).configureSmtpServer(realmRepresentation);
		verify(realmResource).update(realmRepresentation);
	}

	@Test
	public void createAdminGroup_Should_ProperlyAssignAdminRole() {
		when(groupsResource.groups("admin", null, null))
				.thenReturn(Collections.singletonList(createRemoteAdminGroup()));
		mockAdminClient();

		migration.createAdminGroup(keycloakClient, "sep.test");

		verifyAdminRole();
	}

	@Test(expected = KeycloakClientException.class)
	public void createAdminGroup_Should_ThrowException_When_CannotFindAdminGroup() {
		when(groupsResource.groups("admin", null, null)).thenReturn(Collections.emptyList());
		migration.createAdminGroup(keycloakClient, "sep.test");
	}

	private void verifyAdminRole() {
		ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);
		verify(roleScopeResource).add(argumentCaptor.capture());
		List<RoleRepresentation> roles = argumentCaptor.getValue();
		assertEquals(1, roles.size());
	}

	private void mockRemoteUsers() {
		UserRepresentation remoteUser1 = createRemoteUser("regularuser");
		UserRepresentation remoteUser2 = createRemoteUser("user1");
		when(usersResource.search(anyString())).thenReturn(Arrays.asList(remoteUser1, remoteUser2));
	}

	private UserRepresentation createRemoteUser(String username) {
		UserRepresentation userRepresentation = new UserRepresentation();
		userRepresentation.setUsername(username);
		userRepresentation.setId(username);
		userRepresentation.setEnabled(true);
		when(usersResource.get(username)).thenReturn(userResource);
		return userRepresentation;
	}

	private GroupRepresentation createRemoteAdminGroup() {
		GroupRepresentation groupRepresentation = new GroupRepresentation();
		groupRepresentation.setName("admin");
		groupRepresentation.setId("admin");
		return groupRepresentation;
	}

	private void mockAdminClient() {
		ClientRepresentation clientRepresentation = new ClientRepresentation();
		clientRepresentation.setId(REALM_MANAGEMENT_CLIENT_ID);
		when(clientsResource.findByClientId(REALM_MANAGEMENT_CLIENT_ID))
				.thenReturn(Collections.singletonList(clientRepresentation));

		RoleResource roleResource = mock(RoleResource.class);
		when(roleResource.toRepresentation()).thenReturn(new RoleRepresentation());

		RolesResource rolesResource = mock(RolesResource.class);
		when(rolesResource.get(REALM_ADMIN_ROLE)).thenReturn(roleResource);

		ClientResource clientResource = mock(ClientResource.class);
		when(clientResource.roles()).thenReturn(rolesResource);
		when(clientsResource.get(REALM_MANAGEMENT_CLIENT_ID)).thenReturn(clientResource);

		RoleMappingResource groupRoles = mock(RoleMappingResource.class);
		when(groupRoles.clientLevel(REALM_MANAGEMENT_CLIENT_ID)).thenReturn(roleScopeResource);

		GroupResource groupResource = mock(GroupResource.class);
		when(groupResource.roles()).thenReturn(groupRoles);

		when(groupsResource.group("admin")).thenReturn(groupResource);
	}

}
