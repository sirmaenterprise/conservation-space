package com.sirma.itt.seip.mail;

import java.lang.invoke.MethodHandles;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;

import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.mail.attachments.MailAttachment;
import com.sirma.itt.seip.mail.attachments.MailAttachmentService;
import com.sun.mail.smtp.SMTPAddressFailedException;

/**
 * Class for sending messages via Java Mail API. To create an object instance you need an instance of the
 * {@link MailConfiguration} to pass.
 *
 * @author Borislav Bonev
 */
public class MailSender {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private MailConfiguration configuration;

	private SMTPAuthenticator authenticator;

	private boolean useAuthentication = false;

	private MailAttachmentService attachmentService;

	/**
	 * Creates a sender with the specified configuration without authentication.
	 *
	 * @param configuration
	 *            is the configuration to use when sending messages.
	 * @param attachmentService
	 *            the attachments service which handles the mails attachment
	 */
	public MailSender(MailConfiguration configuration, MailAttachmentService attachmentService) {
		this.configuration = configuration;
		this.attachmentService = attachmentService;
	}

	/**
	 * Creates a sender with the specified configuration and with authentication enabled.
	 *
	 * @param configuration
	 *            is the configuration to use when sending messages.
	 * @param authenticator
	 *            is the authentication to use to connect to the server.
	 */
	public MailSender(MailConfiguration configuration, SMTPAuthenticator authenticator) {
		this.configuration = configuration;
		setAuthenticator(authenticator);
	}

	/**
	 * Sends a e-mail message to the specified recipients using the configuration specified creation time or later using
	 * the setter method. All needed data must be filled in the specified {@link MailMessage} instance.
	 *
	 * @param mail
	 *            is the message to send.
	 */
	public void postMail(MailMessage mail) {
		try {
			// Set the host smtp address
			Properties properties = getConfiguration().createConfiguration();

			Session session = createNewSession(properties);
			boolean debug = getConfiguration().isDebug();
			session.setDebug(debug);

			// create a message
			MimeMessage message = new MimeMessage(session);

			// set the from and to address
			InternetAddress addressFrom = new InternetAddress(mail.getFrom());
			message.setFrom(addressFrom);

			setRecipients(mail.getRecipients(), message);
			setCcRecipients(mail.getCcRecipients(), message);


			// Setting the Subject
			message.setSubject(mail.getSubject());

			// Add content and attachments if there are any
			addContentAndAttachments(mail.getContent(), mail.getMimeFormat(), mail.getAttachments(), message);

			doSend(session, message);
		} catch (AddressException e) {
			throw new MailSendingException(MailSendingException.MailSendingErrorType.INVALID_ADDRESS, e.getMessage(),
					e);
		} catch (SendFailedException e) {
			if (e.getCause() instanceof SMTPAddressFailedException) {
				throw new MailSendingException(MailSendingException.MailSendingErrorType.UKNOWN_RECEPIENT,
						e.getCause().getMessage(), e);
			}
			throw new MailSendingException(MailSendingException.MailSendingErrorType.UKNOWN_REASON, e);
		} catch (AuthenticationFailedException e) {
			throw new MailSendingException(MailSendingException.MailSendingErrorType.AUTHENTICATION_FAILED,
					e.getMessage(), e);
		} catch (MessagingException e) {
			if (e.getCause() instanceof UnknownHostException || e.getCause() instanceof ConnectException) {
				throw new MailSendingException(MailSendingException.MailSendingErrorType.CONNECTION_FAILED,
						e.getCause().getMessage(), e);
			}
			throw new MailSendingException(MailSendingException.MailSendingErrorType.UKNOWN_REASON, e);
		}
	}

	/**
	 * Sends a e-mail message to the specified recipients using the configuration specified creation time or later using
	 * the setter method.
	 *
	 * @param recipients
	 *            are the message recipients
	 * @param subject
	 *            is the subject of the message to send
	 * @param messageContent
	 *            is the message content to send
	 * @param from
	 *            is the sending e-mail address specified in <code>From:</code> part.
	 * @param mimeFormat
	 *            the outgoing message MIME format.
	 * @param attachments
	 *            mail attachments.
	 */
	public void postMail(String[] recipients, String subject, String messageContent, String from, String mimeFormat,
			MailAttachment[] attachments) {
		MailMessage mailMessage = new MailMessage();
		mailMessage.setRecipients(recipients);
		mailMessage.setSubject(subject);
		mailMessage.setContent(messageContent);
		mailMessage.setFrom(from);
		mailMessage.setMimeFormat(mimeFormat);
		mailMessage.setAttachments(attachments);
		postMail(mailMessage);
	}

	private static void setRecipients(String[] recipients, MimeMessage message) throws MessagingException {
		InternetAddress[] addressTo = Stream
				.of(recipients)
					.filter(StringUtils::isNotBlank)
					.map(MailSender::getNewAddress)
					.filter(Objects::nonNull)
					.toArray(InternetAddress[]::new);
		message.setRecipients(Message.RecipientType.TO, addressTo);
	}

