package com.sirma.itt.seip.eai.service.communication.impl;

import static com.sirma.itt.seip.eai.mock.MockProvider.mockSystem;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.context.ContextualReference;
import com.sirma.itt.seip.eai.configuration.EAIConfigurationProvider;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.model.ServiceResponse;
import com.sirma.itt.seip.eai.model.communication.RequestInfo;
import com.sirma.itt.seip.eai.service.EAIConfigurationService;
import com.sirma.itt.seip.eai.service.communication.BaseEAIServices;
import com.sirma.itt.seip.eai.service.communication.CommunicationConfiguration;
import com.sirma.itt.seip.eai.service.communication.RemoteCommunicationConfiguration;
import com.sirma.itt.seip.eai.service.communication.ServiceEndpoint;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.util.ReflectionUtils;

public class HttpClientCommunicationServiceAdapterTest {

	private Contextual<HttpClientBuilder> clients = new ContextualReference<>();
	@Mock
	private EAIConfigurationService integrationService;

	private HttpClientCommunicationServiceAdapter httpClientCommunicationServiceAdapter;
	private RemoteCommunicationConfiguration communicationConfig;
	private Map<String, Serializable> endpointConfig;

	@Before
	public void setUp() throws URISyntaxException {
		MockitoAnnotations.initMocks(this);
		EAIConfigurationProvider configProvider = mockSystem("TEST", Boolean.TRUE, Boolean.TRUE);
		when(integrationService.getIntegrationConfiguration(eq("TEST"))).thenReturn(configProvider);
		when(integrationService.resolveIntegrationConfiguration(eq("TEST"))).thenReturn(Optional.of(configProvider));
		httpClientCommunicationServiceAdapter = new HttpClientCommunicationServiceAdapter() {

			@Override
			public String getName() {
				return "TEST";
			}

			@Override
			public ServiceResponse invoke(RequestInfo request) throws EAIException {
				return Mockito.mock(ServiceResponse.class);
			}
		};
		ReflectionUtils.setFieldValue(httpClientCommunicationServiceAdapter, "clients", clients);
		ReflectionUtils.setFieldValue(httpClientCommunicationServiceAdapter, "integrationService", integrationService);
		URI baseURI = new URI("http://localhost:8080/eai");
		communicationConfig = new RemoteCommunicationConfiguration(baseURI);
		Map<String, Map<String, Serializable>> data = new HashMap<>();
		endpointConfig = new HashMap<>();
		endpointConfig.put("uri", "/myuri/{0}/{1}");
		endpointConfig.put("timeout", Integer.valueOf(1000));
		data.put("GET", endpointConfig);
		communicationConfig.addServiceEndpoint(new ServiceEndpoint(BaseEAIServices.SEARCH, data));

		Map<String, Map<String, Serializable>> dataPost = new HashMap<>();
		Map<String, Serializable> endpointConfigPost = new HashMap<>();
		endpointConfigPost.put("uri", "/myuripost");
		endpointConfigPost.put("timeout", Integer.valueOf(1000));
		dataPost.put("POST", endpointConfigPost);
		communicationConfig.addServiceEndpoint(new ServiceEndpoint(BaseEAIServices.RETRIEVE, dataPost));

		Mockito.when(configProvider.getCommunicationConfiguration()).thenReturn(
				new ConfigurationPropertyMock<CommunicationConfiguration>(communicationConfig));
	}

	@Test
	public void testProvideHttpClient() throws Exception {
		setUp();
		httpClientCommunicationServiceAdapter.initialize();
		HttpClient provideHttpClient = httpClientCommunicationServiceAdapter.provideHttpClient();
		Assert.assertNotNull(provideHttpClient);
	}

	@Test
	public void testCreateGetMethod() throws Exception {

		HttpGet method = httpClientCommunicationServiceAdapter.createGetMethod(BaseEAIServices.SEARCH, "p1", "p2",
				"p3");

		Assert.assertEquals("http://localhost:8080/eai/myuri/p1/p2", method.getURI().toString());

		endpointConfig.put("uri", "/myuri");
		List<NameValuePair> params = new LinkedList<>();
		params.add(new BasicNameValuePair("testparam", "paramvalue"));
		params.add(new BasicNameValuePair("testparam2", "paramvalue2"));
		method = httpClientCommunicationServiceAdapter.createGetMethod(BaseEAIServices.SEARCH, params);
		Assert.assertEquals("http://localhost:8080/eai/myuri?testparam=paramvalue&testparam2=paramvalue2",
				method.getURI().toString());

	}

	@Test
	public void testCreatePostMethod() throws Exception {
		HttpPost method = httpClientCommunicationServiceAdapter.createPostMethod(BaseEAIServices.RETRIEVE);
		Assert.assertNotNull(method);
		Assert.assertEquals("http://localhost:8080/eai/myuripost", method.getURI().toString());
	}

	@Test
	public void testPrepareHttpMethod() throws Exception {
		HttpGet method = httpClientCommunicationServiceAdapter.prepareHttpMethod(BaseEAIServices.SEARCH, new HttpGet(),
				null, "p1", "p2");
		Assert.assertEquals("http://localhost:8080/eai/myuri/p1/p2", method.getURI().toString());
		Assert.assertEquals(1000, method.getConfig().getSocketTimeout());

		List<NameValuePair> params = new LinkedList<>();
		params.add(new BasicNameValuePair("testparam", "paramvalue"));
		method = httpClientCommunicationServiceAdapter.prepareHttpMethod(BaseEAIServices.SEARCH, new HttpGet(), params,
				"p1", "p2");
		Assert.assertEquals("http://localhost:8080/eai/myuri/p1/p2?testparam=paramvalue", method.getURI().toString());
		Assert.assertEquals(1000, method.getConfig().getSocketTimeout());

		endpointConfig.put("uri", "/myuri");
		method = httpClientCommunicationServiceAdapter.prepareHttpMethod(BaseEAIServices.SEARCH, new HttpGet(), null);
		Assert.assertEquals("http://localhost:8080/eai/myuri", method.getURI().toString());
		Assert.assertEquals(1000, method.getConfig().getSocketTimeout());
	}

}
