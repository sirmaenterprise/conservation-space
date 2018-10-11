package com.sirma.itt.seip.resources.security;

import java.io.Serializable;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

/**
 * DTO object that represents an account confirmation request when new user is entering his/her new password after
 * receiving an email setting new password or password reset.
 *
 * @author smustafov
 */
public class AccountConfirmationRequest implements Serializable {

	private static final long serialVersionUID = 6341461148116236953L;

	private final String username;
	private final String password;
	private final String confirmationCode;
	private final String captchaAnswer;
	private final String tenantId;

	AccountConfirmationRequest(String username, String password, String confirmationCode,
			String captchaAnswer, String tenantId) {
		this.username = username;
		this.password = password;
		this.confirmationCode = confirmationCode;
		this.captchaAnswer = captchaAnswer;
		this.tenantId = tenantId;
	}

	/**
	 * Getter method for username.
	 *
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Getter method for password.
	 *
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Getter method for confirmationCode.
	 *
	 * @return the confirmationCode
	 */
	public String getConfirmationCode() {
		return confirmationCode;
	}

	/**
	 * Getter method for captchaAnswer.
	 *
	 * @return the captchaAnswer
	 */
	public String getCaptchaAnswer() {
		return captchaAnswer;
	}

	/**
	 * Getter method for tenantId.
	 *
	 * @return the tenantId
	 */
	public String getTenantId() {
		return tenantId;
	}

	/**
	 * Builder class for {@link AccountConfirmationRequest} that performs validation before the actual build
	 *
	 * @author bbonev
	 */
	public static class AccountConfirmationRequestBuilder implements Serializable {

		private static final long serialVersionUID = -865656357101273889L;

		private String username;
		private String password;
		private String code;
		private String captchaAnswer;
		private String tenant;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getCaptchaAnswer() {
			return captchaAnswer;
		}

		public void setCaptchaAnswer(String captchaAnswer) {
			this.captchaAnswer = captchaAnswer;
		}

		public String getTenant() {
			return tenant;
		}

		public void setTenant(String tenant) {
			this.tenant = tenant;
		}

		/**
		 * Check if all required properties a filled (user name, password, captcha answer, confirmation code)
		 *
		 * @return true if all of the required properties are filled
		 */
		public boolean validate() {
			return StringUtils.isNotBlank(getUsername())
					&& StringUtils.isNotBlank(getCaptchaAnswer())
					&& StringUtils.isNotBlank(getCode())
					&& StringUtils.isNotBlank(getPassword());
		}

		/**
		 * Validate and build the {@link AccountConfirmationRequest} instance. If there is a missing parameter an empty
		 * Optional will be returned
		 *
		 * @return an AccountConfirmationRequest instance or empty optional if not valid
		 */
		public Optional<AccountConfirmationRequest> build() {
			if (!validate()) {
				return Optional.empty();
			}

			return Optional.of(
					new AccountConfirmationRequest(getUsername(), getPassword(), getCode(), getCaptchaAnswer(),
							getTenant()));
		}
	}

}
