/**
 *
 */
package com.sirma.itt.seip.alfresco4.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.joda.time.DateTime;

import com.sirma.itt.seip.PropertyConfigsWrapper;

/**
 * Base class for calling web scripts.
 *
 * @author Borislav Bonev
 */
public class AbstractRESTClient implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8168400014809201723L;

	/** The host. */
	private String host;

	/** The port. */
	private int port;

	/** The Constant UTF_8. */
	public static final String UTF_8 = "UTF-8";

	/** The Constant RH_PARAM_ACCEPT. */
	public static final String RH_PARAM_ACCEPT = "Accept";

	/** The Constant MIME_TYPE_APPLICATION_JSON. */
	public static final String MIME_TYPE_APPLICATION_JSON = "application/json";

	/** The Constant HTTP_SOCKET_TIMEOUT. */
	public static final String HTTP_SOCKET_TIMEOUT = "http.socket.timeout";

	/** The Constant HEADER_COOKIE. */
	public static final String HEADER_COOKIE = "Cookie";

	/** The Constant HEADER_SET_COOKIE. */
	public static final String HEADER_SET_COOKIE = "Set-Cookie";

	/** The Constant PROP_JSESSIONID. */
	public static final String PROP_JSESSIONID = "JSESSIONID";

	/** The Constant SERVICE_BASE_URI. */
	public static final String SERVICE_BASE_URI = "/alfresco/service";

	/** The Constant PROP_ACCEPT_ENCODING. */
	public static final String PROP_ACCEPT_ENCODING = "Accept-Encoding";

	/** The Constant PROP_CONTENT_ENCODING. */
	public static final String PROP_CONTENT_ENCODING = "Content-Encoding";

	/** The Constant COMPRESSION_FORMAT_GZIP. */
	public static final String COMPRESSION_FORMAT_GZIP = "gzip";

	/** The Constant PROTOCOL. */
	public static final String PROTOCOL = "protocol";

	/** The Constant PROTOCOL_HTTP. */
	public static final String PROTOCOL_HTTP = "http";

	/** The Constant PROTOCOL_HTTPS. */
	public static final String PROTOCOL_HTTPS = "https";

	/** The Constant DEFAULT_HTTP_PORT. */
	public static final String DEFAULT_HTTP_PORT = "8080";

	/** The Constant DEFAULT_HTTPS_PORT. */
	public static final String DEFAULT_HTTPS_PORT = "8443";

	/** The use authentication. */
	private boolean useAuthentication = false;

	/** The use dms service base. */
	private boolean useDmsServiceBase = true;

	private String protocol = PROTOCOL_HTTP;

	/** The http state. */
	private HttpState httpState;

	/** The session id. */
	private static String sessionId;

	/** The default timeout. */
	private static int defaultTimeout = 0;

	/** The timeout. */
	private int timeout = 0;

	/** The credentials set. */
	private boolean credentialsSet;

	/** The user. */
	private String user;

	/** The pass. */
	private String pass;

	/** The ticket. */
	private String ticket;

	/**
	 * Calls the given web script URI with the given {@link HttpMethod}. The server host and port need to be initialized
	 * via constructor or method
	 *
	 * @param uri
	 *            is the script URI
	 * @param method
	 *            is the method to use
	 * @return the response body as string or <code>null</code> if exception occur
	 *         {@link #setServerLocation(String, int)} or {@link #setDefaultCredentials(String, int, String, String)}
	 */
	public InputStream invoke(String uri, HttpMethod method) {
		if (!credentialsSet) {
			String[] authentication = getAuthentication();
			String username = authentication[0];
			String password = authentication[1];
			if (ticket != null) {
				String ticketUpdated = ticket;
				// add the saml ticket
				method.addRequestHeader("SAMLToken", ticketUpdated);
				debug("INVOKING WEBSCRIPT @ " + uri + " user:" + username + " token:" + ticketUpdated);
			} else if (username != null && password != null) {
				setDefaultCredentials(getHost(), getPort(), username, password);
				debug("INVOKING WEBSCRIPT @ " + uri + " user:" + username + " pass:" + password);
			}
		}
		method.addRequestHeader("SAMLToken", getTicket());

		HttpClient client = getClient();
		if (timeout > 0) {
			method.getParams().setIntParameter(HTTP_SOCKET_TIMEOUT, timeout);
		} else if (defaultTimeout > 0) {
			method.getParams().setIntParameter(HTTP_SOCKET_TIMEOUT, defaultTimeout);
		}
		try {
			setUri(method, uri);
			method.setDoAuthentication(isUseAuthentication());
			setSessionId(method);
			client.executeMethod(method);
			saveSessionId(method);
			// get the stream result - probably instanceof AutoCloseStream
			InputStream inputStream = method.getResponseBodyAsStream();
			int statusCode = method.getStatusCode();
			debug("Received response with status code: " + statusCode + "  @ " + uri);
			if (statusCode == 200) {
				return inputStream;
			} else {
				// just print the msg
				debug("Received response  : " + getResponse(inputStream));
			}
		} catch (SocketTimeoutException e) {
			throw new IllegalArgumentException("Timeout has occurred on a socket read or accept", e);
		} catch (URIException e) {
			throw new IllegalArgumentException("URI parsing and escape encoding exception", e);
		} catch (HttpException e) {
			throw new IllegalArgumentException("HTTP or HttpClient exception has occurred", e);
		} catch (ConnectException e) {
			throw new IllegalArgumentException("The connection was refused remotely", e);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("IP address of a host could not be determined", e);
		} catch (SocketException e) {
			throw new IllegalArgumentException("Error in the underlying protocol", e);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Gets the ticket.
	 *
	 * @return the ticket
	 */
	private String getTicket() {
		return ticket;
	}

	/**
	 * Gets the client.
	 *
	 * @return the client
	 */
	private HttpClient getClient() {
		HttpClient client = new HttpClient();
		if (httpState != null) {
			client.setState(httpState);
		}
		return client;
	}

	/**
	 * Initialize user credentials for the given host and port.
	 *
	 * @param host
	 *            is the destination host, required
	 * @param port
	 *            is the destination port
	 * @param user
	 *            is the user name
	 * @param password
	 *            is the password for the given user name
	 * @see #setServerLocation(String, int)
	 * @see #setCredentials(AuthScope, Credentials, HttpState)
	 */
	public void setDefaultCredentials(String host, int port, String user, String password) {
		if (host == null) {
			throw new IllegalArgumentException("Host is not defined!");
		}
		setServerLocation(host, port);
		if (user != null) {
			setCredentials(new AuthScope(getHost(), getPort()), new UsernamePasswordCredentials(user, password),
					new HttpState());
			ticket = createToken(user).replace("\r", "\t").replace("\n", "\t");
			credentialsSet = true;
		}
		this.user = user;
		pass = password;
	}

	/**
	 * Creates the token.
	 *
	 * @param adminUser
	 *            the admin user
	 * @return the string
	 */
	private static String createToken(String adminUser) {
		return encrypt(createResponse("http://localhost:8080/alfresco/ServiceLogin", "http://localhost:8081",
				"https://localhost:9448/samlsso", adminUser).toString());
	}

	/**
	 * Creates the response.
	 *
	 * @param alfrescoURL
	 *            the alfresco url
	 * @param audianceURL
	 *            the audiance url
	 * @param samlURL
	 *            the saml url
	 * @param user
	 *            the user
	 * @return the string buffer
	 */
	public static StringBuffer createResponse(String alfrescoURL, String audianceURL, String samlURL, String user) {

		DateTime now = new DateTime();

		DateTime barrier = now.plusMinutes(10);
		StringBuffer saml = new StringBuffer();
		saml
				.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
					.append("<saml2p:Response ID=\"inppcpljfhhckioclinjenlcneknojmngnmgklab\" IssueInstant=\"")
					.append(now.toString())
					.append("\" Version=\"2.0\" xmlns:saml2p=\"urn:oasis:names:tc:SAML:2.0:protocol\">")
					.append("<saml2p:Status>")
					.append("<saml2p:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\"/>")
					.append("</saml2p:Status>")
					.append("<saml2:Assertion ID=\"ehmifefpmmlichdcpeiogbgcmcbafafckfgnjfnk\" IssueInstant=\"")
					.append(now.toString())
					.append("\" Version=\"2.0\" xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\">")
					.append("<saml2:Issuer Format=\"urn:oasis:names:tc:SAML:2.0:nameid-format:entity\">")
					.append(samlURL)
					.append("</saml2:Issuer>")
					.append("<saml2:Subject>")
					.append("<saml2:NameID>")
					.append(user)
					.append("</saml2:NameID>")
					.append("<saml2:SubjectConfirmation Method=\"urn:oasis:names:tc:SAML:2.0:cm:bearer\">")
					.append("<saml2:SubjectConfirmationData InResponseTo=\"0\" NotOnOrAfter=\"")
					.append(barrier.toString())
					.append("\" Recipient=\"")
					.append(alfrescoURL)
					.append("\"/>")
					.append("</saml2:SubjectConfirmation>")
					.append("</saml2:Subject>")
					.append("<saml2:Conditions NotBefore=\"")
					.append(now.toString())
					.append("\" NotOnOrAfter=\"")
					.append(barrier.toString())
					.append("\">")
					.append("<saml2:AudienceRestriction>")
					.append("<saml2:Audience>")
					.append(audianceURL)
					.append("</saml2:Audience>")
					.append("</saml2:AudienceRestriction>")
					.append("</saml2:Conditions>")
					.append("<saml2:AuthnStatement AuthnInstant=\"")
					.append(now.toString())
					.append("\">")
					.append("<saml2:AuthnContext>")
					.append("<saml2:AuthnContextClassRef>urn:oasis:names:tc:SAML:2.0:ac:classes:Password</saml2:AuthnContextClassRef>")
					.append("</saml2:AuthnContext>")
					.append("</saml2:AuthnStatement>")
					.append("</saml2:Assertion>")
					.append("</saml2p:Response>");
		return saml;
	}

	/**
	 * Initialize the given {@link HttpState} with the given {@link AuthScope} and {@link Credentials}. Saves the state
	 * into the current instance.
	 *
	 * @param authScope
	 *            is the authentication scope to set
	 * @param credentials
	 *            are the user credentials to set
	 * @param httpState
	 *            is the destination {@link HttpState}
	 */
	public void setCredentials(AuthScope authScope, Credentials credentials, HttpState httpState) {
		httpState.setCredentials(authScope, credentials);
		this.httpState = httpState;
	}

	/**
	 * Initialize the destination host and port.
	 *
	 * @param host
	 *            is the host to set
	 * @param port
	 *            is the port to set
	 */
	public void setServerLocation(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * Sets the use authentication.
	 *
	 * @param useAuthentication
	 *            the useAuthentication to set
	 */
	public void setUseAuthentication(boolean useAuthentication) {
		this.useAuthentication = useAuthentication;
	}

	/**
	 * Checks if is use authentication.
	 *
	 * @return the useAuthentication
	 */
	public boolean isUseAuthentication() {
		return useAuthentication;
	}

	/**
	 * Getter method for host.
	 *
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Getter method for port.
	 *
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Creates and initialize an {@link HttpMethod} instance with the given parts.
	 *
	 * @param method
	 *            is the http method
	 * @param parts
	 *            is the part entries to set
	 * @param authentication
	 *            if to use an authentication
	 * @return the initialized {@link HttpMethod} instance
	 * @see #getMethod()
	 */
	public HttpMethod createMethod(HttpMethod method, Part[] parts, boolean authentication) {
		HttpMethodParams params = new HttpMethodParams();
		if (method instanceof EntityEnclosingMethod) {
			params.setHttpElementCharset(UTF_8);
			EntityEnclosingMethod enclosingMethod = (EntityEnclosingMethod) method;
			enclosingMethod.setRequestEntity(new MultipartRequestEntity(parts, params));
		}
		String useCompression = "false"; // Config.getInstance().getPropoperty(
		// Config.USE_COMPRESSION, "false");
		if (Boolean.valueOf(useCompression)) {
			method.addRequestHeader(PROP_CONTENT_ENCODING, COMPRESSION_FORMAT_GZIP);
			method.addRequestHeader(PROP_ACCEPT_ENCODING, COMPRESSION_FORMAT_GZIP);
		}

		setUseAuthentication(authentication);
		return method;
	}

	/**
	 * Creates and initialize an {@link HttpMethod} instance with the given content.
	 *
	 * @param method
	 *            is the http method
	 * @param content
	 *            is content to set
	 * @param authentication
	 *            if to use an authentication
	 * @return the initialized {@link HttpMethod} instance
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @see #getMethod()
	 */
	public HttpMethod createMethod(HttpMethod method, String content, boolean authentication)
			throws UnsupportedEncodingException {

		method.setParams(new HttpMethodParams());
		if (method instanceof EntityEnclosingMethod) {
			EntityEnclosingMethod enclosingMethod = (EntityEnclosingMethod) method;
			enclosingMethod.setRequestEntity(new StringRequestEntity(content, MIME_TYPE_APPLICATION_JSON, UTF_8));
		}
		String useCompression = "false"; // Config.getInstance().getPropoperty(
		// Config.USE_COMPRESSION, "false");
		if (Boolean.valueOf(useCompression)) {
			method.addRequestHeader(PROP_CONTENT_ENCODING, COMPRESSION_FORMAT_GZIP);
			method.addRequestHeader(PROP_ACCEPT_ENCODING, COMPRESSION_FORMAT_GZIP);
		}

		setUseAuthentication(authentication);
		return method;
	}

	/**
	 * Constructs and sets an URI to the given {@link HttpMethod}. In order to call this method
	 * {@link #setServerLocation(String, int)} or
	 *
	 * @param method
	 *            is the destination method to update
	 * @param uri
	 *            is the URI to set. {@link #setDefaultCredentials(String, int, String, String)} need to be called in
	 *            advance
	 * @see #setServerLocation(String, int)
	 * @see #setDefaultCredentials(String, int, String, String)
	 */
	protected void setUri(HttpMethod method, String uri) {
		String localUri = uri;
		if (isUseDmsServiceBase() && !localUri.startsWith(SERVICE_BASE_URI)) {
			if (!localUri.startsWith("/")) {
				localUri = "/" + localUri;
			}
			localUri = SERVICE_BASE_URI + localUri;
		}
		try {
			method.setURI(new URI(getProtocol(), null, getHost(), getPort(), localUri));
		} catch (URIException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieves the contents of the given {@link InputStream} as {@link String} .
	 *
	 * @param inputStream
	 *            is the stream to read.
	 * @return is the stream contents or <code>null</code> if error occure during reading.
	 */
	private String getResponse(InputStream inputStream) {
		if (inputStream == null) {
			return null;
		}
		BufferedReader reader = null;
		StringWriter writer = null;
		try {
			reader = new BufferedReader(new InputStreamReader(inputStream, UTF_8));
			if (inputStream.available() != -1) {
				writer = new StringWriter(inputStream.available());
			} else {
				writer = new StringWriter();
			}
			String line;
			while ((line = reader.readLine()) != null) {
				writer.write(line);
			}
			return writer.toString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}

	/**
	 * Adds an Accept request header for JSON format.
	 *
	 * @param method
	 *            is the method to update
	 * @return is the updated method
	 */
	protected HttpMethod setAcceptJson(HttpMethod method) {
		Header header = method.getRequestHeader(RH_PARAM_ACCEPT);
		if (header == null) {
			header = new Header(RH_PARAM_ACCEPT, MIME_TYPE_APPLICATION_JSON);
		} else {
			header = new Header(RH_PARAM_ACCEPT, header.getValue() + "; " + MIME_TYPE_APPLICATION_JSON);
		}
		method.setRequestHeader(header);
		return method;
	}

	/**
	 * Sets a session id to the given method if present.
	 *
	 * @param method
	 *            is the target method to update
	 */
	private static void setSessionId(HttpMethod method) {
		if (sessionId == null) {
			return;
		}
		Header header = method.getRequestHeader(HEADER_COOKIE);
		if (header == null) {
			header = new Header(HEADER_COOKIE, PROP_JSESSIONID + "=" + sessionId);
			method.addRequestHeader(header);
		} else {
			Header nHeader = new Header(HEADER_COOKIE, header.getValue() + "; " + PROP_JSESSIONID + "=" + sessionId);
			method.setRequestHeader(nHeader);
		}
	}

	/**
	 * Saves the session ID from the response header for later use.
	 *
	 * @param method
	 *            is the source method to get the id from
	 */
	private static void saveSessionId(HttpMethod method) {
		Header header = method.getResponseHeader(HEADER_SET_COOKIE);
		if (header != null) {
			HeaderElement[] elements = header.getElements();
			for (int i = 0; i < elements.length; i++) {
				HeaderElement element = elements[i];
				if (PROP_JSESSIONID.equals(element.getName())) {
					sessionId = element.getValue();
					break;
				}
			}
		}
	}

	/**
	 * Getter method for defaultTimeout.
	 *
	 * @return the defaultTimeout
	 */
	public static int getDefaultTimeout() {
		return defaultTimeout;
	}

	/**
	 * Setter method for defaultTimeout.
	 *
	 * @param defaultTimeout
	 *            the defaultTimeout to set
	 */
	public static void setDefaultTimeout(int defaultTimeout) {
		AbstractRESTClient.defaultTimeout = defaultTimeout;
	}

	/**
	 * Getter method for timeout.
	 *
	 * @return the timeout
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * Setter method for timeout.
	 *
	 * @param timeout
	 *            the timeout to set
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * Creates a UTF-8 encoded {@link StringPart}.
	 *
	 * @param key
	 *            is the key/name of the part
	 * @param value
	 *            is the value of the part
	 * @return the created part
	 */
	protected Part createStringPart(String key, String value) {
		return new StringPart(key, value, UTF_8);
	}

	/**
	 * Getter method for protocol.
	 *
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * Sets the protocol.
	 *
	 * @param protocol
	 *            the new protocol
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * Check is current protocol http.
	 *
	 * @return true if current protocol http, otherwise false.
	 */
	public boolean isProtocolHttp() {
		return getProtocol().equals(AbstractRESTClient.PROTOCOL_HTTP);
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.remote.RESTClient#invokeWithResponse(java.lang.String,
	 * org.apache.commons.httpclient.HttpMethod)
	 */
	/**
	 * Invoke with response.
	 *
	 * @param uri
	 *            the uri
	 * @param method
	 *            the method
	 * @return the string
	 */
	public String invokeWithResponse(String uri, HttpMethod method) {
		InputStream invoke = invoke(uri, method);
		if (invoke != null) {
			String response = getResponse(invoke);
			debug("Received response  : " + response);
			return response;
		}
		return null;
	}

	/**
	 * Gets the authentication.
	 *
	 * @return the authentication
	 */
	protected String[] getAuthentication() {
		return new String[] { user, pass };
	}

	/**
	 * Prints logging debug msg.
	 *
	 * @param msg
	 *            the message to print
	 */
	private void debug(String msg) {
		System.out.println(msg);
	}

	/**
	 * http://stackoverflow.com/questions/339004/java-encrypt-decrypt-user- name- and-password-from-a-configuration-file
	 *
	 * @param plainText
	 *            is the plain text
	 * @return the encrypted password or throws runtime exception on error
	 */
	@SuppressWarnings("restriction")
	public static String encrypt(String plainText) {
		// only the first 8 Bytes of the constructor argument are used
		// as material for generating the keySpec
		try {
			DESKeySpec keySpec = new DESKeySpec(
					PropertyConfigsWrapper.getInstance().getProperty("chipher").getBytes("UTF8"));
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey cipherKey = keyFactory.generateSecret(keySpec);
			sun.misc.BASE64Encoder base64encoder = new sun.misc.BASE64Encoder();
			// ENCODE plainTextPassword String
			byte[] cleartext = plainText.getBytes("UTF-8");

			Cipher cipher = Cipher.getInstance("DES"); // cipher is not
			// thread
			// safe
			cipher.init(Cipher.ENCRYPT_MODE, cipherKey);
			String encryptedPwd = base64encoder.encode(cipher.doFinal(cleartext));
			return encryptedPwd;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(plainText + " is not encrypted, due to exception: " + e.getMessage());
		}
	}

	/**
	 * Getter method for useDmsServiceBase.
	 *
	 * @return the useDmsServiceBase
	 */
	public boolean isUseDmsServiceBase() {
		return useDmsServiceBase;
	}

	/**
	 * Setter method for useDmsServiceBase.
	 *
	 * @param useDmsServiceBase
	 *            the useDmsServiceBase to set
	 */
	public void setUseDmsServiceBase(boolean useDmsServiceBase) {
		this.useDmsServiceBase = useDmsServiceBase;
	}
}
