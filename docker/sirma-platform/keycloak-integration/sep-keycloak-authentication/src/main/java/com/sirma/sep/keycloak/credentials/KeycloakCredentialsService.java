package com.sirma.sep.keycloak.credentials;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.security.PasswordChangeFailException;
import com.sirma.itt.seip.resources.security.UserCredentialService;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.util.SecurityUtil;
import com.sirma.sep.keycloak.authentication.util.KeycloakAuthUtil;
import com.sirma.sep.keycloak.producers.KeycloakClientProducer;
import com.sirma.sep.keycloak.tenant.KeycloakDeploymentRetriever;
import com.sirma.sep.keycloak.util.KeycloakApiUtil;

/**
 * Keycloak implementation of {@link UserCredentialService}.
 * The client of keycloak do not have api for changing password of user with old and new passwords, instead here we
 * initiate login with the old password to check and after password is changed we logout the user.
 *
 * @author smustafov
 */
@Extension(target = UserCredentialService.NAME, order = 2)
public class KeycloakCredentialsService implements UserCredentialService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Pattern POLICY_SEPARATOR_PATTERN = Pattern.compile(" and ");
	private static final Map<String, String> ERROR_MAPPING;
	private static final Map<String, PasswordChangeFailException.PasswordFailType> ERROR_KEY_TO_FAIL_TYPE_MAPPING;

	static final String BLACKLISTED_MESSAGE = "invalidPasswordBlacklistedMessage";
	static final String HISTORY_MESSAGE = "invalidPasswordHistoryMessage";
	static final String NOT_USERNAME_MESSAGE = "invalidPasswordNotUsernameMessage";
	static final String MIN_DIGITS_MESSAGE = "invalidPasswordMinDigitsMessage";
	static final String MIN_LENGTH_MESSAGE = "invalidPasswordMinLengthMessage";
	static final String MIN_LOWER_CASE_CHARS_MESSAGE = "invalidPasswordMinLowerCaseCharsMessage";
	static final String MIN_UPPER_CASE_CHARS_MESSAGE = "invalidPasswordMinUpperCaseCharsMessage";
	static final String MIN_SPECIAL_CHARS_MESSAGE = "invalidPasswordMinSpecialCharsMessage";
	static final String JSON_ERROR_KEY = "error";
	static final String JSON_ERROR_DESCRIPTION_KEY = "error_description";

	static {
		ERROR_MAPPING = new HashMap<>();
		ERROR_MAPPING.put("passwordBlacklist", BLACKLISTED_MESSAGE);
		ERROR_MAPPING.put("passwordHistory", HISTORY_MESSAGE);
		ERROR_MAPPING.put("notUsername", NOT_USERNAME_MESSAGE);
		ERROR_MAPPING.put("digits", MIN_DIGITS_MESSAGE);
		ERROR_MAPPING.put("length", MIN_LENGTH_MESSAGE);
		ERROR_MAPPING.put("lowerCase", MIN_LOWER_CASE_CHARS_MESSAGE);
		ERROR_MAPPING.put("upperCase", MIN_UPPER_CASE_CHARS_MESSAGE);
		ERROR_MAPPING.put("specialChars", MIN_SPECIAL_CHARS_MESSAGE);

		ERROR_KEY_TO_FAIL_TYPE_MAPPING = new HashMap<>();
		ERROR_KEY_TO_FAIL_TYPE_MAPPING
				.put(BLACKLISTED_MESSAGE, PasswordChangeFailException.PasswordFailType.BLACKLISTED);
		ERROR_KEY_TO_FAIL_TYPE_MAPPING.put(HISTORY_MESSAGE, PasswordChangeFailException.PasswordFailType.HISTORY);
		ERROR_KEY_TO_FAIL_TYPE_MAPPING
				.put(NOT_USERNAME_MESSAGE, PasswordChangeFailException.PasswordFailType.NOT_USERNAME);
		ERROR_KEY_TO_FAIL_TYPE_MAPPING.put(MIN_DIGITS_MESSAGE, PasswordChangeFailException.PasswordFailType.MIN_DIGITS);
		ERROR_KEY_TO_FAIL_TYPE_MAPPING.put(MIN_LENGTH_MESSAGE, PasswordChangeFailException.PasswordFailType.MIN_LENGTH);
		ERROR_KEY_TO_FAIL_TYPE_MAPPING
				.put(MIN_LOWER_CASE_CHARS_MESSAGE, PasswordChangeFailException.PasswordFailType.MIN_LOWER_CASE_CHARS);
		ERROR_KEY_TO_FAIL_TYPE_MAPPING
				.put(MIN_UPPER_CASE_CHARS_MESSAGE, PasswordChangeFailException.PasswordFailType.MIN_UPPER_CASE_CHARS);
		ERROR_KEY_TO_FAIL_TYPE_MAPPING
				.put(MIN_SPECIAL_CHARS_MESSAGE, PasswordChangeFailException.PasswordFailType.MIN_SPECIAL_CHARS);
	}

	@Inject
	private KeycloakClientProducer clientProducer;

	@Inject
	private KeycloakDeploymentRetriever deploymentRetriever;

	@Override
	public boolean changeUserPassword(String username, String oldPassword, String newPassword) {
		StringPair userAndTenant = SecurityUtil.getUserAndTenant(username);
		KeycloakDeployment deployment = deploymentRetriever.getDeployment(userAndTenant.getSecond());

		AccessTokenResponse tokenResponse = KeycloakAuthUtil
				.loginWithCredentials(deployment, userAndTenant.getFirst(), oldPassword);
		if (tokenResponse == null) {
			throw new PasswordChangeFailException(PasswordChangeFailException.PasswordFailType.WRONG_OLD_PASSWORD,
					"Current password confirmation failed");
		}

		CredentialRepresentation credential = new CredentialRepresentation();
		credential.setValue(newPassword);
		credential.setType(CredentialRepresentation.PASSWORD);
		credential.setTemporary(false);

		try {
			UsersResource usersResource = clientProducer.produceUsersResource();
			String userId = KeycloakApiUtil.retrieveUserId(usersResource, userAndTenant.getFirst());

			UserResource userResource = usersResource.get(userId);
			userResource.resetPassword(credential);

			LOGGER.info("Successfully changed password of: {}", username);
		} catch (BadRequestException e) {
			handlePasswordValidationError(e);
		} catch (WebApplicationException e) {
			throw new PasswordChangeFailException(PasswordChangeFailException.PasswordFailType.UNKNOWN_TYPE, e);
		} finally {
			logoutUser(deployment, tokenResponse, username);
		}

		return true;
	}

	private void logoutUser(KeycloakDeployment deployment, AccessTokenResponse tokenResponse, String username) {
		if (tokenResponse != null) {
			KeycloakAuthUtil.logout(deployment, tokenResponse, username);
		}
	}

	private void handlePasswordValidationError(BadRequestException exception) {
		String configuredPasswordPolicy = clientProducer.produceRealmResource().toRepresentation().getPasswordPolicy();
		Map<String, String> policyMapping = parse(configuredPasswordPolicy);

		JSON.readObject(exception.getResponse().readEntity(String.class), json -> {
			String errorKey = json.getString(JSON_ERROR_KEY);
			throw new PasswordChangeFailException(getFailType(errorKey), policyMapping.get(errorKey),
					json.getString(JSON_ERROR_DESCRIPTION_KEY));
		});
	}

	private Map<String, String> parse(String policyString) {
		Map<String, String> policyMap = new HashMap<>();
		if (StringUtils.isNotBlank(policyString)) {
			for (String policy : POLICY_SEPARATOR_PATTERN.split(policyString)) {
				policy = policy.trim();

				String key;
				String config = "";

				int indexOfOpeningBracket = policy.indexOf('(');
				if (indexOfOpeningBracket == -1) {
					key = policy;
				} else {
					key = policy.substring(0, indexOfOpeningBracket);
					config = policy.substring(indexOfOpeningBracket + 1, policy.length() - 1);
				}

				policyMap.put(ERROR_MAPPING.get(key), config);
			}
		}
		return policyMap;
	}

	private static PasswordChangeFailException.PasswordFailType getFailType(String errorKey) {
		return ERROR_KEY_TO_FAIL_TYPE_MAPPING
				.getOrDefault(errorKey, PasswordChangeFailException.PasswordFailType.UNKNOWN_TYPE);
	}

	@Override
	public String getName() {
		return SecurityConfiguration.KEYCLOAK_IDP;
	}
}
