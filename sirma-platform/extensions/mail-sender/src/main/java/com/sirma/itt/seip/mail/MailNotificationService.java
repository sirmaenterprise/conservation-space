package com.sirma.itt.seip.mail;

/**
 * The MailNotificationService is class that dynamically builds a ftl template with the details provided in the
 * {@link MailNotificationContext}.
 *
 * @author bbanchev
 */
public interface MailNotificationService {

	/**
	 * Send email to the user specified by the delegate's data.
	 *
	 * @param delegate
	 *            the delegate
	 */
	void sendEmail(MailNotificationContext delegate);

	/**
	 * Send email to the user specified by the delegate's data.
	 *
	 * @param delegate
	 *            the delegate
	 * @param mailGroupId
	 *            the mail group id. Used to extract all mails that have the same message from the DB
	 */
	void sendEmail(MailNotificationContext delegate, String mailGroupId);

}
