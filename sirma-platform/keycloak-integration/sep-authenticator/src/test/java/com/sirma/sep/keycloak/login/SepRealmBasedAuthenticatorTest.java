package com.sirma.sep.keycloak.login;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.spi.HttpRequest;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link SepRealmBasedAuthenticator}.
 *
 * @author smustafov
 */
public class SepRealmBasedAuthenticatorTest {

	private static final URI BASE_URI = URI.create("http://localhost:8090/auth/");

	@InjectMocks
	private SepRealmBasedAuthenticator authenticator;

	@Mock
	private AuthenticationFlowContext context;
	@Mock
	private LoginFormsProvider loginFormsProvider;
	@Mock
	private RealmModel realmModel;
	@Mock
	private AuthenticationSessionModel sessionModel;
	@Mock
	private KeycloakSession keycloakSession;
	@Mock
	private RealmProvider realmProvider;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_NotRequireUser() {
		assertFalse(authenticator.requiresUser());
	}

	@Test
	public void should_NotBeConfigured() {
		assertFalse(authenticator.configuredFor(null, null, null));
	}

	@Test
	public void authenticate_Should_ShowRealmForm_When_UserInitiatesFirstLogin() {
		mockAuthFlowContext(null, null);

		authenticator.authenticate(context);

		verify(context).challenge(any(Response.class));
	}

	@Test
	public void authenticate_Should_MarkExecutionWithSuccess_When_UserAlreadyEnteredUsername() {
		mockAuthFlowContext("john", null);

		authenticator.authenticate(context);

		verify(context).success();
	}

	@Test
	public void action_Should_AbortAndShowErrorMessage_When_UsernameEmpty() {
		mockAuthFlowContext(null, Messages.MISSING_USERNAME);
		EventBuilder eventBuilder = mockEventBuilder();

		authenticator.action(context);

		verify(eventBuilder).error(Errors.USERNAME_MISSING);
		verify(loginFormsProvider).createForm(SepRealmBasedAuthenticator.FORM_TEMPLATE);
		verify(context).failureChallenge(any(AuthenticationFlowError.class), any(Response.class));
	}

	@Test
	public void action_Should_AbortAndShowErrorMessage_When_InvalidTenantPassed() {
		mockAuthFlowContext("regularuser@notExisting", SepRealmBasedAuthenticator.INVALID_REALM);
		mockRealm("master");
		EventBuilder eventBuilder = mockEventBuilder();

		authenticator.action(context);

		verify(eventBuilder).error(Errors.INVALID_USER_CREDENTIALS);
		verify(loginFormsProvider).createForm(SepRealmBasedAuthenticator.FORM_TEMPLATE);
		verify(context).failureChallenge(any(AuthenticationFlowError.class), any(Response.class));
	}

	@Test
	public void action_Should_RedirectToCorrectRealm_When_UsernameNotEmpty() {
		mockAuthFlowContext("regularuser@sep.test", null);
		mockRealm("master");
		mockClient("sep-ui");
		mockAuthSessionModel("http://localhost:5000");

		authenticator.action(context);

		verify(context).forceChallenge(any(Response.class));
	}

	@Test
	public void action_Should_RedirectToMasterRealm_When_NoTenantInUsername() {
		mockAuthFlowContext("regularuser", null);
		mockRealm("master");
		mockClient("sep-ui");
		mockAuthSessionModel("http://localhost:5000");

		authenticator.action(context);

		verify(context).forceChallenge(any(Response.class));
	}

	private void mockAuthFlowContext(String username, String errorMessage) {
		mockLoginForms(errorMessage);
		mockHttpRequest(username);

		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getBaseUri()).thenReturn(BASE_URI);
		when(context.getUriInfo()).thenReturn(uriInfo);

		when(sessionModel.getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM)).thenReturn(username);
		when(context.getAuthenticationSession()).thenReturn(sessionModel);
	}

	private void mockHttpRequest(String username) {
		HttpRequest httpRequest = mock(HttpRequest.class);
		MultivaluedMap<String, String> params = new MultivaluedMapImpl<>();
		params.add(AuthenticatorUtil.FULL_USERNAME, username);
		when(context.getHttpRequest()).thenReturn(httpRequest);
		when(httpRequest.getDecodedFormParameters()).thenReturn(params);
	}

	private void mockLoginForms(String errorMessage) {
		when(loginFormsProvider.createForm(SepRealmBasedAuthenticator.FORM_TEMPLATE))
				.thenReturn(Response.accepted().build());
		when(loginFormsProvider.setError(errorMessage)).thenReturn(loginFormsProvider);
		when(context.form()).thenReturn(loginFormsProvider);
	}

	private EventBuilder mockEventBuilder() {
		EventBuilder eventBuilder = mock(EventBuilder.class);
		when(context.getEvent()).thenReturn(eventBuilder);
		return eventBuilder;
	}

	private void mockAuthSessionModel(String redirectUri) {
		when(sessionModel.getRedirectUri()).thenReturn(redirectUri);
		when(sessionModel.getClientNote(OIDCLoginProtocol.RESPONSE_MODE_PARAM)).thenReturn("fragment");
		when(sessionModel.getClientNote(OIDCLoginProtocol.RESPONSE_TYPE_PARAM)).thenReturn("code");
		when(sessionModel.getClientNote(OIDCLoginProtocol.SCOPE_PARAM)).thenReturn("openid");
	}

	private void mockRealm(String name) {
		when(realmModel.getName()).thenReturn(name);
		when(context.getRealm()).thenReturn(realmModel);
		when(context.getSession()).thenReturn(keycloakSession);
		when(keycloakSession.realms()).thenReturn(realmProvider);
		when(realmProvider.getRealm("sep.test")).thenReturn(realmModel);
	}

	private void mockClient(String clientId) {
		ClientModel clientModel = mock(ClientModel.class);
		when(clientModel.getClientId()).thenReturn(clientId);
		when(sessionModel.getClient()).thenReturn(clientModel);
	}

}
