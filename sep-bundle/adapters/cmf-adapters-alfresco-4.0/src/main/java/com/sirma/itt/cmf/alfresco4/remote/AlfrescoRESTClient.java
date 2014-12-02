/**
 *
 */
package com.sirma.itt.cmf.alfresco4.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.inject.Inject;

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
import org.apache.log4j.Logger;

import com.sirma.itt.cmf.alfresco4.AlfrescoErrorReader;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.remote.DMSClientException;
import com.sirma.itt.emf.remote.RESTClient;
import com.sirma.itt.emf.security.SecurityConfigurationProperties;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.security.model.UserWithCredentials;

/**
 * Base class for calling web scripts in alfresco. <br>
 * REVIEW remove internal state and reconsider for making it @ApplicationScoped
 * 
 * @author Borislav Bonev
 */
public class AlfrescoRESTClient implements RESTClient {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8168400014809201723L;
	/** the key for saml token. */
	public static final String SAML_TOKEN = "SAMLToken";
	/** The Constant LOGGER. */
	public static final Logger LOGGER = Logger.getLogger(AlfrescoRESTClient.class);

	private static final boolean DEBUG_ENABLED = LOGGER.isDebugEnabled();

	private static final boolean TRACE_ENABLED = LOGGER.isTraceEnabled();

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

	/** The Constant DEFAULT_HTTP_PORT. */
	public static final String DEFAULT_HTTP_PORT = "8080";

	/** The Constant DEFAULT_HTTPS_PORT. */
	public static final String DEFAULT_HTTPS_PORT = "8443";

	/** The host. */
	@Inject
	@Config(name = CmfConfigurationProperties.DMS_HOST)
	private String host;

	/** The port. */
	@Inject
	@Config(name = CmfConfigurationProperties.DMS_PORT)
	private int port;
	/** is sso enabled */
	@Inject
	@Config(name = SecurityConfigurationProperties.SECURITY_SSO_ENABLED, defaultValue = "false")
	private Boolean ssoEnabled = false;

	/** The use authentication. */
	private boolean useAuthentication = false;

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

	/** The ticket. */
	private String ticket;

	/** The use service path when building request URI. True by default. */
	private boolean useServicePath = true;

	@Override
	public InputStream request(HttpMethod method, String uri) throws DMSClientException {
		HttpMethod rawRequest = rawRequest(method, uri);
		try {
			return rawRequest.getResponseBodyAsStream();
		} catch (IOException e) {
			throw new DMSClientException(AlfrescoErrorReader.parse(e), 500);
		}
	}

	@Override
	public String request(String uri, HttpMethod method) throws DMSClientException {
		HttpMethod returned = rawRequest(method, uri);
		String response = getResponse(returned);
		trace("Received response  : ", response);
		return response;
	}

