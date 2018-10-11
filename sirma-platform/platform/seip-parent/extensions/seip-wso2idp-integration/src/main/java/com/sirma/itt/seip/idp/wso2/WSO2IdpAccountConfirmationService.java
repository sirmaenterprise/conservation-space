package com.sirma.itt.seip.idp.wso2;

import static com.sirma.itt.seip.collections.CollectionUtils.createHashMap;
import static com.sirma.itt.seip.resources.security.AccountConfirmationFailedException.ConfirmationFailType.ACCOUNT_LOCKED;
import static com.sirma.itt.seip.resources.security.AccountConfirmationFailedException.ConfirmationFailType.INVALID_CAPTCHA_ANSWER;
import static com.sirma.itt.seip.resources.security.AccountConfirmationFailedException.ConfirmationFailType.INVALID_CONFIRMATION_CODE;
import static com.sirma.itt.seip.resources.security.AccountConfirmationFailedException.ConfirmationFailType.INVALID_OR_EXPIRED_CONFIRMATION_CODE;
import static com.sirma.itt.seip.resources.security.AccountConfirmationFailedException.ConfirmationFailType.INVALID_PASSWORD;
import static com.sirma.itt.seip.resources.security.AccountConfirmationFailedException.ConfirmationFailType.INVALID_USERNAME;
import static com.sirma.itt.seip.resources.security.AccountConfirmationFailedException.ConfirmationFailType.UNEXPECTED_CODE;
import static com.sirma.itt.seip.resources.security.AccountConfirmationFailedException.ConfirmationFailType.UNKNOWN;
import static com.sirma.itt.seip.resources.security.AccountConfirmationFailedException.ConfirmationFailType.USER_DOES_NOT_EXIST;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryService;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException;
import org.wso2.carbon.identity.mgt.stub.beans.VerificationBean;

import com.sirma.itt.seip.idp.config.IDPConfiguration;
import com.sirma.itt.seip.resources.security.AccountConfirmationFailedException;
import com.sirma.itt.seip.resources.security.AccountConfirmationService;
import com.sirma.itt.seip.security.util.SecurityUtil;

/**
 * Implementation of WSO2 IDP account confirmation. It uses webservice protocol for communication with the IDP server
 * using an instance of {@link UserInformationRecoveryService}.
 *
 * @author smustafov
 */
