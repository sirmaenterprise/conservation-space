package com.sirma.sep.keycloak.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.ServerInfoResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.resources.synchronization.RemoteUserStoreAdapterProxy;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.keycloak.config.KeycloakConfiguration;

/**
 * Tests for {@link SystemTenantMigration}.
 *
 * @author smustafov
 */
public class SystemTenantMigrationTest {

	@InjectMocks
	private SystemTenantMigration migration;

	@Mock
	private KeycloakConfiguration keycloakConfiguration;

	@Mock
	private SecurityConfiguration securityConfiguration;

	@Mock
	private ConfigurationManagement configurationManagement;

	@Mock
	private RemoteUserStoreAdapterProxy userStoreAdapterProxy;

	@Mock
	private Keycloak keycloakClient;

	@Mock
	private ServerInfoResource serverInfoResource;

	@Mock
	private RealmResource realmResource;

	@Mock
	private UsersResource usersResource;

	@Mock
	private UserResource userResource;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);

		when(securityConfiguration.getSystemAdminUsername()).thenReturn("systemadmin");
		when(securityConfiguration.getAdminUserPassword()).thenReturn(new ConfigurationPropertyMock<>("test"));

		when(keycloakConfiguration.getKeycloakAddress()).thenReturn(new ConfigurationPropertyMock<>());
		when(keycloakConfiguration.getKeycloakSystemTenantId()).thenReturn(new ConfigurationPropertyMock<>());
		when(keycloakConfiguration.getKeycloakAdminClientId()).thenReturn(new ConfigurationPropertyMock<>());

		when(keycloakClient.serverInfo()).thenReturn(serverInfoResource);
		when(keycloakClient.realm(any())).thenReturn(realmResource);
		when(realmResource.users()).thenReturn(usersResource);
		migration.setClientProvider((address, tenantId, username, password, clientId) -> keycloakClient);
	}

	@Test
	public void should_ChangeIdpConfigs() {
		mockIdpConfigs(SecurityConfiguration.WSO_IDP);

		migration.migrate();

		ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);
		verify(configurationManagement).updateSystemConfigurations(argumentCaptor.capture());
		assertEquals(2, argumentCaptor.getValue().size());
	}

	@Test
	public void should_NotChangePassword_When_AuthIsSuccessful() {
		mockIdpConfigs(SecurityConfiguration.WSO_IDP);

		migration.migrate();

		verifyPasswordNotChanged();
	}

	@Test
	public void should_ChangePassword_When_AuthFailed() {
		mockIdpConfigs(SecurityConfiguration.WSO_IDP);
		mockRemoteUser();
		when(keycloakClient.serverInfo())
				.thenThrow(new NotAuthorizedException(Response.status(Response.Status.UNAUTHORIZED).build()));

		migration.migrate();

		verifyPasswordChanged();
	}

	@Test
	public void should_DoNothing_When_AlreadyMigrated() {
		mockIdpConfigs(SecurityConfiguration.KEYCLOAK_IDP);

		migration.migrate();

		verifyPasswordNotChanged();
		verify(configurationManagement, never()).updateSystemConfigurations(anyList());
	}

	private void verifyPasswordChanged() {
		ArgumentCaptor<CredentialRepresentation> argumentCaptor = ArgumentCaptor
				.forClass(CredentialRepresentation.class);
		verify(userResource).resetPassword(argumentCaptor.capture());

		CredentialRepresentation credentialRepresentation = argumentCaptor.getValue();
		assertEquals("test", credentialRepresentation.getValue());
		assertEquals(CredentialRepresentation.PASSWORD, credentialRepresentation.getType());
		assertFalse(credentialRepresentation.isTemporary());
	}

	private void verifyPasswordNotChanged() {
		verify(keycloakClient, never()).realm(anyString());
		verify(userResource, never()).resetPassword(any(CredentialRepresentation.class));
	}

	private void mockIdpConfigs(String idp) {
		when(securityConfiguration.getIdpProviderName()).thenReturn(new ConfigurationPropertyMock<>(idp));
		when(userStoreAdapterProxy.getSynchronizationProviderName()).thenReturn(new ConfigurationPropertyMock<>(idp));
	}

	private void mockRemoteUser() {
		UserRepresentation userRepresentation = new UserRepresentation();
		userRepresentation.setId("systemadmin");
		userRepresentation.setUsername("systemadmin");
		when(usersResource.search("systemadmin")).thenReturn(Collections.singletonList(userRepresentation));

		when(usersResource.get("systemadmin")).thenReturn(userResource);
	}

}
