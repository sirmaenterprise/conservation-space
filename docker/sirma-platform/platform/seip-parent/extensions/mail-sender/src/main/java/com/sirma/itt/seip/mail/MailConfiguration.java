/**
 * Copyright (c) 2010 13.08.2010 , Sirma ITT.
 */
package com.sirma.itt.seip.mail;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * Class helper for creating configuration for sending e-mails to SMTP or SMTPS servers.
 *
 * @author B.Bonev
 */
public class MailConfiguration {

	private static final String SMTP_PROTOCOL = "mail.smtp.";
	private static final String SMTPS_PROTOCOL = "mail.smtps.";
	/**
	 * The SMTP server to connect to.
	 */
	public static final String HOST = "host";
	/**
	 * The SMTP server port to connect to, if the connect() method doesn't explicitly specify one. Defaults to 25.
	 */
	public static final String PORT = "port";
	/**
	 * Defines the message origin server name. Defaults the mail server host.
	 */
	public static final String FROM = "from";
	/**
	 * If true, attempt to authenticate the user using the AUTH command. Defaults to false.
	 */
	public static final String AUTH = "auth";
	/**
	 * If true, enables the use of the STARTTLS command (if supported by the server) to switch the connection to a
	 * TLS-protected connection before issuing any login commands. Note that an appropriate trust store must configured
	 * so that the client will trust the server's certificate. Defaults to false.
	 */
	public static final String ENABLE_TLS = "starttls.enable";
	/**
	 * If true, requires the use of the STARTTLS command. If the server doesn't support the STARTTLS command, or the
	 * command fails, the connect method will fail. Defaults to false.
	 */
	public static final String TLS_REQUIRED = "starttls.required";
	/**
	 * Specifies the port to connect to when using the specified socket factory. If not set, the default port will be
	 * used.
	 */
	public static final String SOCKET_FACTORY_PORT = "socketFactory.port";
	/**
	 * If set, specifies the name of a class that implements the javax.net.SocketFactory interface. This class will be
	 * used to create SMTP sockets.
	 */
	public static final String SOCKET_FACTORY_CLASS = "socketFactory.class";
	/**
	 * If set to true, failure to create a socket using the specified socket factory class will cause the socket to be
	 * created using the java.net.Socket class. Defaults to true.
	 */
	public static final String SOCKET_FACTORY_FALLBACK = "socketFactory.fallback";

	/**
	 * If set to true, use SSL to connect and use the SSL port by default. Defaults to false for the "smtp" protocol and
	 * true for the "smtps" protocol.
	 */
	public static final String ENABLE_SSL = "ssl.enable";
	/**
	 * If set, and a socket factory hasn't been specified, enables use of a MailSSLSocketFactory. If set to "*", all
	 * hosts are trusted. If set to a whitespace separated list of hosts, those hosts are trusted. Otherwise, trust
	 * depends on the certificate the server presents.
	 */
	public static final String SSL_TRUST = "ssl.trust";
	/**
	 * Specifies the port to connect to when using the specified socket factory. If not set, the default port will be
	 * used.
	 */
	public static final String SSL_SOCKET_FACTORY_PORT = "ssl.socketFactory.port";
	/**
	 * If set, specifies the name of a class that extends the javax.net.ssl.SSLSocketFactory class. This class will be
	 * used to create SMTP SSL sockets.
	 */
	public static final String SSL_SOCKET_FACTORY_CLASS = "ssl.socketFactory.class";
	/**
	 * Specifies the SSL protocols that will be enabled for SSL connections. The property value is a whitespace
	 * separated list of tokens acceptable to the javax.net.ssl.SSLSocket.setEnabledProtocols method.
	 */
	public static final String SSL_PROTOCOLS = "ssl.protocols";

	/**
	 * If set, specifies that authentication should be used for communication with that username.
	 */
	public static final String USERNAME = "username";

	/**
	 * Specifies the password used if authentication is set.
	 */
	public static final String PASSWORD = "password"; // NOSONAR

	static final String DEFAULT_SSL_SOCKET_FACTORY = "com.sun.mail.util.MailSSLSocketFactory";
	static final String DEFAULT_SSL_TRUST_ZONE = "*";
	static final String SECURITY_TYPE_NO_SECURITY = "NO";
	static final String SECURITY_TYPE_TLS = "TLS";
	static final String SECURITY_TYPE_SSL = "SSL";

	private Properties configuration = new Properties();
	private final String protocol;
	private boolean debug;

