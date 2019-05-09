package com.sirma.sep.keycloak.login;

import static com.sirma.sep.keycloak.login.AuthenticatorUtil.FULL_USERNAME;

import javax.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.events.Errors;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.messages.Messages;

import com.sirma.sep.keycloak.util.SecurityUtil;

/**
 * Custom keycloak authenticator which can be used in a authentication flow of a tenant.
 * <p>
 * This authenticator renders single input field for entering username in format: username@tenantId
 * and a button for proceeding to the login page of the tenant extracted from the field.
 * <p>
 * After successful login this authenticator appends the tenant in which the login was performed as query param
 * in the authentication response so the apps which use keycloak for authentication can figure out the authenticated
 * tenant.
 *
 * @author smustafov
 */
public class SepRealmBasedAuthenticator implements Authenticator {

	static final String FORM_TEMPLATE = "realm-based-authenticator.ftl";
	static final String INVALID_REALM = "invalidRealm";

	/**
	 * Invoked when first authentication request is initialized.
	 *
	 * @param context the authentication flow context which contains current authentication request data
	 */
	@Override
	public void authenticate(AuthenticationFlowContext context) {
		if (context.getAuthenticationSession().getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM) != null) {
			// if we have username to fulfill then we already executed this authenticator so continue to
			// the next one in the flow
			context.success();
			return;
		}

		Response response = context.form().createForm(FORM_TEMPLATE);
		context.challenge(response);
	}

	/**
	 * Invoked after user clicks the continue button. Extracts tenant from the field and redirects to the login
	 * page of that tenant. The username is passed as {@link OIDCLoginProtocol#LOGIN_HINT_PARAM} so after the
	 * redirect user will not need to enter it again.
	 *
	 * @param context the authentication flow context which contains current authentication request data
	 */
	@Override
	public void action(AuthenticationFlowContext context) {
		String fullUsername = getFullUsername(context);
		if (!validateUsername(context, fullUsername) || !validateTenant(context, fullUsername)) {
			return;
		}

		AuthenticatorUtil.performRedirect(context, fullUsername);
	}

	private String getFullUsername(AuthenticationFlowContext context) {
		return context.getHttpRequest().getDecodedFormParameters().getFirst(FULL_USERNAME);
	}

	private boolean validateUsername(AuthenticationFlowContext context, String fullUsername) {
		if (isBlank(fullUsername)) {
			context.getEvent().error(Errors.USERNAME_MISSING);
			Response challengeResponse = buildInvalidUserResponse(context, Messages.MISSING_USERNAME);
			context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
			return false;
		}
		return true;
	}

	private static boolean isBlank(String fullUsername) {
		return fullUsername == null || fullUsername.trim().isEmpty();
	}

	private Response buildInvalidUserResponse(AuthenticationFlowContext context, String errorMessage) {
		return context.form().setError(errorMessage).createForm(FORM_TEMPLATE);
	}

	private boolean validateTenant(AuthenticationFlowContext context, String fullUsername) {
		String tenant = SecurityUtil.getUserAndTenant(fullUsername.trim())[1];
		if (tenant == null) {
			return true;
		}

		RealmModel realm = context.getSession().realms().getRealm(tenant);
		if (realm == null) {
			context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
			Response response = buildInvalidUserResponse(context, INVALID_REALM);
			context.failureChallenge(AuthenticationFlowError.INVALID_USER, response);
			return false;
		}
		return true;
	}

	@Override
	public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
		return false;
	}

	@Override
	public boolean requiresUser() {
		return false;
	}

	@Override
	public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
		// not needed
	}

	@Override
	public void close() {
		// not needed
	}

}
