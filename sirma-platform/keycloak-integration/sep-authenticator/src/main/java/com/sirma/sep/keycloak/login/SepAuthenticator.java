package com.sirma.sep.keycloak.login;

import static com.sirma.sep.keycloak.login.AuthenticatorUtil.FULL_USERNAME;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * Custom keycloak authenticator which can be used in a authentication flow of a tenant. Extends the base authenticator
 * for username and password form {@link UsernamePasswordForm}.
 * <p>
 * This authenticator handles tenant(realm) login page switching. On the login page there should be single input field
 * where the user can enter full username in format: username@tenantId. When the user populates that field and clicks the
 * button to continue, this authenticator detects that and initiates redirect to the tenant login page.
 * Otherwise the flow is propagated to be handled by the base authenticator for username and password.
 * <p>
 * This authenticator appends the tenant for which redirect was performed as query param to the client redirect.
 * This way apps which use keycloak for authentication can figure out the authenticated tenant.
 *
 * @author smustafov
 */
public class SepAuthenticator extends UsernamePasswordForm {

	static final String MASTER_REALM_NAME = "master";
	static final String ADMIN_CONSOLE_CLIENT_ID = "security-admin-console";

	@Override
	public void action(AuthenticationFlowContext context) {
		String fullUsername = getFullUsername(context);
		if (fullUsername != null && !fullUsername.isEmpty()) {
			AuthenticatorUtil.performRedirect(context, fullUsername);
			return;
		}

		super.action(context);
	}

	private String getFullUsername(AuthenticationFlowContext context) {
		return context.getHttpRequest().getDecodedFormParameters().getFirst(FULL_USERNAME);
	}

	@Override
	public boolean requiresUser() {
		return false;
	}

	@Override
	public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
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