	/**
	 * Constructor initializing the configuration protocol. One of the constants {@value #SMTP_PROTOCOL} or
	 * {@value #SMTPS_PROTOCOL} must be passed.
	 *
	 * @param protocol
	 *            is the configuration protocol
	 */
	private MailConfiguration(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * Creates instance for SMTP protocol communications.
	 *
	 * @return empty SMTP configuration
	 */
	public static MailConfiguration createSMTPConfiguration() {
		return new MailConfiguration(SMTP_PROTOCOL);
	}

	/**
	 * Creates instance for SMTPS protocol communications.
	 *
	 * @return empty SMTPS configuration
	 */
	public static MailConfiguration createSMTPSConfiguration() {
		return new MailConfiguration(SMTPS_PROTOCOL);
	}

	/**
	 * Creates configuration copy that is returned by the method.
	 *
	 * @return new configuration copy.
	 */
	public Properties createConfiguration() {
		return new Properties(configuration);
	}

	/**
	 * Sets a property value for the specified property key. The key format must be as the format of the defined
	 * properties in the class. The key must not include the protocol.
	 *
	 * @param key
	 *            is the key of the property.
	 * @param value
	 *            is the property value
	 */
	public void setProperty(String key, String value) {
		configuration.setProperty(protocol + key, value);
	}

	/**
	 * Retrieves a property value for the given key;
	 *
	 * @param key
	 *            is the key to look for.
	 * @return the property value.
	 */
	public String getProperty(String key) {
		return configuration.getProperty(protocol + key);
	}

	/**
	 * Retrieves a property value for the given key;
	 *
	 * @param key
	 *            is the key to look for.
	 * @param defaultValue
	 *            the default value if the given key is not found
	 * @return the property value.
	 */
	public String getProperty(String key, String defaultValue) {
		return configuration.getProperty(protocol + key, defaultValue);
	}

	/**
	 * Sets the mail server host name/address.
	 *
	 * @param host
	 *            the mail server host/address
	 * @see MailConfiguration#HOST
	 */
	public void setServerHost(String host) {
		setProperty(HOST, host);
		// set default value for the *from property as well
		setServerFrom(host);
	}

	/**
	 * Sets the mail server host to be used for MAIL FROM property. This identifies the origin mail server.
	 *
	 * @param host
	 *            the mail server host/address
	 * @see MailConfiguration#FROM
	 */
	public void setServerFrom(String host) {
		setProperty(FROM, host);
	}

	/**
	 * Sets the mail server port
	 *
	 * @param port
	 *            the port to set
	 * @see MailConfiguration#PORT
	 */
	public void setServerPort(Integer port) {
		setProperty(PORT, port.toString());
	}

	/**
	 * Enables/Disables the TLS secure connection
	 *
	 * @param tls
	 *            to enable/disable TLS connection
	 * @see MailConfiguration#ENABLE_TLS
	 */
	public void enableTLS(Boolean tls) {
		setProperty(ENABLE_TLS, tls.toString());
	}

	/**
	 * Sets if the TLS secure connection is required
	 *
	 * @param tls
	 *            to set if the TLS is required
	 * @see MailConfiguration#TLS_REQUIRED
	 */
	public void setTLSRequired(Boolean tls) {
		setProperty(TLS_REQUIRED, tls.toString());
	}

	/**
	 * Sets the socket factory class
	 *
	 * @param clazz
	 *            the factory class
	 * @see MailConfiguration#SOCKET_FACTORY_CLASS
	 */
	public void setSocketFactoryClass(String clazz) {
		setProperty(SOCKET_FACTORY_CLASS, clazz);
	}

	/**
	 * Sets the socket factory port
	 *
	 * @param port
	 *            the factory port
	 * @see MailConfiguration#SOCKET_FACTORY_PORT
	 */
	public void setSocketFactoryPort(Integer port) {
		setProperty(SOCKET_FACTORY_PORT, port.toString());
	}

	/**
	 * Sets the socket factory fallback if the connection is not possible.
	 *
	 * @param back
	 *            the factory class
	 * @see MailConfiguration#SOCKET_FACTORY_FALLBACK
	 */
	public void setSocketFactoryFallback(Boolean back) {
		setProperty(SOCKET_FACTORY_FALLBACK, back.toString());
	}

	/**
	 * Sets the SSL socket factory class
	 *
	 * @param clazz
	 *            the factory class
	 * @see MailConfiguration#SSL_SOCKET_FACTORY_CLASS
	 */
	public void setSSLSocketFactoryClass(String clazz) {
		setProperty(SSL_SOCKET_FACTORY_CLASS, clazz);
	}

	/**
	 * Sets the SSL socket factory port
	 *
	 * @param port
	 *            the factory port
	 * @see MailConfiguration#SSL_SOCKET_FACTORY_PORT
	 */
	public void setSSLSocketFactoryPort(Integer port) {
		setProperty(SSL_SOCKET_FACTORY_PORT, port.toString());
	}

	/**
	 * Sets the SSL trust addresses separated with space
	 *
	 * @param trust
	 *            the trust addresses
	 * @see MailConfiguration#SSL_TRUST
	 */
	public void setSslTrust(String trust) {
		setProperty(SSL_TRUST, trust);
	}

	/**
	 * Enables/Disables the SSL connection.
	 *
	 * @param ssl
	 *            enable/disable ssl connection
	 * @see MailConfiguration#ENABLE_SSL
	 */
	public void enableSSL(Boolean ssl) {
		setProperty(ENABLE_SSL, ssl.toString());
	}

	/**
	 * Sets the SSL protocols to use.
	 *
	 * @param protocols
	 *            the protocols to use
	 * @see MailConfiguration#SSL_PROTOCOLS
	 */
	public void setSSLProtocols(String protocols) {
		setProperty(SSL_PROTOCOLS, protocols);
	}

	/**
	 * Enables/Disables debugging information
	 *
	 * @param debug
	 *            the debug to set
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * Returns the current mode.
	 *
	 * @return the debug
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * Returns the current used protocol.
	 *
	 * @return the current protocol
	 */
	public boolean isSMTPSProtocol() {
		if (SMTPS_PROTOCOL.equals(protocol)) {
			return true;
		}
		return false;
	}

	/**
	 * Prints the current configuration as {@link String}.
	 *
	 * @return the printed configuration
	 */
	public String printConfiguration() {
		StringBuilder builder = new StringBuilder(300);
		builder.append("MailConfiguration[");
		Set<Entry<Object, Object>> entrySet = configuration.entrySet();
		for (Iterator<Entry<Object, Object>> iterator = entrySet.iterator();;) {
			Entry<Object, Object> entry = iterator.next();
			builder.append(entry.getKey()).append("=").append(entry.getValue());
			if (iterator.hasNext()) {
				builder.append(", ");
			} else {
				break;
			}
		}
		builder.append("]");
		return builder.toString();
	}

}
