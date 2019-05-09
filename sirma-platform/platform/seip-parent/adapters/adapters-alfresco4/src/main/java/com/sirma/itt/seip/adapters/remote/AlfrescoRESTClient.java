/**
 *
 */
package com.sirma.itt.seip.adapters.remote;

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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.adapters.AdaptersConfiguration;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.resources.security.SecurityTokenService;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.exception.AuthenticationException;
import com.sirma.itt.seip.security.util.SecurityUtil;

/**
 * Base class for calling web scripts in alfresco. <br>
 *
 * @author Borislav Bonev
 */
@ApplicationScoped
public class AlfrescoRESTClient implements RESTClient {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RESTClient.class);

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8168400014809201723L;
	/** the key for saml token. */
	public static final String SAML_TOKEN = "SAMLToken";

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

	@Inject
	private SecurityContext securityContext;
	@Inject
	private SecurityConfiguration securityConfiguration;
	@Inject
	private AdaptersConfiguration configuration;

	@Inject
	private SecurityTokenService tokenService;

	private Map<String, String> tokensCache = new ConcurrentHashMap<>();

	private transient Supplier<HttpClient> httpClientSupplier = HttpClient::new;

	/*
	 * stores DMS sessions per tenant. It will force alfresco to reuse sessions
	 */
	private static final Map<String, String> SESSIONS = new HashMap<>(32);

	/** The default timeout. */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "dms.remote.request.timeout", defaultValue = "0", sensitive = true, type = Integer.class)
	private ConfigurationProperty<Integer> timeout;

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
		LOGGER.trace("Received response  : {}", response);
		return response;
	}

	@Override
	public HttpMethod rawRequest(HttpMethod method, String uri) throws DMSClientException {
		try {
			return rawRequest(method, buildFullURL(uri, true));
		} catch (URIException e) {
			handleException(e);
			// comes here only if the method above could not handle it
			throw new DMSClientException("Unknown communication error", e, 500);
		}
	}

	@Override
	public HttpMethod rawRequest(HttpMethod method, URI uri) throws DMSClientException {
		try {
			HttpClient client = getClient();

			setAuthentication(uri, method);

			method.setURI(uri);

			setTimeout(method, getTimeout());
			method.setDoAuthentication(true);
			// set session id if exists this will force alfresco to reuse
			// sessions
			setSessionId(method, securityContext);

			client.executeMethod(method);

			saveSessionId(method, securityContext);
			// get the stream result - probably instanceof AutoCloseStream
			int statusCode = method.getStatusCode();
			LOGGER.trace("Received response with status code: {} @ {}", statusCode, uri);
			if (statusCode == 200) {
				return method;
			}
			throw new DMSClientException(AlfrescoErrorReader.parse(getResponse(method)), statusCode);
		} catch (DMSClientException e) {
			throw e;
		} catch (Exception e) {
			handleException(e);
			// comes here only if the method above could not handle it
			throw new DMSClientException("Unknown communication error", e, 500);
		}
	}

	private static void handleException(Exception e) throws DMSClientException {
		String message = null;
		if (e instanceof SocketTimeoutException) {
			message = "Timeout has occurred on a socket read or accept";
		} else if (e instanceof URIException) {
			message = "URI parsing and escape encoding exception";
		} else if (e instanceof HttpException) {
			message = "HTTP or HttpClient exception has occurred";
		} else if (e instanceof ConnectException) {
			message = "The connection was refused remotely";
		} else if (e instanceof UnknownHostException) {
			message = "IP address of a host could not be determined";
		} else if (e instanceof SocketException) {
			message = "Error in the underlying protocol";
		} else if (e instanceof IOException) {
			message = AlfrescoErrorReader.parse(e);
		}
		if (message != null) {
			throw new DMSClientException(message, e, 500);
		}
	}

	private static void setTimeout(HttpMethod method, int timeout) {
		if (timeout > 0) {
			method.getParams().setIntParameter(HTTP_SOCKET_TIMEOUT, timeout);
		}
	}

	/**
	 * Sets HTTP Basic authentication for the given HttpMethod by adding authorization header. Uses tenant admin
	 * credentials.
	 *
	 * @param uri the remote uri
	 * @param method the http method in which to set authorization
	 */
	private void setAuthentication(Object uri, HttpMethod method) {
		String username = getAdminUserName();
		String token = tokensCache.computeIfAbsent(username, user -> {
			try {
				String samlToken = tokenService.requestToken(username, getAdminPassword());
				byte[] encryptedToken = SecurityUtil
						.encrypt(Base64.decodeBase64(samlToken), securityConfiguration.getCryptoKey().get());
				return new String(Base64.encodeBase64(encryptedToken), StandardCharsets.UTF_8);
			} catch (Exception e) {
				throw new AuthenticationException(username, "Security token request failed", e);
			}
		});

		method.addRequestHeader(SAML_TOKEN, token);
		LOGGER.trace("INVOKING WEBSCRIPT @ {} user: {}", uri, username);
	}

	private String getAdminUserName() {
		if (securityContext.isSystemTenant()) {
			return securityConfiguration.getSystemAdminUsername();
		}
		return securityConfiguration.getAdminUserName().get();
	}

	private String getAdminPassword() {
		return securityConfiguration.getAdminUserPassword().get();
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
	 */
	@Override
	public HttpMethod createMethod(HttpMethod method, Part[] parts, boolean authentication) {
		HttpMethodParams params = new HttpMethodParams();
		if (method instanceof EntityEnclosingMethod) {
			params.setHttpElementCharset(UTF_8);
			EntityEnclosingMethod enclosingMethod = (EntityEnclosingMethod) method;
			enclosingMethod.setRequestEntity(new MultipartRequestEntity(parts, params));
		}
		method.addRequestHeader(PROP_CONTENT_ENCODING, COMPRESSION_FORMAT_GZIP);
		method.addRequestHeader(PROP_ACCEPT_ENCODING, COMPRESSION_FORMAT_GZIP);
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
	 */
	@Override
	public HttpMethod createMethod(HttpMethod method, String content, boolean authentication)
			throws UnsupportedEncodingException {

		method.setParams(new HttpMethodParams());
		if (method instanceof EntityEnclosingMethod) {
			EntityEnclosingMethod enclosingMethod = (EntityEnclosingMethod) method;
			enclosingMethod.setRequestEntity(new StringRequestEntity(content, MIME_TYPE_APPLICATION_JSON, UTF_8));
		}
		method.addRequestHeader(PROP_CONTENT_ENCODING, COMPRESSION_FORMAT_GZIP);
		method.addRequestHeader(PROP_ACCEPT_ENCODING, COMPRESSION_FORMAT_GZIP);
		return method;
	}

	/**
	 * Constructs and URI to the given relative url. Service should be initialized in prior with correct remote host and
	 * port.
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

		java.net.URI uri = configuration.getDmsAddress().get();
		return new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), localUri);
	}

	@Override
	public URI buildFullURL(final String relativeURL) throws URIException {
		return buildFullURL(relativeURL, true);
	}

	/**
	 * Retrieves the contents of the given {@link InputStream} as {@link String} .
	 *
	 * @param method
	 *            is the method to read stream to read.
	 * @return is the stream contents or <code>null</code> if error occure during reading.
	 * @throws DMSClientException on IO error
	 */
	public static String getResponse(HttpMethod method) throws DMSClientException {
		try (InputStream inputStream = method.getResponseBodyAsStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, UTF_8))) {
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
	 * Adds an Accept request header for JSON format.
	 *
	 * @param method
	 *            is the method to update
	 * @return is the updated method
	 */
	public static HttpMethod setAcceptJson(HttpMethod method) {
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
	 * @param security
	 *            is the security context
	 */
	private static void setSessionId(HttpMethod method, SecurityContext security) {
		String sessionId = SESSIONS.get(security.getCurrentTenantId());
		if (sessionId == null) {
			// no session id, yet
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
	private static void saveSessionId(HttpMethod method, SecurityContext security) {
		Header header = method.getResponseHeader(HEADER_SET_COOKIE);
		if (header != null) {
			HeaderElement[] elements = header.getElements();
			for (HeaderElement element : elements) {
				if (PROP_JSESSIONID.equals(element.getName())) {
					storeSessionId(security, element.getValue());
					return;
				}
			}
		}
	}

	private static void storeSessionId(SecurityContext security, String sessionId) {
		String currentTenantId = security.getCurrentTenantId();
		synchronized (SESSIONS) {
			if (SESSIONS.containsKey(currentTenantId)) {
				return;
			}
			SESSIONS.put(currentTenantId, sessionId);
		}
	}

	/**
	 * Gets the client.
	 *
	 * @return the client
	 */
	private HttpClient getClient() {
		return httpClientSupplier.get();
	}

	/**
	 * Setter method for httpClientSupplier.
	 *
	 * @param httpClientSupplier
	 *            the httpClientSupplier to set
	 */
	void setClient(Supplier<HttpClient> httpClientSupplier) {
		this.httpClientSupplier = httpClientSupplier;
	}

	/**
	 * Getter method for host.
	 *
	 * @return the host
	 */
	public String getHost() {
		return configuration.getDmsAddress().get().getHost();
	}

	/**
	 * Getter method for port.
	 *
	 * @return the port
	 */
	public int getPort() {
		return configuration.getDmsAddress().get().getPort();
	}

	/**
	 * Getter method for timeout.
	 *
	 * @return the timeout
	 */
	public int getTimeout() {
		return timeout.get();
	}

	/**
	 * Getter method for protocol.
	 *
	 * @return the protocol
	 */
	public String getProtocol() {
		return configuration.getDmsAddress().get().getScheme();
	}

}
