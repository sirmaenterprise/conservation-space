package com.sirma.itt.seip.mail;

import java.lang.invoke.MethodHandles;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.mail.attachments.MailAttachment;
import com.sirma.itt.seip.mail.attachments.MailAttachmentService;

/**
 * Constructs email configuration using external parameters and sends email messages.
 *
 * @author Adrian Mitev
 */
@ApplicationScoped
public class MessageSender {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@ConfigurationPropertyDefinition(sensitive = true, label = "Mail server host name")
	private static final String MAIL_SERVER_HOST = "mail.serverHost";
	@ConfigurationPropertyDefinition(type = Integer.class, defaultValue = "25", sensitive = true, label = "Mail server port")
	private static final String MAIL_SERVER_PORT = "mail.serverPort";
	@ConfigurationPropertyDefinition(sensitive = true, label = "Mail server host name to be used for MAIL FROM property in the send messages. If nothing is specified then the value from mail.serverHost will be used.")
	private static final String MAIL_SERVER_FROM = "mail.server.from";
	@ConfigurationPropertyDefinition(sensitive = true, label = "Default server user name")
	private static final String MAIL_USER_NAME = "mail.username";
	@ConfigurationPropertyDefinition(sensitive = true, password = true, label = "Default server user password")
	private static final String MAIL_CREDENTIAL = "mail.password";
	@ConfigurationPropertyDefinition(defaultValue = MailConfiguration.SECURITY_TYPE_NO_SECURITY, sensitive = true, label = "The of security to use when communication to the mail server. Valid values are: NO, TLS, SSL.")
	private static final String MAIL_SECURITY_TYPE = "mail.security.type";

	@ConfigurationGroupDefinition(type = MailSender.class, properties = { MAIL_SERVER_HOST, MAIL_SERVER_PORT,
			MAIL_USER_NAME, MAIL_CREDENTIAL, MAIL_SECURITY_TYPE,
			MAIL_SERVER_FROM }, label = "Mail sender instance that perform the actual mail server communication")
	private static final String MAIL_SENDER_INSTANCE = "mail.sender.instance";

