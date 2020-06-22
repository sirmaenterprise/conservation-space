package com.sirma.itt.seip.resources.security;

/**
 * Contains operations about account confirmation process.
 * <br>The process is started when new user is created.
 * <ol>
 * <li>An automatic email notification is send, from the IDP server, to the newly created user with a confirmation link
 * that the user should click and enter his password. The opened page is located in the application UI and not in the
 * IDP server</li>
 * <li>The link contains a parameter named {@code confirmationCode} that should be used in the methods bellow.</li>
 * <li>On the confirmation page should be rendered a captcha image provided from the method
 * {@link #retrieveCaptchaLink(String)} using the confirmation code</li>
 * <li>after the user fills his password and the captcha answer the method
 * {@link #confirmAccount(String, String, String, String, String)} should be called to finish the account activation</li>
 * </ol>
 *
 * @author smustafov
 * @author BBonev
 */
public interface AccountConfirmationService {

	/**
	 * Retrieves link to a captcha image that will be shown to the user.
	 *
	 * @param confirmationCode
	 *            that the user received for account confirmation
	 * @return link to the captcha image
	 */
	String retrieveCaptchaLink(String confirmationCode);

	/**
	 * Confirms account for given confirmation code.
	 * <br> May throw {@link AccountConfirmationFailedException} if
	 * <ul>
	 * <li>One of the required parameters is not passed</li>
	 * <li>Fails to communicate with the IDP server</li>
	 * <li>The IDP server responds with an error</li>
	 * </ul>
	 *
	 * @param username of the account that is about to be confirmed, required
	 * @param password of the account that is about to be confirmed, required
	 * @param confirmationCode that the user received for account confirmation, required
	 * @param captchaAnswer user answer of the captcha image, required
	 * @param tenantId id of the tenant that the user is created in
	 */
	void confirmAccount(String username, String password, String confirmationCode, String captchaAnswer,
			String tenantId);

}
