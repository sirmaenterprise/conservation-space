package com.sirma.itt.seip.idp.wso2.authority;

import java.lang.invoke.MethodHandles;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.idp.wso2.WSO2IdPStubManagement;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.security.PasswordChangeFailException;
import com.sirma.itt.seip.resources.security.PasswordChangeFailException.PasswordFailType;
import com.sirma.itt.seip.resources.security.UserCredentialService;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.util.SecurityUtil;

/**
 * The IDPUserCredentialService is the idp specific backend for {@link UserCredentialService}. Make use of
 * {@link WSO2IdPStubManagement}
 */
@Extension(target = UserCredentialService.NAME, order = 1)
public class IDPUserCredentialServiceImpl implements UserCredentialService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final int MIN_PASSWORD_LENGTH = 6;
	private static final int MAX_PASSWORD_LENGTH = 30;

	@Inject
	private Instance<UserStoreManager> userStoreManager;

	@Override
	public boolean changeUserPassword(String username, String oldPassword, String newPassword) {
		validatePassword(oldPassword, newPassword);

		try {
			// IDP SP1 works only with username in domain mode
			StringPair userAndTenant = SecurityUtil.getUserAndTenant(username);
			userStoreManager.get().updateCredential(userAndTenant.getFirst(), newPassword, oldPassword);
			return true;
		} catch (UserStoreException e) {
			throw new PasswordChangeFailException(PasswordFailType.WRONG_OLD_PASSWORD, e);
		} catch (Exception e) {
			LOGGER.error("Failed during password change process.{} ", e.getMessage(), e);
			throw new PasswordChangeFailException(PasswordFailType.UNKNOWN_TYPE, e);
		}
	}

	void validatePassword(String oldPassword, String newPassword) {
		checkForEmptyPasswordsAndThrowError(oldPassword, newPassword);
		if (oldPassword.equals(newPassword)) {
			throw new PasswordChangeFailException(PasswordChangeFailException.PasswordFailType.SAME_PASSWORD,
					"The new password is the same as the old password");
		}
		if (newPassword.length() < MIN_PASSWORD_LENGTH) {
			throw new PasswordChangeFailException(PasswordChangeFailException.PasswordFailType.SHORT_PASSWORD,
					"The new password is too short. Min allowed: " + MIN_PASSWORD_LENGTH);
		}
		if (newPassword.length() > MAX_PASSWORD_LENGTH) {
			throw new PasswordChangeFailException(PasswordChangeFailException.PasswordFailType.LONG_PASSWORD,
					"The new password is too long. Max allowed: " + MAX_PASSWORD_LENGTH);
		}
	}

	private static void checkForEmptyPasswordsAndThrowError(String oldPassword, String newPassword) {
		if (StringUtils.isBlank(newPassword)) {
			throw new PasswordChangeFailException(PasswordChangeFailException.PasswordFailType.NEW_PASSWORD_EMPTY,
					"The new password is empty");
		}
		if (StringUtils.isBlank(oldPassword)) {
			throw new PasswordChangeFailException(PasswordChangeFailException.PasswordFailType.OLD_PASSWORD_EMPTY,
					"The old password is empty");
		}
	}

	@Override
	public String getName() {
		return SecurityConfiguration.WSO_IDP;
	}
}
