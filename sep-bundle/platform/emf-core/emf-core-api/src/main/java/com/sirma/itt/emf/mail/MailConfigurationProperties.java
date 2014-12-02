package com.sirma.itt.emf.mail;

import com.sirma.itt.emf.configuration.Configuration;
import com.sirma.itt.emf.util.Documentation;

/**
 * Mail sending configurations.
 * 
 * @author Adrian Mitev
 */
@Documentation("Configurations for the email message component")
public interface MailConfigurationProperties extends Configuration {
	@Documentation("Mail server host name")
	String MAIL_SERVER_HOST = "mail.serverHost";
	@Documentation("Mail server port. <b>Default value is: 25</b>")
	String MAIL_SERVER_PORT = "mail.serverPort";
	@Documentation("Default mail sender")
	String MAIL_SENDER = "mail.sender";
	@Documentation("Default server user name")
	String MAIL_USER_NAME = "mail.username";
	@Documentation("Default server user password")
	String MAIL_PASSWORD = "mail.password";
	@Documentation("The of security to use when communication to the mail server. Valid values are: NO, TLS, SSL. <b>Default value is: NO</b>")
	String MAIL_SECURITY_TYPE = "mail.security.type";
}
