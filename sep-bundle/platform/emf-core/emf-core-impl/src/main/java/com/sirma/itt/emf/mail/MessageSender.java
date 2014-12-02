/**
 *
 */
package com.sirma.itt.emf.mail;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;

/**
 * Constructs email configuration using external parameters and sends email messages.
 *
 * @author Adrian Mitev
 */
@ApplicationScoped
public class MessageSender {

	/** Server host inject */
	@Inject
	@Config(name = MailConfigurationProperties.MAIL_SERVER_HOST)
	private String host;

	/** Server port inject */
	@Inject
	@Config(name = MailConfigurationProperties.MAIL_SERVER_PORT, defaultValue = "25")
	private int port;

	/** sender email inject */
	@Inject
	@Config(name = MailConfigurationProperties.MAIL_SENDER)
	private String from;

	/** username inject */
	@Inject
	@Config(name = MailConfigurationProperties.MAIL_USER_NAME)
	private String username;

	/** pass inject */
	@Inject
	@Config(name = MailConfigurationProperties.MAIL_PASSWORD)
	private String password;

	/** TLS enabled inject */
	@Inject
	@Config(name = MailConfigurationProperties.MAIL_SECURITY_TYPE, defaultValue = MailConfiguration.SECURITY_TYPE_NO_SECURITY)
	private String securityType;

	@Inject
	private MailConfigurationBuilder configurationBuilder;

	/**
	 * Cached instance of the sender
	 */
	private MailSender sender;

	/**
	 * Creates a sender instance.
	 */
	@PostConstruct
	public void init() {
		// Create configuration
		MailConfiguration configuration = configurationBuilder.buildOutgoingConfiguration(host,
				port, username, securityType);

		// Create new mail sender and set the authenticator if needed.
		sender = new MailSender(configuration);
		if (StringUtils.isNotNullOrEmpty(username)) {
			sender.setAuthenticator(new SMTPAuthenticator(username, password));
		}
	}

	/**
	 * Sends an email message.
	 *
	 * @param message
	 *            message to send.
	 */
	public void sendMessage(MailMessage message) {
		// Send mail
		sender.postMail(message);
	}

	/**
	 * Constructs a message object with for text/html content.
	 *
	 * @param recepients
	 *            mail recipients.
	 * @param subject
	 *            mail subject.
	 * @param message
	 *            mail content.
	 * @param attachments
	 *            message attachments.
	 * @return constructed message.
	 */
	MailMessage constructMessage(String message, String subject, String[] recepients,
			MailAttachment[] attachments) {
		return constructMessage(message, subject, from, recepients, attachments);
	}

	/**
	 * Constructs a message object with for text/html content.
	 *
	 * @param message
	 *            mail content.
	 * @param subject
	 *            mail subject.
	 * @param from
	 *            send from mail
	 * @param recepients
	 *            mail recipients.
	 * @param attachments
	 *            message attachments.
	 * @return constructed message.
	 */
	MailMessage constructMessage(String message, String subject, String from, String[] recepients,
			MailAttachment[] attachments) {
		MailMessage mailMessage = new MailMessage();
		mailMessage.setFrom(from);
		mailMessage.setMimeFormat("text/html; charset=UTF-8");
		mailMessage.setSubject(subject);
		mailMessage.setRecipients(recepients);
		mailMessage.setContent(message);
		mailMessage.setAttachments(attachments);
		return mailMessage;
	}
}