	@Inject
	@Configuration(MAIL_SENDER_INSTANCE)
	private ConfigurationProperty<MailSender> mailSender;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "mail.sender", sensitive = true, label = "Default mail sender")
	private ConfigurationProperty<String> sendFrom;

	@ConfigurationConverter(MAIL_SENDER_INSTANCE)
	static MailSender buildSender(GroupConverterContext context, MailAttachmentService attachmentService) {
		String host = context.get(MAIL_SERVER_HOST);
		Integer port = context.get(MAIL_SERVER_PORT);
		String from = context.get(MAIL_SERVER_FROM);
		String securityType = context.get(MAIL_SECURITY_TYPE);
		// Create configuration
		MailConfiguration configuration = buildOutgoingConfiguration(host, port, from, securityType);

		String username = context.get(MAIL_USER_NAME);
		String password = context.get(MAIL_CREDENTIAL);
		// Create new mail sender and set the authenticator if needed.
		MailSender sender = new MailSender(configuration, attachmentService);
		if (StringUtils.isNotBlank(username)) {
			sender.setAuthenticator(new SMTPAuthenticator(username, password));
		}
		return sender;
	}

	/**
	 * Builds and configures configuration instance for sending email messages based on the system configuration. The
	 * builded instance can be used for initializing a {@link MailSender}.
	 *
	 * @param host
	 *            hostname of the mail server.
	 * @param port
	 *            port of the mail server.
	 * @param from
	 *            mail sender.
	 * @param securityType
	 *            type of the mail security.
	 * @return mail configuration instance.
	 * @see MailSender#MailSender(MailConfiguration, SMTPAuthenticator)
	 */
	private static MailConfiguration buildOutgoingConfiguration(String host, Integer port, String from, String securityType) {
		MailConfiguration configuration;
		// create base configuration
		if (MailConfiguration.SECURITY_TYPE_SSL.equalsIgnoreCase(securityType)) {
			configuration = MailConfiguration.createSMTPSConfiguration();
			// sets some defaults
			configuration.enableSSL(Boolean.TRUE);
			configuration.setSSLSocketFactoryClass(MailConfiguration.DEFAULT_SSL_SOCKET_FACTORY);
			configuration.setSocketFactoryFallback(Boolean.FALSE);
			configuration.setSslTrust(MailConfiguration.DEFAULT_SSL_TRUST_ZONE);
		} else {
			configuration = MailConfiguration.createSMTPConfiguration();
			if (MailConfiguration.SECURITY_TYPE_TLS.equalsIgnoreCase(securityType)) {
				configuration.enableTLS(Boolean.TRUE);
				// optional
				configuration.setTLSRequired(Boolean.TRUE);
				configuration.setSocketFactoryFallback(Boolean.FALSE);
			}
		}

		// set server host address
		if (StringUtils.isNotBlank(host)) {
			configuration.setServerHost(host);
		} else {
			LOGGER.warn("No SMTP server host specified! Using defaults: localhost");
		}

		if (StringUtils.isNotBlank(from)) {
			configuration.setServerFrom(from);
		}

		// set server port
		if (port != null) {
			configuration.setServerPort(port);
		} else {
			LOGGER.warn("The server port is not specified. Using default port for the protocol: "
					+ (configuration.isSMTPSProtocol() ? 467 : 25));
		}

		return configuration;
	}

	/**
	 * Sends an email message.
	 *
	 * @param message
	 *            message to send.
	 */
	public void sendMessage(MailMessage message) {
		if (StringUtils.isBlank(message.getFrom())) {
			message.setFrom(sendFrom.get());
		}
		mailSender.get().postMail(message);
	}

	/**
	 * Check if the mail sender is configured.
	 *
	 * @return true if the mail sender is configured
	 */
	public boolean isConfigured() {
		return sendFrom.isSet() && mailSender.isSet();
	}

	/**
	 * Constructs a message object with for text/html content.
	 *
	 * @param recepients
	 *            mail recipients.
	 * @param subject
	 *            mail subject.
	 * @param mailGroupId
	 *            the id of the mails which are with the same {@link MailMessage}. Used for querying more mails at once
	 * @param message
	 *            mail content.
	 * @param attachments
	 *            message attachments.
	 * @return constructed message.
	 */
	MailMessage constructMessage(String message, String subject, String mailGroupId, String[] recepients,
			MailAttachment[] attachments) {
		return constructMessage(message, subject, mailGroupId, sendFrom.get(), recepients, attachments);
	}

	/**
	 * Constructs a message object with for text/html content.
	 *
	 * @param message            mail content.
	 * @param subject            mail subject.
	 * @param mailGroupId        the id of the mails which are with the same {@link MailMessage}. Used for querying more mails at once
	 * @param recepients         mail recipients.
	 * @param ccRecipients       the "Cc" (carbon copy) recipients.
	 * @param attachments        message attachments.
	 * @return constructed message.
	 */
	public MailMessage constructMessage(String message, String subject, String mailGroupId, String[] recepients, String[] ccRecipients,
			MailAttachment[] attachments) {
		return constructMessage(message, subject, mailGroupId, sendFrom.get(), recepients, ccRecipients, attachments);
	}

	/**
	 * Constructs a message object with for text/html content.
	 *
	 * @param message
	 *            mail content.
	 * @param subject
	 *            mail subject.
	 * @param mailGroupId
	 *            the id of the mails which are with the same {@link MailMessage}. Used for querying more mails at once
	 * @param from
	 *            send from mail
	 * @param recepients
	 *            mail recipients.
	 * @param attachments
	 *            message attachments.
	 * @return constructed message.
	 */
	MailMessage constructMessage(String message, String subject, String mailGroupId, String from, String[] recepients,
			MailAttachment[] attachments) {
		return constructMessage(message, subject, mailGroupId, from, recepients, null, attachments);
	}

	/**
	 * Constructs a message object with for text/html content.
	 *
	 * @param message
	 *            mail content.
	 * @param subject
	 *            mail subject.
	 * @param mailGroupId
	 *            the id of the mails which are with the same {@link MailMessage}. Used for querying more mails at once
	 * @param from
	 *            send from mail
	 * @param recepients
	 *            mail recipients.
	 * @param attachments
	 *            message attachments.
	 * @return constructed message.
	 */
	private static MailMessage constructMessage(String message, String subject, String mailGroupId, String from, String[] recepients, String[] ccRecipients,
			MailAttachment[] attachments) {
		MailMessage mailMessage = new MailMessage();
		mailMessage.setFrom(from);
		mailMessage.setMimeFormat("text/html; charset=UTF-8");
		mailMessage.setSubject(subject);
		mailMessage.setRecipients(recepients);
		mailMessage.setCcRecipients(ccRecipients);
		mailMessage.setContent(message);
		mailMessage.setAttachments(attachments);
		mailMessage.setMailGroupId(mailGroupId);
		return mailMessage;
	}
}
