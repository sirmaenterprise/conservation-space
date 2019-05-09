package com.sirma.sep.keycloak.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.spi.HttpRequest;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link SepAuthenticator}.
 *
 * @author smustafov
 */
public class SepAuthenticatorTest {

	private static final URI BASE_URI = URI.create("http://localhost:8090/auth/");

	@InjectMocks
	private SepAuthenticator authenticator;

	@Mock
	private AuthenticationFlowContext context;
	@Mock
	private RealmModel realmModel;
	@Mock
	private AuthenticationSessionModel sessionModel;

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
	public void action_Should_RedirectToCorrectRealm_When_CurrentRealmIsNotTheSame() {
		mockAuthFlowContext("regularuser@sep.test");
		mockRealm("master");
		mockClient("sep-ui");
		mockAuthSessionModel("http://localhost:5000");

		authenticator.action(context);

		verifyRedirect("sep.test", "http://localhost:5000?tenant=sep.test", "regularuser");
	}

	@Test
	public void action_Should_RedirectToMasterRealm_When_NoTenantInUsername() {
		mockAuthFlowContext("systemadmin");
		mockRealm("sirma.bg");
		mockClient(SepAuthenticator.ADMIN_CONSOLE_CLIENT_ID);
		mockAuthSessionModel("http://localhost:8090/auth/admin/master/console/");

		authenticator.action(context);

		verifyRedirect(SepAuthenticator.MASTER_REALM_NAME, "http://localhost:8090/auth/admin/master/console/",
				"systemadmin");
	}

	@Test
	public void action_Should_AppendRealmNameAsQueryParamInRedirectUri() {
		mockAuthFlowContext("regularuser@sep.test");
		mockRealm("master");
		mockClient("sep-ui");
		mockAuthSessionModel("http://localhost:5000/#/idoc/emf:instanceId");

		authenticator.action(context);

		verifyRedirect("sep.test", "http://localhost:5000/#/idoc/emf:instanceId?tenant=sep.test", "regularuser");
	}

	@Test
	public void action_Should_CorrectlyAppendRealmNameAsQueryParamInRedirectUri_When_RedirectUriHasAnchorTags() {
		mockAuthFlowContext("regularuser@sep.test");
		mockRealm("master");
		mockClient("sep-ui");
		mockAuthSessionModel("http://localhost:5000/#/idoc/emf:instanceId#permissions-tab");

		authenticator.action(context);

		verifyRedirect("sep.test", "http://localhost:5000/#/idoc/emf:instanceId#permissions-tab?tenant=sep.test",
				"regularuser");
	}

	@Test
	public void action_Should_CorrectlyAppendRealmNameAsQueryParamInRedirectUri_When_RedirectUriHasQueryParams() {
		mockAuthFlowContext("regularuser@sep.test");
		mockRealm("master");
		mockClient("sep-ui");
		mockAuthSessionModel("http://localhost:5000/#/administration?tool=user-management");

		authenticator.action(context);

		verifyRedirect("sep.test", "http://localhost:5000/#/administration?tool=user-management&tenant=sep.test",
				"regularuser");
	}

	@Test
	public void action_Should_HaveOnlyOneTenantQueryParam_When_MakingMultipleTenantLoginPageSwitching() {
		mockAuthFlowContext("regularuser@sep.test");
		mockRealm("master");
		mockClient("sep-ui");
		mockAuthSessionModel("http://localhost:5000?tenant=sep.release");

		authenticator.action(context);

		verifyRedirect("sep.test", "http://localhost:5000?tenant=sep.test", "regularuser");
	}

	private void verifyRedirect(String realm, String redirectUri, String username) {
		ArgumentCaptor<Response> argumentCaptor = ArgumentCaptor.forClass(Response.class);
		verify(context).forceChallenge(argumentCaptor.capture());

		Response response = argumentCaptor.getValue();
		assertEquals(buildExpectedRedirectUrl(realm, redirectUri, username), response.getLocation().toString());
	}

	private String buildExpectedRedirectUrl(String realm, String redirectUri, String username) {
		String expectedUrl = BASE_URI.toString() + "realms/" + realm + "/protocol/openid-connect/auth";
		UriBuilder uriBuilder = UriBuilder.fromUri(URI.create(expectedUrl));

		uriBuilder.queryParam(OIDCLoginProtocol.CLIENT_ID_PARAM, sessionModel.getClient().getClientId());
		uriBuilder.queryParam(OIDCLoginProtocol.REDIRECT_URI_PARAM, redirectUri);
		uriBuilder.queryParam(OIDCLoginProtocol.RESPONSE_MODE_PARAM,
				sessionModel.getClientNote(OIDCLoginProtocol.RESPONSE_MODE_PARAM));
		uriBuilder.queryParam(OIDCLoginProtocol.RESPONSE_TYPE_PARAM,
				sessionModel.getClientNote(OIDCLoginProtocol.RESPONSE_TYPE_PARAM));
		uriBuilder.queryParam(OIDCLoginProtocol.SCOPE_PARAM, sessionModel.getClientNote(OIDCLoginProtocol.SCOPE_PARAM));
		uriBuilder.queryParam(OIDCLoginProtocol.LOGIN_HINT_PARAM, username);

		return uriBuilder.build().toString();
	}

	private void mockAuthFlowContext(String username) {
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
		when(httpRequest.getDecodedFormParameters()).thenReturn(params);
		when(context.getHttpRequest()).thenReturn(httpRequest);
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
	}

	private void mockClient(String clientId) {
		ClientModel clientModel = mock(ClientModel.class);
		when(clientModel.getClientId()).thenReturn(clientId);
		when(sessionModel.getClient()).thenReturn(clientModel);
	}

}
