package com.sirma.itt.seip.eai.service.communication;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;

import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Test {@link RemoteCommunicationConfiguration}
 * 
 * @author gshevkedov
 * @author bbanchev
 */
public class RemoteCommunicationConfigurationTest {
	private JSONObject services = JsonUtil.createObjectFromString(
			"{\"search\":{\"GET\":{\"uri\":\"service/media/images.json\", \"timeout\":72000}},\"search2\":{\"GET\":{\"uri\":\"/service/media/images2.json\", \"timeout\":72000}},\"retrieve\":{\"GET\":{\"uri\":\"service/media/{0}/images/{1}.json\", \"timeout\":72000}},\"content\":{\"GET\":{\"uri\":\"service/media/{0}/images/{1}\", \"timeout\":72000}},\"logging\":{\"POST\":{\"uri\":\"service/log\", \"timeout\":72000}}}");

	@Test
	public void testAddHttpHeader() throws URISyntaxException {
		URI baseURI = new URI("cultObj");
		RemoteCommunicationConfiguration communicationConfig = new RemoteCommunicationConfiguration(baseURI);
		String key = "1";
		String value = "test";
		communicationConfig.addHttpHeader(key, value);
		assertEquals(1, communicationConfig.getRequestHeaders().size());
	}

	@Test
	public void tesGetRequestServiceURI() throws Exception {
		URI baseURI = new URI("http://localhost:8080/eai");
		RemoteCommunicationConfiguration communicationConfig = new RemoteCommunicationConfiguration(baseURI);
		communicationConfig.addServiceEndpoint(
				new ServiceEndpoint(BaseEAIServices.SEARCH, JsonUtil.toMap(services.getJSONObject("search"))));
		URIBuilder requestServiceURI = communicationConfig.getRequestServiceURI(BaseEAIServices.SEARCH);
		assertEquals("http://localhost:8080/eai/service/media/images.json", requestServiceURI.build().toString());
		communicationConfig.addServiceEndpoint(
				new ServiceEndpoint(BaseEAIServices.SEARCH, JsonUtil.toMap(services.getJSONObject("search2"))));
		requestServiceURI = communicationConfig.getRequestServiceURI(BaseEAIServices.SEARCH);
		assertEquals("http://localhost:8080/eai/service/media/images2.json", requestServiceURI.build().toString());

		baseURI = new URI("http://localhost:8080/eai/");
		communicationConfig = new RemoteCommunicationConfiguration(baseURI);
		communicationConfig.addServiceEndpoint(
				new ServiceEndpoint(BaseEAIServices.SEARCH, JsonUtil.toMap(services.getJSONObject("search"))));
		requestServiceURI = communicationConfig.getRequestServiceURI(BaseEAIServices.SEARCH);
		assertEquals("http://localhost:8080/eai/service/media/images.json", requestServiceURI.build().toString());
		communicationConfig.addServiceEndpoint(
				new ServiceEndpoint(BaseEAIServices.SEARCH, JsonUtil.toMap(services.getJSONObject("search2"))));
		requestServiceURI = communicationConfig.getRequestServiceURI(BaseEAIServices.SEARCH);
		assertEquals("http://localhost:8080/eai/service/media/images2.json", requestServiceURI.build().toString());
	}

	@Test
	public void tesAddServiceEndpoint() throws URISyntaxException, JSONException {
		URI baseURI = new URI("http://localhost:8080/eai");
		RemoteCommunicationConfiguration communicationConfig = new RemoteCommunicationConfiguration(baseURI);
		communicationConfig.addServiceEndpoint(
				new ServiceEndpoint(BaseEAIServices.SEARCH, JsonUtil.toMap(services.getJSONObject("search"))));
		assertEquals(1, communicationConfig.getServiceEndpoints().size());
		communicationConfig.addServiceEndpoint(
				new ServiceEndpoint(BaseEAIServices.SEARCH, JsonUtil.toMap(services.getJSONObject("search2"))));
		assertEquals(1, communicationConfig.getServiceEndpoints().size());
		communicationConfig.addServiceEndpoint(new ServiceEndpoint(new EAIServiceIdentifier() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getServiceId() {
				return "search2";
			}
		}, JsonUtil.toMap(services.getJSONObject("search2"))));

		communicationConfig.seal();
		communicationConfig.addServiceEndpoint(
				new ServiceEndpoint(BaseEAIServices.RETRIEVE, JsonUtil.toMap(services.getJSONObject("retrieve"))));
		assertEquals(2, communicationConfig.getServiceEndpoints().size());
	}

	@Test(expected = EAIException.class)
	public void testGetRequestServiceURIThows() throws EAIException, URISyntaxException {
		URI baseURI = new URI("cultObj");
		RemoteCommunicationConfiguration communicationConfig = new RemoteCommunicationConfiguration(baseURI);
		EAIServiceIdentifier id = Mockito.mock(EAIServiceIdentifier.class);
		URIBuilder uriBuilder = Mockito.mock(URIBuilder.class);
		Mockito.when(communicationConfig.getRequestServiceURI(id)).thenReturn(uriBuilder);
	}

	@Test(expected = EAIException.class)
	public void testGetRequestServiceURIWithTwoParams() throws URISyntaxException, EAIException {
		URI baseURI = new URI("cultObj");
		RemoteCommunicationConfiguration communicationConfig = new RemoteCommunicationConfiguration(baseURI);
		EAIServiceIdentifier id = Mockito.mock(EAIServiceIdentifier.class);
		URIBuilder uriBuilder = Mockito.mock(URIBuilder.class);
		String httpMethod = "GET";
		Mockito.when(communicationConfig.getRequestServiceURI(id, httpMethod)).thenReturn(uriBuilder);
	}
}
