package com.sirma.sep.keycloak.login;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.common.util.UriUtils;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;

import com.sirma.sep.keycloak.util.SecurityUtil;

/**
 * Utility for working with authenticators.
 *
 * @author smustafov
 */
public class AuthenticatorUtil {

	private static final Logger LOGGER = Logger.getLogger(AuthenticatorUtil.class);

	static final String FULL_USERNAME = "fullUsername";
	static final String MASTER_REALM_NAME = "master";
	static final String ADMIN_CONSOLE_CLIENT_ID = "security-admin-console";

	/**
	 * Performs redirect from one tenant(realm) to another tenant's login page. The tenant is determined by the tenant
	 * domain in the given username. When the username have no tenant the redirect is initiated to the master realm.
	 *
	 * @param context the current auth flow context
	 * @param fullUsername the username in format: username@tenantId
	 */
	public static void performRedirect(AuthenticationFlowContext context, String fullUsername) {
		String[] usernameAndTenant = SecurityUtil.getUserAndTenant(fullUsername.trim());
		String username = usernameAndTenant[0];
		String tenant = usernameAndTenant[1];

		LOGGER.infof("Extracted username: %s and tenant: %s", username, tenant);

		if (tenant == null) {
			// user entered no tenant, going to redirect to the master realm
			redirectToRealmLoginPage(context, username, MASTER_REALM_NAME);
			return;
		}

		redirectToRealmLoginPage(context, username, tenant);
	}

	private static void redirectToRealmLoginPage(AuthenticationFlowContext context, String username, String realm) {
		UriBuilder uriBuilder = UriBuilder.fromUri(URI.create(getBaseLoginUri(context, realm)));
		uriBuilder.queryParam(OIDCLoginProtocol.CLIENT_ID_PARAM, getClientId(context));
		uriBuilder.queryParam(OIDCLoginProtocol.REDIRECT_URI_PARAM, getRedirectUri(context, realm));
		uriBuilder.queryParam(OIDCLoginProtocol.RESPONSE_MODE_PARAM, getResponseMode(context));
		uriBuilder.queryParam(OIDCLoginProtocol.RESPONSE_TYPE_PARAM, getResponseType(context));
		uriBuilder.queryParam(OIDCLoginProtocol.SCOPE_PARAM, getScope(context));
		uriBuilder.queryParam(OIDCLoginProtocol.LOGIN_HINT_PARAM, username);

		URI redirectUrl = uriBuilder.build();
		LOGGER.infof("Redirecting login to: %s", redirectUrl);

		Response response = Response.seeOther(redirectUrl).build();
		context.forceChallenge(response);
	}

	private static String getBaseLoginUri(AuthenticationFlowContext context, String realm) {
		return context.getUriInfo().getBaseUri().toString() + "realms/" + realm + "/protocol/openid-connect/auth";
	}

	private static String getClientId(AuthenticationFlowContext context) {
		return context.getAuthenticationSession().getClient().getClientId();
	}

	private static String getRedirectUri(AuthenticationFlowContext context, String realm) {
		String redirect = context.getAuthenticationSession().getRedirectUri();
		if (ADMIN_CONSOLE_CLIENT_ID.equals(getClientId(context))) {
			return redirect.replace(context.getRealm().getName(), realm);
		}

		if (redirect.contains("tenant=")) {
			redirect = UriUtils.stripQueryParam(redirect, "tenant");
		}

		if (redirect.contains("?")) {
			redirect += "&";
		} else {
			redirect += "?";
		}

		// append the tenant in which will be logged in as query param, so client apps can figure out the tenant
		return redirect + "tenant=" + realm;
	}

	private static String getResponseMode(AuthenticationFlowContext context) {
		return context.getAuthenticationSession().getClientNote(OIDCLoginProtocol.RESPONSE_MODE_PARAM);
	}

	private static String getResponseType(AuthenticationFlowContext context) {
		return context.getAuthenticationSession().getClientNote(OIDCLoginProtocol.RESPONSE_TYPE_PARAM);
	}

	private static String getScope(AuthenticationFlowContext context) {
		return context.getAuthenticationSession().getClientNote(OIDCLoginProtocol.SCOPE_PARAM);
	}

	private AuthenticatorUtil() {
		// utility
	}
}
