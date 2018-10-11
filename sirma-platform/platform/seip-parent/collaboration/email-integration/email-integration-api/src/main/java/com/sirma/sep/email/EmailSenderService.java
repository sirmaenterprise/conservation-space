package com.sirma.sep.email;

import com.sirma.sep.email.exception.EmailIntegrationException;

/**
 * Email sender service using mail server api to send mail messages to email
 * accounts.
 * 
 * @author georgi.ts
 *
 */
public interface EmailSenderService {
	/**
	 * Sends email message using mail service API
	 * 
	 * @param from
	 *            from field
	 * @param to
	 *            to field
	 * @param subject
	 *            subject field
	 * @param content
	 *            email content
	 * @param senderPassword
	 *            password of sender used for authentication
	 */
	void sendMessage(String from, String to, String subject, String content, String senderPassword)
			throws EmailIntegrationException;
}
