package com.sirma.itt.seip.eai.service.communication.impl;

import java.lang.invoke.MethodHandles;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.eai.configuration.EAIConfigurationProvider;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.service.EAIConfigurationService;
import com.sirma.itt.seip.eai.service.communication.CommunicationConfiguration;
import com.sirma.itt.seip.eai.service.communication.EAICommunicationServiceAdapter;
import com.sirma.itt.seip.eai.service.communication.EAIServiceIdentifier;
import com.sirma.itt.seip.eai.service.communication.RemoteCommunicationConfiguration;

/**
 * Abstract adapter for http/s based requests. All common logic is placed here and optionally might be overridden
 *
 * @author bbanchev
 */
public abstract class HttpClientCommunicationServiceAdapter implements EAICommunicationServiceAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	protected EAIConfigurationService integrationService;

	@Inject
	protected Contextual<HttpClientBuilder> clients;

	@PostConstruct
	protected void initialize() {
		clients.initializeWith(this::getClientBuilder);
		integrationService
				.resolveIntegrationConfiguration(getName())
				.map(EAIConfigurationProvider::getCommunicationConfiguration)
				.ifPresent(config -> config.addConfigurationChangeListener(v -> clients.clearContextValue()));
	}

	/**
	 * Provide http client and cache it.
	 *
	 * @return the http client
	 */
	protected HttpClient provideHttpClient() {
		return clients.getContextValue().build();
	}

	/**
	 * Builds the reusable http client builder.
	 *
	 * @return the http client builder
	 */
	private HttpClientBuilder getClientBuilder() {
		RemoteCommunicationConfiguration communicationConfiguration = getCommunicationConfiguration();
		// here may add timeouts and protocol constraints.
		HttpClientBuilder httpClientBuilder = HttpClientBuilder
				.create()
					.setDefaultHeaders(getDefaultHeaders(communicationConfiguration));
		if (communicationConfiguration.isTrustAllCertificates()) {
			disableSSLCertificateValidation(httpClientBuilder);
		}
		return httpClientBuilder;
	}

	private static void disableSSLCertificateValidation(HttpClientBuilder httpClientBuilder) {
		try {
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				private X509Certificate[] accepted = {};

				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return accepted;
				}

				@Override
				public void checkClientTrusted(X509Certificate[] certs, String authType) {
					// skip
				}

				@Override
				public void checkServerTrusted(X509Certificate[] certs, String authType) {
					// skip
				}
			} };

			// Install the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sc,
					SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			httpClientBuilder.setSSLSocketFactory(sslSocketFactory);
		} catch (Exception e) {
			throw new EAIRuntimeException("Failed to prepare https client.", e);
		}
	}

	/**
	 * Creates a get method and sets the full uri based on the invoked service and the provided query parameters
	 * 
	 * @param serviceId
	 *            the service that to be invoked
	 * @param params
	 *            the query name value pair parameters
	 * @return the ready to execute get method
	 * @throws EAIException
	 *             on method constructions error
	 */
	protected HttpGet createGetMethod(EAIServiceIdentifier serviceId, List<NameValuePair> params) throws EAIException {
		return prepareHttpMethod(serviceId, new HttpGet(), params);
	}

	/**
	 * Creates a get method and sets the full uri based on the invoked service
	 * 
	 * @param serviceId
	 *            the service that to be invoked
	 * @param pathArgument
	 *            the path argument to substitute using '{0},{1},...' in the service URI
	 * @return the ready to execute get method
	 * @throws EAIException
	 *             on method constructions error
	 */
	protected HttpGet createGetMethod(EAIServiceIdentifier serviceId, String... pathArgument) throws EAIException {
		return prepareHttpMethod(serviceId, new HttpGet(), null, pathArgument);
	}

	/**
	 * Creates post method and sets the full uri based on the invoked service.
	 * 
	 * @param serviceId
	 *            the service that to be invoked
	 * @return the ready to execute post method
	 * @throws EAIException
	 *             on method constructions error
	 */
	protected HttpPost createPostMethod(EAIServiceIdentifier serviceId) throws EAIException {
		return prepareHttpMethod(serviceId, new HttpPost(), null);
	}

	/**
	 * Sets the uri, timeout and the other provided parameters to the provided method using the correct
	 * {@link CommunicationConfiguration} as base uri
	 *
	 * @param <T>
	 *            the generic type for method
	 * @param serviceId
	 *            the service id to get uri for
	 * @param method
	 *            the method to update uri for
	 * @param params
	 *            are the name-value params to append as query parameters of the uri. Might be null
	 * @param pathArgument
	 *            additional uri customization set by {@link MessageFormat} on the base uri path. Might be null
	 * @return the updated method with encoded URI
	 * @throws EAIException
	 *             on uri setup error or missing service configuration on invoke of
	 *             {@link RemoteCommunicationConfiguration#getRequestServiceURI(EAIServiceIdentifier, String)}
	 */
	protected <T extends HttpRequestBase> T prepareHttpMethod(EAIServiceIdentifier serviceId, T method,
			List<NameValuePair> params, String... pathArgument) throws EAIException {
		URIBuilder uriBuilder = null;
		try {
			RemoteCommunicationConfiguration communicationConfiguration = getCommunicationConfiguration();
			uriBuilder = communicationConfiguration.getRequestServiceURI(serviceId, method.getMethod());

			int timeout = communicationConfiguration
					.getServiceEndpoints()
						.get(serviceId)
						.getMethodTimeout(method.getMethod());
			Builder requestConfigBuilder;
			if (method.getConfig() != null) {
				requestConfigBuilder = RequestConfig.copy(method.getConfig());
			} else {
				requestConfigBuilder = RequestConfig.custom();
			}
			// set timeout
			requestConfigBuilder.setSocketTimeout(timeout);
			method.setConfig(requestConfigBuilder.build());

			if (pathArgument != null) {
				uriBuilder.setPath(MessageFormat.format(uriBuilder.getPath(), (Object[]) pathArgument));
			}
			if (params != null) {
				uriBuilder.addParameters(params);
			}
			method.setURI(uriBuilder.build());
			LOGGER.trace("Going to invoke {}@{} ", method.getMethod(), method.getURI());
			return method;
		} catch (EAIException e) {
			throw e;
		} catch (Exception e) {
			throw new EAIException("Failed to construct service method: '" + serviceId + "', uri: '" + uriBuilder
					+ "', path arguments: '" + (pathArgument != null ? Arrays.toString(pathArgument) : "null")
					+ " parameters: '" + params + "'", e);
		}
	}

	/**
	 * Gets the headers that would attached to each request.
	 *
	 * @param communicationConfiguration
	 *            the communication configuration for current system and tenant
	 * @return the headers as {@link Header} collection
	 */
	protected static Collection<Header> getDefaultHeaders(RemoteCommunicationConfiguration communicationConfiguration) {
		return communicationConfiguration
				.getRequestHeaders()
					.entrySet()
					.stream()
					.map(e -> new BasicHeader(e.getKey(), e.getValue()))
					.collect(Collectors.toList());
	}

	private RemoteCommunicationConfiguration getCommunicationConfiguration() {
		CommunicationConfiguration communicationConfiguration = integrationService
				.getIntegrationConfiguration(getName())
					.getCommunicationConfiguration()
					.get();
		if (!(communicationConfiguration instanceof RemoteCommunicationConfiguration)) {
			throw new EAIRuntimeException("Wrong configuration model provided for remote connections!");
		}
		return (RemoteCommunicationConfiguration) communicationConfiguration;
	}
}
