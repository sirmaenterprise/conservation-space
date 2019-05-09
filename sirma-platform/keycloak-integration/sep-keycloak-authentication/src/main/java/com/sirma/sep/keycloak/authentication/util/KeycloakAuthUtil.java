package com.sirma.sep.keycloak.authentication.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.util.JsonSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.security.exception.AuthenticationException;
import com.sirma.itt.seip.security.util.SecurityUtil;
import com.sirma.sep.keycloak.ClientProperties;

/**
 * Contains utility methods for authentication.
 *
 * @author smustafov
 */
public class KeycloakAuthUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private KeycloakAuthUtil() {
		// utility
	}

	/**
	 * Initiates login to Keycloak with username and password. If user credentials are valid {@link AccessTokenResponse}
	 * is returned which contains the access, id and refresh tokens. If it fails to login the user, it will return null.
	 * <p>
	 * Note that this creates new session in Keycloak for the user.
	 *
	 * @param deployment the keycloak tenant deployment
	 * @param username   of the user to authenticate
	 * @param password   of the user to authenticate
	 * @return {@link AccessTokenResponse} if login successful, if fails to login null will be returned
	 */
	public static AccessTokenResponse loginWithCredentials(KeycloakDeployment deployment, String username,
			String password) {
		try {
			HttpPost post = buildBasicAuthRequest(deployment, username, password);
			HttpResponse response = deployment.getClient().execute(post);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				LOGGER.error("Failed to authenticate user: {}. IDP response: {} {}. Check your credentials", username,
						response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
				return null;
			}

			HttpEntity entity = response.getEntity();
			if (entity == null) {
				throw new AuthenticationException(username, "No login response");
			}

			AccessTokenResponse accessTokenResponse;

			try (InputStream inputStream = entity.getContent()) {
				accessTokenResponse = JsonSerialization.readValue(inputStream, AccessTokenResponse.class);
			}

			return accessTokenResponse;
		} catch (IOException e) {
			throw new AuthenticationException(username, "Failed to authenticate user", e);
		}
	}

	private static HttpPost buildBasicAuthRequest(KeycloakDeployment deployment, String username, String password) {
		HttpPost post = new HttpPost(buildAuthUri(deployment));

		List<NameValuePair> formParams = new ArrayList<>();
		formParams.add(new BasicNameValuePair(OAuth2Constants.GRANT_TYPE, OAuth2Constants.PASSWORD));
		formParams.add(new BasicNameValuePair("username", SecurityUtil.getUserWithoutTenant(username)));
		formParams.add(new BasicNameValuePair("password", password));
		formParams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_ID, ClientProperties.SEP_UI_CLIENT_ID));

		UrlEncodedFormEntity form = new UrlEncodedFormEntity(formParams, StandardCharsets.UTF_8);
		post.setEntity(form);

		return post;
	}

	/**
	 * Logouts (destroys session associated with the refresh token) user by refresh token.
	 * If the logout fails for some reason {@link AuthenticationException} will be thrown.
	 * <p>
	 * In order to use this method first {@link AccessTokenResponse} must be retrieved via
	 * {@link KeycloakAuthUtil#loginWithCredentials(KeycloakDeployment, String, String)} method.
	 *
	 * @param deployment    the keycloak tenant deployment
	 * @param tokenResponse {@link AccessTokenResponse} retrieved from login
	 * @param username      of the user to logout
	 */
	public static void logout(KeycloakDeployment deployment, AccessTokenResponse tokenResponse, String username) {
		try {
			HttpPost post = buildLogoutRequest(deployment, tokenResponse);
			HttpResponse response = deployment.getClient().execute(post);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
				LOGGER.error("Failed to logout user: {}. IDP response: {} {}", username,
						response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
				throw new AuthenticationException(username, "Failed to logout user");
			}
		} catch (IOException e) {
			throw new AuthenticationException("Failed to read logout response");
		}
	}

	private static HttpPost buildLogoutRequest(KeycloakDeployment deployment, AccessTokenResponse tokenResponse) {
		HttpPost post = new HttpPost(buildLogoutUri(deployment));
		List<NameValuePair> formParams = new ArrayList<>();
		formParams.add(new BasicNameValuePair(OAuth2Constants.REFRESH_TOKEN, tokenResponse.getRefreshToken()));
		formParams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_ID, ClientProperties.SEP_UI_CLIENT_ID));

		UrlEncodedFormEntity form = new UrlEncodedFormEntity(formParams, StandardCharsets.UTF_8);
		post.setEntity(form);

		return post;
	}

	private static URI buildAuthUri(KeycloakDeployment deployment) {
		return buildUri(deployment, ServiceUrlConstants.TOKEN_PATH);
	}

	private static URI buildLogoutUri(KeycloakDeployment deployment) {
		return buildUri(deployment, ServiceUrlConstants.TOKEN_SERVICE_LOGOUT_PATH);
	}

	private static URI buildUri(KeycloakDeployment deployment, String baseServiceUrl) {
		String authServerBaseUrl = deployment.getAuthServerBaseUrl();
		String url = baseServiceUrl.replace("{realm-name}", deployment.getRealm());
		return URI.create(authServerBaseUrl + url);
	}

}
