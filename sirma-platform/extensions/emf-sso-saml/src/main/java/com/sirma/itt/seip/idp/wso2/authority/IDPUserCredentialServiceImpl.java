package com.sirma.itt.seip.idp.wso2.authority;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.idp.exception.IDPClientException;
import com.sirma.itt.seip.idp.wso2.WSO2IdPStubManagment;
import com.sirma.itt.seip.resources.event.UserPasswordChangeEvent;
import com.sirma.itt.seip.resources.security.PasswordChangeFailException;
import com.sirma.itt.seip.resources.security.PasswordChangeFailException.PasswordFailType;
import com.sirma.itt.seip.resources.security.UserCredentialService;
import com.sirma.itt.seip.security.util.SecurityUtil;

/**
 * The IDPUserCredentialService is the idp specific backend for {@link UserCredentialService}. Make use of
 * {@link WSO2IdPStubManagment}
 */
@ApplicationScoped
public class IDPUserCredentialServiceImpl implements UserCredentialService {

	private static final Logger LOGGER = Logger.getLogger(IDPUserCredentialServiceImpl.class);
	private static final int MIN_PASSWORD_LENGTH = 6;
	private static final int MAX_PASSWORD_LENGTH = 30;

	/** The user store manager. Use each time a fresh service instance. */
	@Inject
	private Instance<UserStoreManager> userStoreManager;

	@Inject
	private EventService eventService;

	@Override
	public boolean changeUserPassword(String username, String oldPassword, String newPassword)
			throws IDPClientException {
		validatePassword(oldPassword, newPassword);

		try {
			// IDP SP1 works only with username in domain mode
			StringPair userAndTenant = SecurityUtil.getUserAndTenant(username);
			userStoreManager.get().updateCredential(userAndTenant.getFirst(), newPassword, oldPassword);
			eventService.fire(new UserPasswordChangeEvent());
			return true;
		} catch (UserStoreException e) {
			throw new PasswordChangeFailException(PasswordFailType.WRONG_OLD_PASSWORD, e);
		} catch (Exception e) {
			LOGGER.error("Failed during password change process." + e.getMessage(), e);
			throw new IDPClientException(e);
		}
	}

	@Override
	public void validatePassword(String oldPassword, String newPassword) {
		checkForEmptyPasswordsAndThrowError(oldPassword, newPassword);
		if (oldPassword.equals(newPassword)) {
			throw new PasswordChangeFailException(PasswordFailType.SAME_PASSWORD,
					"The new password is the same as the old password");
		}
		if (newPassword.length() < MIN_PASSWORD_LENGTH) {
			throw new PasswordChangeFailException(PasswordFailType.SHORT_PASSWORD,
					"The new password is too short. Min allowed: " + MIN_PASSWORD_LENGTH);
		}
		if (newPassword.length() > MAX_PASSWORD_LENGTH) {
			throw new PasswordChangeFailException(PasswordFailType.LONG_PASSWORD,
					"The new password is too long. Max allowed: " + MAX_PASSWORD_LENGTH);
		}
	}

	private static void checkForEmptyPasswordsAndThrowError(String oldPassword, String newPassword) {
		if (StringUtils.isNullOrEmpty(newPassword)) {
			throw new PasswordChangeFailException(PasswordFailType.NEW_PASSWORD_EMPTY, "The new password is empty");
		}
		if (StringUtils.isNullOrEmpty(oldPassword)) {
			throw new PasswordChangeFailException(PasswordFailType.OLD_PASSWORD_EMPTY, "The old password is empty");
		}
	}

}
