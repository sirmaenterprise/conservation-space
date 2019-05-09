package com.sirma.sep.keycloak.credentials;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.resources.security.PasswordChangeFailException;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.sep.keycloak.producers.KeycloakClientProducer;
import com.sirma.sep.keycloak.tenant.KeycloakDeploymentRetriever;

/**
 * Tests for {@link KeycloakCredentialsService}.
 *
 * @author smustafov
 */
public class KeycloakCredentialsServiceTest {

	@InjectMocks
	private KeycloakCredentialsService credentialsService;

	@Mock
	private KeycloakClientProducer clientProducer;

	@Mock
	private KeycloakDeploymentRetriever deploymentRetriever;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_ChangePassword_When_CredentialsCorrect() throws IOException {
		mockDeployment(HttpStatus.SC_OK, HttpStatus.SC_NO_CONTENT);
		UserResource userResource = withUserInIdp("regularuser", "regular-user-id");

		credentialsService.changeUserPassword("regularuser@sep.test", "123456", "qwerty");

		verify(userResource).resetPassword(buildCredential("qwerty"));
	}

	@Test
	public void should_ContainCorrectFailType_When_IdpValidationFails() throws IOException {
		mockDeployment(HttpStatus.SC_OK, HttpStatus.SC_NO_CONTENT);
		mockPasswordPolicy("length(8)");
		mockResponse(KeycloakCredentialsService.MIN_LENGTH_MESSAGE);

		verifyFailure(PasswordChangeFailException.PasswordFailType.MIN_LENGTH, "8");
	}

	@Test
	public void should_ContainCorrectPolicyValue_When_IdpValidationFails() throws IOException {
		mockDeployment(HttpStatus.SC_OK, HttpStatus.SC_NO_CONTENT);
		mockPasswordPolicy(
				"length(8) and upperCase(1) and lowerCase(1) and digits(3) and specialChars(1) and notUsername(undefined)");
		mockResponse(KeycloakCredentialsService.MIN_DIGITS_MESSAGE);

		verifyFailure(PasswordChangeFailException.PasswordFailType.MIN_DIGITS, "3");
	}

	@Test(expected = PasswordChangeFailException.class)
	public void should_ThrowException_When_CurrentPasswordWrong() throws IOException {
		mockDeployment(HttpStatus.SC_UNAUTHORIZED, HttpStatus.SC_UNAUTHORIZED);
		withUserInIdp("regularuser", "regular-user-id");

		credentialsService.changeUserPassword("regularuser@sep.test", "123456", "qwerty");
	}

	@Test(expected = PasswordChangeFailException.class)
	public void should_ThrowException_When_CannotFetchUser() throws IOException {
		mockDeployment(HttpStatus.SC_OK, HttpStatus.SC_NO_CONTENT);
		UserResource userResource = withUserInIdp("regularuser", "regular-user-id");
		doThrow(NotFoundException.class).when(userResource).resetPassword(any());

		credentialsService.changeUserPassword("regularuser@sep.test", "123456", "qwerty");
	}

	@Test
	public void should_HaveName() {
		assertEquals(SecurityConfiguration.KEYCLOAK_IDP, credentialsService.getName());
	}

	private void verifyFailure(PasswordChangeFailException.PasswordFailType passwordFailType, String policyValue) {
		try {
			credentialsService.changeUserPassword("regularuser@sep.test", "123456", "qwerty");
		} catch (PasswordChangeFailException e) {
			assertEquals(passwordFailType, e.getType());
			assertEquals(policyValue, e.getPolicyValue());
		}
	}

	private void mockDeployment(int loginStatus, int logoutStatus) throws IOException {
		KeycloakDeployment deployment = mock(KeycloakDeployment.class);
		when(deployment.getRealm()).thenReturn("sep.test");

		HttpClient httpClient = mock(HttpClient.class);
		HttpResponse httpResponse = mock(HttpResponse.class);
		StatusLine statusLine = mock(StatusLine.class);
		when(statusLine.getStatusCode()).thenReturn(loginStatus, logoutStatus);
		when(httpResponse.getStatusLine()).thenReturn(statusLine);
		when(httpClient.execute(any())).thenReturn(httpResponse);
		when(deployment.getClient()).thenReturn(httpClient);

		HttpEntity entity = mock(HttpEntity.class);

		String json = "{\"access_token\": \"accToken\", \"refresh_token\": \"refrToken\"}";
		when(entity.getContent()).thenReturn(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));

		when(httpResponse.getEntity()).thenReturn(entity);

		when(deploymentRetriever.getDeployment("sep.test")).thenReturn(deployment);
	}

	private void mockPasswordPolicy(String policy) {
		RealmResource realmResource = mock(RealmResource.class);
		RealmRepresentation realmRepresentation = new RealmRepresentation();
		realmRepresentation.setPasswordPolicy(policy);
		when(realmResource.toRepresentation()).thenReturn(realmRepresentation);
		when(clientProducer.produceRealmResource()).thenReturn(realmResource);
	}

	private void mockResponse(String error) {
		UserResource userResource = withUserInIdp("regularuser", "regular-user-id");
		Response response = mock(Response.class);
		when(response.getStatusInfo()).thenReturn(Response.Status.BAD_REQUEST);
		when(response.getStatus()).thenReturn(Response.Status.BAD_REQUEST.getStatusCode());
		when(response.readEntity(String.class))
				.thenReturn("{\"error\":\"" + error + "\",\"error_description\":\"invalid password\"}");
		doThrow(new BadRequestException(response)).when(userResource)
				.resetPassword(any(CredentialRepresentation.class));
	}

	private UserResource withUserInIdp(String username, String id) {
		UsersResource usersResource = mock(UsersResource.class);
		UserRepresentation userRepresentation = new UserRepresentation();
		userRepresentation.setUsername(username);
		userRepresentation.setId(id);
		when(usersResource.search(username)).thenReturn(Collections.singletonList(userRepresentation));
		when(clientProducer.produceUsersResource()).thenReturn(usersResource);

		UserResource userResource = mock(UserResource.class);
		when(usersResource.get(id)).thenReturn(userResource);

		return userResource;
	}

	private static CredentialRepresentation buildCredential(String password) {
		CredentialRepresentation credential = new CredentialRepresentation();
		credential.setValue(password);
		credential.setType(CredentialRepresentation.PASSWORD);
		credential.setTemporary(false);
		return credential;
	}

}