	/**
	 * Add the "Cc" (carbon copy) recipients to <code>message</code> if any.
	 *
	 * @param cc - the "Cc" (carbon copy) recipients.
	 * @param message -the message.
	 * @throws MessagingException the messaging exception
	 */
	private static void setCcRecipients(String[] cc, MimeMessage message) throws MessagingException {
		InternetAddress[] addressTo = Stream
				.of(cc)
					.filter(StringUtils::isNotBlank)
					.map(MailSender::getNewAddress)
					.filter(Objects::nonNull)
					.toArray(InternetAddress[]::new);
		message.setRecipients(Message.RecipientType.CC, addressTo);
	}

	/**
	 * Creates new {@link InternetAddress}.
	 *
	 * @param address
	 *            the address of the recipient
	 * @return new {@link InternetAddress} object or null if the address is invalid
	 */
	static InternetAddress getNewAddress(String address) {
		try {
			return new InternetAddress(address);
		} catch (AddressException e) {
			LOGGER.warn("The address [{}] is invalid.", address);
			LOGGER.debug("Invalid address.", e);
		}
		return null;
	}

	private Session createNewSession(Properties properties) {
		Session session;
		if (getUseAuthentication()) {
			session = Session.getInstance(properties, getAuthenticator());
		} else {
			session = Session.getInstance(properties);
		}
		return session;
	}

	private void doSend(Session session, MimeMessage message) throws MessagingException {
		String protocol = getConfiguration().isSMTPSProtocol() ? "smtps" : "smtp";
		Transport transport = session.getTransport(protocol);
		if (transport != null) {
			// localhost is the default host is no host is specified
			String host = getConfiguration().getProperty(MailConfiguration.HOST, "localhost");
			// -1 is the default value for no specified port in the
			// Transport
			// implementation
			int port = Integer.parseInt(getConfiguration().getProperty(MailConfiguration.PORT, "-1"));
			if (getUseAuthentication()) {
				PasswordAuthentication authentication = getAuthenticator().getPasswordAuthentication();
				// connects to server using specified user name and password
				transport.connect(host, port, authentication.getUserName(), authentication.getPassword());
				transport.sendMessage(message, message.getAllRecipients());
			} else {
				transport.connect(host, port, null, null);
				transport.sendMessage(message, message.getAllRecipients());
			}
			transport.close();
		} else {
			LOGGER.warn("The specified protocol {} was not configured. Using defaults.", protocol);
			Transport.send(message);
		}
	}

	private void addContentAndAttachments(String messageContent, String mimeFormat, MailAttachment[] attachments,
			MimeMessage message) throws MessagingException {
		if (attachments != null && attachments.length > 0) {
			Multipart multiPart = new MimeMultipart();

			// add body content
			MimeBodyPart bodyPart = new MimeBodyPart();
			bodyPart.addHeader("Content-Type", mimeFormat);
			bodyPart.setContent(messageContent, mimeFormat);
			multiPart.addBodyPart(bodyPart);

			Collection<MimeBodyPart> attachmentParts = attachmentService.getAttachmentParts(attachments);
			for (MimeBodyPart part : attachmentParts) {
				multiPart.addBodyPart(part);
			}

			LOGGER.info("[{}] parts are added to the mail.", attachmentParts.size());
			message.setContent(multiPart);
			return;
		}
		message.setContent(messageContent, mimeFormat);
	}

	/**
	 * Sets if the when sending message to use authentication. The default is <code>false</code>.
	 *
	 * @param useAuthentication
	 *            the useAuthentication to set
	 */
	public void setUseAuthentication(boolean useAuthentication) {
		this.useAuthentication = useAuthentication;
	}

	/**
	 * Returns if when sending message to use authentication. The default is <code>false</code>.
	 *
	 * @return <code>true</code> if is going to use authentication when sending message and <code>false</code>
	 *         otherwise.
	 */
	public boolean getUseAuthentication() {
		return useAuthentication;
	}

	/**
	 * Sets the authentication credentials to use when sending message. Calling this method with not <code>null</code>
	 * argument will automatically enable sending with authentication.
	 *
	 * @param authenticator
	 *            the authenticator to set
	 */
	public void setAuthenticator(SMTPAuthenticator authenticator) {
		this.authenticator = authenticator;
		if (authenticator != null) {
			setUseAuthentication(true);
		}
	}

	/**
	 * Returns the used authentication.
	 *
	 * @return the authenticator
	 */
	public SMTPAuthenticator getAuthenticator() {
		return authenticator;
	}

	/**
	 * Getter method for configuration.
	 *
	 * @return the configuration
	 */
	public MailConfiguration getConfiguration() {
		return configuration;
	}

}