	@Override
	public HttpMethod rawRequest(HttpMethod method, String uri) throws DMSClientException {
		try {
			if (!credentialsSet) {
				String[] authentication = getAuthentication();
				String username = authentication[0];
				String password = authentication[1];
				String ticket = authentication[2];
				if (ssoEnabled && (ticket != null)) {
					// as header does not support \r\n in the middle of the message
					int size = ticket.length();
					String ticketUpdated = ticket.replace("\r\n", "\t");
					if (size == ticketUpdated.length()) {
						ticketUpdated = ticketUpdated.replace("\r", "\t").replace("\n", "\t");
					}
					// add the saml ticket
					method.addRequestHeader(SAML_TOKEN, ticketUpdated);
					debug("INVOKING WEBSCRIPT @ ", uri, " user:", username, " token: PROTECTED");
				} else if ((username != null) && (password != null)) {
					setDefaultCredentials(getHost(), getPort(), username, password);
					debug("INVOKING WEBSCRIPT @ ", uri, " user:", username, " pass: PROTECTED");
				} else {
					// REVIEW: could we still login without login information? - yes it depends on
					// alfresco security plan
					LOGGER.warn("Unauthenticated request!");
				}
			} else if (getTicket() != null) {
				method.addRequestHeader(SAML_TOKEN, getTicket());
			}

			HttpClient client = getClient();
			if (timeout > 0) {
				method.getParams().setIntParameter(HTTP_SOCKET_TIMEOUT, timeout);
			} else if (defaultTimeout > 0) {
				method.getParams().setIntParameter(HTTP_SOCKET_TIMEOUT, defaultTimeout);
			}
			method.setURI(buildFullURL(uri, isUseServicePath()));
			method.setDoAuthentication(isUseAuthentication());
			setSessionId(method);
			client.executeMethod(method);
			saveSessionId(method);
			// get the stream result - probably instanceof AutoCloseStream
			int statusCode = method.getStatusCode();
			debug("Received response with status code: ", statusCode, "  @ ", uri);
			if (statusCode == 200) {
				return method;
			}
			throw new DMSClientException(AlfrescoErrorReader.parse(getResponse(method)),
					statusCode);
		} catch (SocketTimeoutException e) {
			throw new DMSClientException("Timeout has occurred on a socket read or accept", e, 500);
		} catch (URIException e) {
			throw new DMSClientException("URI parsing and escape encoding exception", e, 500);
		} catch (HttpException e) {
			throw new DMSClientException("HTTP or HttpClient exception has occurred", e, 500);
		} catch (ConnectException e) {
			throw new DMSClientException("The connection was refused remotely", e, 500);
		} catch (UnknownHostException e) {
			throw new DMSClientException("IP address of a host could not be determined", e, 500);
		} catch (SocketException e) {
			throw new DMSClientException("Error in the underlying protocol", e, 500);
		} catch (IOException e) {
			throw new DMSClientException(AlfrescoErrorReader.parse(e), 500);
		} catch (DMSClientException e) {
			throw e;
		}
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
	@Override
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
	@Override
	public HttpMethod createMethod(HttpMethod method, String content, boolean authentication)
			throws UnsupportedEncodingException {

		method.setParams(new HttpMethodParams());
		if (method instanceof EntityEnclosingMethod) {
			EntityEnclosingMethod enclosingMethod = (EntityEnclosingMethod) method;
			enclosingMethod.setRequestEntity(new StringRequestEntity(content,
					MIME_TYPE_APPLICATION_JSON, UTF_8));
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
	 * Constructs and URI to the given relative url. Service should be initialized in prior with
	 * correct remote host and port.
	 * 
	 * @param relativeURL
	 *            is the uri of rest service on the remote machine
	 * @param useServicePath
	 *            is whether to add the service path or it is other uri
	 * @return the builded uri
	 * @throws URIException
	 *             on any error during build
	 */
	private URI buildFullURL(final String relativeURL, boolean useServicePath) throws URIException {
		if (relativeURL == null) {
			throw new URIException("Null relative url privded!");
		}
		String localUri = relativeURL;
		if (!localUri.startsWith(SERVICE_BASE_URI) && useServicePath) {
			if (!localUri.startsWith("/")) {
				localUri = "/" + localUri;
			}
			localUri = SERVICE_BASE_URI + localUri;
		}

		URI fullURI = new URI(getProtocol(), null, getHost(), getPort(), localUri);
		return fullURI;
	}

	@Override
	public URI buildFullURL(final String relativeURL) throws URIException {
		return buildFullURL(relativeURL, isUseServicePath());
	}

	/**
	 * Retrieves the contents of the given {@link InputStream} as {@link String} .
	 * 
	 * @param method
	 *            is the method to read stream to read.
	 * @return is the stream contents or <code>null</code> if error occure during reading.
	 * @throws DMSClientException
	 */
	private String getResponse(HttpMethod method) throws DMSClientException {
		try (InputStream inputStream = method.getResponseBodyAsStream();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inputStream, UTF_8))) {
			// default StringWriter capacity is 16
			int capacity = 16;
			if (inputStream.available() != -1) {
				capacity = inputStream.available();
			}
			try (StringWriter writer = new StringWriter(capacity)) {
				String line;
				while ((line = reader.readLine()) != null) {
					writer.write(line);
				}
				return writer.toString();
			}
		} catch (IOException e) {
			throw new DMSClientException(AlfrescoErrorReader.parse(e), 500);
		}
	}

	/**
	 * Gets the authentication.
	 * 
	 * @return the authentication
	 */
	protected String[] getAuthentication() {
		User authentication = SecurityContextManager.getRunAsAuthentication();
		if (authentication instanceof UserWithCredentials) {
			UserWithCredentials credentials = (UserWithCredentials) authentication;
			if (StringUtils.isNotNullOrEmpty(credentials.getName())) {
				String[] result = new String[3];
				result[0] = credentials.getName();
				if (StringUtils.isNotNullOrEmpty((String) credentials.getCredentials())) {
					result[1] = (String) credentials.getCredentials();
				}
				if (StringUtils.isNotNullOrEmpty(credentials.getTicket())) {
					SecurityContextManager.updateUserToken(credentials);
					result[2] = credentials.getTicket();
				}
				return result;
			}
		} else {
			authentication = SecurityContextManager.getFullAuthentication();
			if (authentication instanceof UserWithCredentials) {
				UserWithCredentials credentials = (UserWithCredentials) authentication;
				if (StringUtils.isNotNullOrEmpty(credentials.getName())) {
					String[] result = new String[3];
					result[0] = credentials.getName();
					if (StringUtils.isNotNullOrEmpty((String) credentials.getCredentials())) {
						result[1] = (String) credentials.getCredentials();
					}
					if (StringUtils.isNotNullOrEmpty(credentials.getTicket())) {
						SecurityContextManager.updateUserToken(credentials);
						result[2] = credentials.getTicket();
					}
					return result;
				}
			}
		}
		throw new IllegalStateException(
				"Must provide username and password for authenticating in the DMS");
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
			header = new Header(RH_PARAM_ACCEPT, header.getValue() + "; "
					+ MIME_TYPE_APPLICATION_JSON);
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
			Header nHeader = new Header(HEADER_COOKIE, header.getValue() + "; " + PROP_JSESSIONID
					+ "=" + sessionId);
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
	 * @param ticket
	 *            the ticket for standalone invocation
	 * @see #setServerLocation(String, int)
	 * @see #setCredentials(AuthScope, Credentials, HttpState)
	 */
	public void setDefaultCredentials(String host, int port, String user, String password,
			String ticket) {
		setDefaultCredentials(host, port, user, password);
		this.ticket = ticket;
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
			setCredentials(new AuthScope(getHost(), getPort()), new UsernamePasswordCredentials(
					user, password), new HttpState());
			credentialsSet = true;
		}
	}

	/**
	 * Initialize the given {@link HttpState} with the given {@link AuthScope} and
	 * {@link Credentials}. Saves the state into the current instance.
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
		AlfrescoRESTClient.defaultTimeout = defaultTimeout;
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
	public static String getProtocol() {
		return "http";
	}

	/**
	 * Prints logging debug msg.
	 * 
	 * @param msg
	 *            the message to print
	 */
	private void debug(Object... msg) {
		if (DEBUG_ENABLED) {
			if (msg != null) {
				StringBuffer buffer = new StringBuffer();
				for (Object object : msg) {
					buffer.append(object);
				}
				LOGGER.debug(buffer.toString());
			}
		}
	}

	/**
	 * Prints logging trace msg.
	 * 
	 * @param msg
	 *            the message to print
	 */
	private void trace(Object... msg) {
		if (TRACE_ENABLED) {
			if (msg != null) {
				StringBuffer buffer = new StringBuffer();
				for (Object object : msg) {
					buffer.append(object);
				}
				LOGGER.trace(buffer.toString());
			}
		}
	}

	/**
	 * Getter method for useServicePath.
	 * 
	 * @return the useServicePath
	 */
	public boolean isUseServicePath() {
		return useServicePath;
	}

	/**
	 * Setter method for useServicePath.
	 * 
	 * @param useServicePath
	 *            the useServicePath to set
	 */
	public void setUseServicePath(boolean useServicePath) {
		this.useServicePath = useServicePath;
	}

}
