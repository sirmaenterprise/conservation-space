package com.sirma.itt.emf.authentication.sso.saml.authenticator;

import java.util.Map;

import javax.crypto.SecretKey;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.sirma.itt.emf.authentication.sso.saml.SAMLMessageProcessor;
import com.sirma.itt.emf.security.event.UserAuthenticatedEvent;
import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.authentication.Authenticator;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.AuthenticationException;
import com.sirma.itt.seip.security.util.SecurityUtil;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Base Saml authenticator implementation that provides a common method for SAML authentication with token
 *
 * @author BBonev
 */
public abstract class BaseSamlAuthenticator implements Authenticator {

	@Inject
	private SAMLMessageProcessor messageProcessor;
	@Inject
	private SecurityContextManager securityContextManager;
	@Inject
	private Event<UserAuthenticatedEvent> authenticatedEvent;

	@Inject
	private SecurityConfiguration securityConfiguration;

	@Inject
	private UserStore userStore;

	/**
	 * Authenticates user with a given IDP token.
	 *
	 * @param rawToken
	 *            is the raw token from SAML server
	 * @return the authenticated authority or throws exception on authentication failure
	 */
	protected User authenticateWithToken(byte[] rawToken) {
		SAMLTokenInfo token = prepareToken(rawToken, securityConfiguration.getCryptoKey().get());
		// get the processed token so we can extract useriD
		Map<String, String> processedToken = messageProcessor.processSAMLResponse(token.getDecrypted());

		// application that the user has successfully authenticated
		String fullUserId = SAMLMessageProcessor.getSubject(processedToken);
		StringPair userAndTenant = SecurityUtil.getUserAndTenant(fullUserId);
		String tenantId = userAndTenant.getSecond();

		User user;
		try {
			// authenticate as admin to fetch the user data
			user = securityContextManager.executeAsTenant(tenantId).biFunction(userStore::loadByIdentityId, fullUserId,
					tenantId);

		} catch (Exception e) {
			// for some reason the user does not exists
			throw new AuthenticationException(fullUserId, SAMLMessageProcessor.getSessionIndex(processedToken),
					"User '" + fullUserId + "' from tenant '" + tenantId + "' not found in the system", e);
		}
		if (user == null) {
			// for some reason the user does not exists
			throw new AuthenticationException(fullUserId, SAMLMessageProcessor.getSessionIndex(processedToken),
					"User '" + fullUserId + "' from tenant '" + tenantId + "' not found in the system");
		}

		completeAuthentication(user, token, processedToken);
		return user;
	}

	/**
	 * Authenticates user with a given IDP token.
	 *
	 * @param user
	 *            the user to get information from
	 * @param rawToken
	 *            is the raw token from SAML server
	 * @return the authentication token for authority or throws exception on authentication failure
	 */
	protected String authenticateWithTokenAndGetTicket(User user, byte[] rawToken) {

		SAMLTokenInfo token = prepareToken(rawToken, securityConfiguration.getCryptoKey().get());

		// get the processed token so we can extract useriD
		Map<String, String> processedToken = messageProcessor.processSAMLResponse(token.getDecrypted());

		// application that the user has successfully authenticated
		String fullUserId = SAMLMessageProcessor.getSubject(processedToken);
		StringPair userAndTenant = SecurityUtil.getUserAndTenant(fullUserId);
		String tenantId = userAndTenant.getSecond();

		if (!EqualsHelper.nullSafeEquals(user.getTenantId(), tenantId) && !SecurityContext.isDefaultTenant(tenantId)
				&& !SecurityContext.isSystemTenant(tenantId)) {
			throw new AuthenticationException(fullUserId, SAMLMessageProcessor.getSessionIndex(processedToken),
					"The requested authentication ("
							+ SecurityUtil.buildTenantUserId(user.getIdentityId(), user.getTenantId())
							+ ") and resolved (" + fullUserId + ") authentications does not match!");
		}

		completeAuthentication(user, token, processedToken);
		return token.getToken();
	}

	/**
	 * Gets the pair of encrypted and decrypted version of the given token. The decrypted result is always in base64
	 *
	 * @param token
	 *            the token
	 * @param secretKey
	 *            the crypto key to use
	 * @return the encrypt decrypted pair as wrapper class
	 */
	protected SAMLTokenInfo prepareToken(byte[] token, SecretKey secretKey) {
		SAMLTokenInfo result = null;
		byte[] decodedBase64 = token;
		byte[] encodedBase64 = decodedBase64;
		boolean isEncoded = false;
		boolean needsEncoding = true;
		do {
			isEncoded = SAMLMessageProcessor.isEncodedSAMLMessage(decodedBase64);
			if (isEncoded) {
				needsEncoding = false;
				// save last encoded state for optimization
				encodedBase64 = decodedBase64;
				decodedBase64 = SAMLMessageProcessor.decodeSAMLMessage(decodedBase64);
			}
		} while (isEncoded);
		// check whether we have cached encoded version
		encodedBase64 = needsEncoding ? SAMLMessageProcessor.encodeSAMLMessage(encodedBase64) : encodedBase64;
		try {
			result = new SAMLTokenInfo(encodedBase64,
					SAMLMessageProcessor.encodeSAMLMessage(SecurityUtil.decrypt(decodedBase64, secretKey)));
		} catch (Exception e) {// NOSONAR
			// looks like it is already decrypted message
			result = new SAMLTokenInfo(
					SAMLMessageProcessor.encodeSAMLMessage(SecurityUtil.encrypt(decodedBase64, secretKey)),
					encodedBase64);

		}
		return result;
	}

	/**
	 * Update the token of user and its properties with the login information
	 *
	 * @param authenticated
	 *            is the user
	 * @param info
	 *            is the token info bean
	 * @param processedToken
	 *            is the saml idp response
	 */
	protected void completeAuthentication(User authenticated, SAMLTokenInfo info, Map<String, String> processedToken) {
		User authenticatedWithToken = userStore.setUserTicket(authenticated, info.getToken());
		authenticatedWithToken.getProperties().putAll(processedToken);
		fireAuhenticationEvent(authenticatedWithToken);
	}

	/**
	 * Fire the authentiaction event to start a session login and to trail as audit entry
	 *
	 * @param authenticated
	 */
	protected void fireAuhenticationEvent(User authenticated) {
		StringPair userAndTenant = SecurityUtil.getUserAndTenant(authenticated.getIdentityId());
		securityContextManager.executeAsTenant(userAndTenant.getSecond()).consumer(authenticatedEvent::fire,
				new UserAuthenticatedEvent(authenticated, false));
	}
}