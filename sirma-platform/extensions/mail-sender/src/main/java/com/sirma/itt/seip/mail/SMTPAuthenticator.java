package com.sirma.itt.seip.mail;

import javax.mail.PasswordAuthentication;

/**
 * SimpleAuthenticator is used to do simple authentication when the SMTP server requires it.
 *
 * @author Borislav Bonev
 */
public class SMTPAuthenticator extends javax.mail.Authenticator {

	private String username;
	private String password;

	/**
	 * @param username
	 *            is SMTP user name
	 * @param password
	 *            is SMTP password
	 */
	public SMTPAuthenticator(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(username, password);
	}

}