package com.sirma.itt.emf.mail;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
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
import javax.mail.util.ByteArrayDataSource;

import org.apache.log4j.Logger;

import com.sirma.itt.emf.mail.MailAttachment;
import com.sirma.itt.emf.mail.MailMessage;
import com.sun.mail.smtp.SMTPAddressFailedException;

/**
 * Class for sending messages via Java Mail API. To create an object instance you need an instance
 * of the {@link MailConfiguration} to pass.
 * 
 * @author Borislav Bonev
 */
class MailSender {

	private Logger log = Logger.getLogger(this.getClass());

	private MailConfiguration configuration;
	private SMTPAuthenticator authenticator;
	private Boolean useAuthentication = Boolean.FALSE;

	/**
	 * Creates a sender with the specified configuration without authentication.
	 * 
	 * @param configuration
	 *            is the configuration to use when sending messages.
	 */
	public MailSender(MailConfiguration configuration) {
		this.configuration = configuration;
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
		this.setAuthenticator(authenticator);
	}

	/**
	 * Sends a e-mail message to the specified recipients using the configuration specified creation
	 * time or later using the setter method. All needed data must be filled in the specified
	 * {@link MailMessage} instance.
	 * 
	 * @param mail
	 *            is the message to send.
	 */
	public void postMail(MailMessage mail) {
		postMail(mail.getRecipients(), mail.getSubject(), mail.getContent(), mail.getFrom(),
				mail.getMimeFormat(), mail.getAttachments());
	}

	/**
	 * Sends a e-mail message to the specified recipients using the configuration specified creation
	 * time or later using the setter method.
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
	public void postMail(String[] recipients, String subject, String messageContent, String from,
			String mimeFormat, MailAttachment[] attachments) {
		try {
			// Set the host smtp address
			Properties properties = getConfiguration().createConfiguration();

			Session session;
			if (getUseAuthentication()) {
				session = Session.getInstance(properties, getAuthenticator());
			} else {
				session = Session.getInstance(properties);
			}
			boolean debug = getConfiguration().isDebug();
			session.setDebug(debug);

			// create a message
			MimeMessage message = new MimeMessage(session);

			// set the from and to address
			InternetAddress addressFrom = new InternetAddress(from);
			message.setFrom(addressFrom);

			InternetAddress[] addressTo = new InternetAddress[recipients.length];
			for (int i = 0; i < recipients.length; i++) {
				addressTo[i] = new InternetAddress(recipients[i]);
			}
			message.setRecipients(Message.RecipientType.TO, addressTo);

			// Setting the Subject
			message.setSubject(subject);

			// Add content and attachments if there are any
			if (attachments != null && attachments.length > 0) {
				Multipart multiPart = new MimeMultipart();

				// add body content
				MimeBodyPart bodyPart = new MimeBodyPart();
				bodyPart.addHeader("Content-Type", mimeFormat);
				bodyPart.setContent(messageContent, mimeFormat);
				multiPart.addBodyPart(bodyPart);

				for (MailAttachment attachment : attachments) {
					MimeBodyPart attachmentBodyPart = new MimeBodyPart();
					DataSource dataSource = new ByteArrayDataSource(attachment.getContent(),
							attachment.getMimeType());
					attachmentBodyPart.setDataHandler(new DataHandler(dataSource));
					attachmentBodyPart.setFileName(attachment.getFileName());
					multiPart.addBodyPart(attachmentBodyPart);
				}

				message.setContent(multiPart);
			} else {
				message.setContent(messageContent, mimeFormat);
			}

			String protocol = getConfiguration().isSMTPSProtocol() ? "smtps" : "smtp";
			Transport transport = session.getTransport(protocol);
			if (transport != null) {
				// localhost is the default host is no host is specified
				String host = getConfiguration().getProperty(MailConfiguration.HOST, "localhost");
				// -1 is the default value for no specified port in the
				// Transport
				// implementation
				Integer port = Integer.valueOf(getConfiguration().getProperty(
						MailConfiguration.PORT, "-1"));
				if (getUseAuthentication()) {
					PasswordAuthentication authentication = getAuthenticator()
							.getPasswordAuthentication();
					// connects to server using specified user name and password
					transport.connect(host, port, authentication.getUserName(),
							authentication.getPassword());
					transport.sendMessage(message, message.getAllRecipients());
				} else {
					transport.connect(host, port, null, null);
					transport.sendMessage(message, message.getAllRecipients());
				}
				transport.close();
			} else {
				log.warn("The specified protocol " + protocol
						+ " was not configured. Using defaults.");
				Transport.send(message);
			}
		} catch (AddressException e) {
			throw new MailSendingException(
					MailSendingException.MailSendingErrorType.INVALID_ADDRESS, e.getMessage(), e);
		} catch (SendFailedException e) {
			if (e.getCause() instanceof SMTPAddressFailedException) {
				throw new MailSendingException(
						MailSendingException.MailSendingErrorType.UKNOWN_RECEPIENT, e.getCause()
								.getMessage(), e);
			}
			throw new MailSendingException(MailSendingException.MailSendingErrorType.UKNOWN_REASON,
					e);
		} catch (AuthenticationFailedException e) {
			throw new MailSendingException(
					MailSendingException.MailSendingErrorType.AUTHENTICATION_FAILED,
					e.getMessage(), e);
		} catch (MessagingException e) {
			if (e.getCause() instanceof UnknownHostException
					|| e.getCause() instanceof ConnectException) {
				throw new MailSendingException(
						MailSendingException.MailSendingErrorType.CONNECTION_FAILED, e.getCause()
								.getMessage(), e);
			}
			throw new MailSendingException(MailSendingException.MailSendingErrorType.UKNOWN_REASON,
					e);
		}
	}

	/**
	 * Sets if the when sending message to use authentication. The default is <code>false</code>.
	 * 
	 * @param useAuthentication
	 *            the useAuthentication to set
	 */
	public void setUseAuthentication(Boolean useAuthentication) {
		this.useAuthentication = useAuthentication;
	}

	/**
	 * Returns if when sending message to use authentication. The default is <code>false</code>.
	 * 
	 * @return <code>true</code> if is going to use authentication when sending message and
	 *         <code>false</code> otherwise.
	 */
	public Boolean getUseAuthentication() {
		return useAuthentication;
	}

	/**
	 * Sets the authentication credentials to use when sending message. Calling this method with not
	 * <code>null</code> argument will automatically enable sending with authentication.
	 * 
	 * @param authenticator
	 *            the authenticator to set
	 */
	public void setAuthenticator(SMTPAuthenticator authenticator) {
		this.authenticator = authenticator;
		if (authenticator != null) {
			setUseAuthentication(Boolean.TRUE);
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
