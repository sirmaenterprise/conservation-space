/**
 * Copyright (c) 2010 15.08.2010 , Sirma ITT.
 */
package com.sirma.itt.emf.mail;

import org.apache.log4j.Logger;

import com.sirma.itt.commons.utils.string.StringUtils;

/**
 * Builder class {@link MailConfiguration}s. The configuration is build based on various sources.
 * 
 * @author B.Bonev
 */
public class MailConfigurationBuilder {

	/**
	 * Logger.
	 */
	private static Logger LOGGER = Logger
			.getLogger(MailConfigurationBuilder.class);

	/**
	 * Builds and configures configuration instance for sending email messages based on the system
	 * configuration. The builded instance can be used for initializing a {@link MailSender}.
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
	 * @see MailSender#MailSender(MailConfiguration)
	 */
	public MailConfiguration buildOutgoingConfiguration(String host,
			Integer port, String from, String securityType) {
		MailConfiguration configuration;
		// create base configuration
		if (MailConfiguration.SECURITY_TYPE_SSL.equalsIgnoreCase(securityType)) {
			configuration = MailConfiguration.createSMTPSConfiguration();
			// sets some defaults
			configuration.enableSSL(Boolean.TRUE);
			configuration
					.setSSLSocketFactoryClass(MailConfiguration.DEFAULT_SSL_SOCKET_FACTORY);
			configuration.setSocketFactoryFallback(Boolean.FALSE);
			configuration.setSslTrust(MailConfiguration.DEFAULT_SSL_TRUST_ZONE);
		} else {
			configuration = MailConfiguration.createSMTPConfiguration();
			if (MailConfiguration.SECURITY_TYPE_TLS
					.equalsIgnoreCase(securityType)) {
				configuration.enableTLS(Boolean.TRUE);
				// optional
				configuration.setTLSRequired(Boolean.TRUE);
				configuration.setSocketFactoryFallback(Boolean.FALSE);
			}
		}

		// set server host address
		if (StringUtils.isNotNullOrEmpty(host)) {
			configuration.setServerHost(host);
		} else {
			LOGGER.warn("No SMTP server host specified! Using defaults: localhost");
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
}