@ApplicationScoped
class WSO2IdpAccountConfirmationService implements AccountConfirmationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Map<String, AccountConfirmationFailedException.ConfirmationFailType> FAIL_TYPES = createHashMap(8);

	@Inject
	private Instance<UserInformationRecoveryService> informationRecoveryService;
	@Inject
	private IDPConfiguration idpConfig;

	private Map<String, CaptchaInfoBean> captchaBeans = new HashMap<>();

	static {
		//https://docs.wso2.com/display/IS500/Error+Codes+and+Descriptions
		/*
			17001	This occurs if the user does not exist.
			17002	This error occurs when invalid credentials are provided.
			17003	This error occurs when an account is locked after multiple incorrect login attempts and the user attempts to log in again.
			18001	Invalid code provided to validate.
			18002	The key/confirmation code provided has expired.
			18003	Invalid user name is given.
			18004	Captcha answer is invalid.
			18013	Unexpected code provided to validate.
		 */
		FAIL_TYPES.put("17001", USER_DOES_NOT_EXIST);
		FAIL_TYPES.put("17002", INVALID_PASSWORD);
		FAIL_TYPES.put("17003", ACCOUNT_LOCKED);
		// idp returns 18001 if the confirmation code is expired or invalid (or already confirmed), besides what the
		// docs say
		FAIL_TYPES.put("18001", INVALID_OR_EXPIRED_CONFIRMATION_CODE);
		// did not found a scenario when 18002 code is returned, but still just for any case
		FAIL_TYPES.put("18002", INVALID_CONFIRMATION_CODE);
		FAIL_TYPES.put("18003", INVALID_USERNAME);
		FAIL_TYPES.put("18004", INVALID_CAPTCHA_ANSWER);
		FAIL_TYPES.put("18013", UNEXPECTED_CODE);
	}

	@Override
	public String retrieveCaptchaLink(String confirmationCode) {
		try {
			CaptchaInfoBean captcha = informationRecoveryService.get().getCaptcha();
			captchaBeans.put(confirmationCode, captcha);

			String imagePath = captcha.getImagePath();
			if (!imagePath.startsWith("/")) {
				imagePath = "/" + imagePath;
			}
			URI idpUrl = URI.create(idpConfig.getIdpServerURL().getOrFail());

			return new URI(idpUrl.getScheme(), null, idpUrl.getHost(), idpUrl.getPort(), imagePath, null, null).toString();
		} catch (RemoteException | UserInformationRecoveryServiceIdentityMgtServiceExceptionException | URISyntaxException e) {
			throw new AccountConfirmationFailedException(UNKNOWN, "Cannot generate captcha!", e);
		}
	}

	@Override
	public void confirmAccount(String username, String password, String confirmationCode, String captchaAnswer,
			String tenantId) {
		validate(username, password, confirmationCode, captchaAnswer);
		CaptchaInfoBean captchaInfoBean = captchaBeans.get(confirmationCode);

		String usernameWithTenant = SecurityUtil.buildTenantUserId(username, tenantId);
		if (captchaInfoBean != null) {
			UserInformationRecoveryService recoveryService = informationRecoveryService.get();

			CaptchaInfoBean bean = new CaptchaInfoBean();
			bean.setImagePath(captchaInfoBean.getImagePath());
			bean.setSecretKey(captchaInfoBean.getSecretKey());
			bean.setUserAnswer(captchaAnswer);

			try {
				VerificationBean verificationBean = recoveryService.verifyConfirmationCode(usernameWithTenant,
						confirmationCode, bean);

				if (verificationBean.getVerified()) {
					recoveryService.updatePassword(usernameWithTenant, verificationBean.getKey(), password);
					captchaBeans.remove(confirmationCode);
					LOGGER.info("User {} successfully confirmed his registration", usernameWithTenant);
				} else {
					handleVerificationError(verificationBean.getError(), usernameWithTenant);
				}
			} catch (RemoteException | UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
				throw new AccountConfirmationFailedException(UNKNOWN, usernameWithTenant, e);
			}
		} else {
			throw new AccountConfirmationFailedException(UNEXPECTED_CODE, usernameWithTenant);
		}
	}

	private static void validate(String username, String password, String confirmationCode, String captchaAnswer) {
		if (StringUtils.isBlank(username)) {
			throw new AccountConfirmationFailedException(INVALID_USERNAME, "[unknown]");
		}
		if (StringUtils.isBlank(password)) {
			throw new AccountConfirmationFailedException(INVALID_PASSWORD, username);
		}
		if (StringUtils.isBlank(captchaAnswer)) {
			throw new AccountConfirmationFailedException(INVALID_CAPTCHA_ANSWER, username);
		}
		if (StringUtils.isBlank(confirmationCode)) {
			throw new AccountConfirmationFailedException(INVALID_OR_EXPIRED_CONFIRMATION_CODE, username);
		}
	}

	private static void handleVerificationError(String errorResponse, String usernameWithTenant) {
		AccountConfirmationFailedException.ConfirmationFailType failType = FAIL_TYPES
				.getOrDefault(extractErrorCode(errorResponse), UNKNOWN);
		throw new AccountConfirmationFailedException(failType, usernameWithTenant);
	}

	private static String extractErrorCode(String errorResponse) {
		int indexOfSpace = errorResponse.indexOf(" ");
		if (indexOfSpace != -1) {
			return errorResponse.substring(0, indexOfSpace);
		}
		return errorResponse;
	}

}
