package com.sirma.itt.seip.adapters.iiif.iip;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.adapters.iiif.Dimension;
import com.sirma.itt.seip.adapters.iip.IIPServerImageProvider;
import com.sirma.itt.seip.testutil.rest.HttpClientTestUtils;

import com.srima.itt.seip.adapters.mock.ImageServerConfigurationsMock;

/**
 * Tests for {@link IIPServerImageProvider}
 */
public class IIPServerImagerProviderTest {

	@InjectMocks
	private IIPServerImageProvider iipServerImageProvider;

	@Spy
	private ImageServerConfigurationsMock imageServerConfigurations;

	private static HttpServer server;

	@BeforeClass
	public static void startTestServer() throws HttpException, IOException {
		server = HttpClientTestUtils.createTestServer(stubResponses());
		server.start();
	}

	@AfterClass
	public static void shutdownTestServer() {
		if (server != null) {
			server.shutdown(1, TimeUnit.SECONDS);
		}
	}

	private static Map<String, HttpRequestHandler> stubResponses() throws HttpException, IOException {
		Map<String, HttpRequestHandler> handlers = new HashMap<>();
		handlers.put("/testGetImageFull/iip-server", buildOkResponse("emf:dummy/full/full/0/default.jpg"));
		handlers.put("/testGetImageWithDimension/iip-server", buildOkResponse("emf:dummy/full/100,100/0/default.jpg"));
		handlers.put("/testGetImageWithDimensionAndBestFit/iip-server", buildOkResponse("emf:dummy/full/!100,100/0/default.jpg"));
		handlers.put("/testGetImageWithDimensionAndNullHeight/iip-server",
				buildOkResponse("emf:dummy/full/100,/0/default.jpg"));
		handlers.put("/testGetImageWithDimensionAndNullWidth/iip-server",
				buildOkResponse("emf:dummy/full/,100/0/default.jpg"));
		return handlers;
	}

	private static HttpRequestHandler buildOkResponse(String validPath) throws HttpException, IOException {
		HttpRequestHandler requestHandler = mock(HttpRequestHandler.class);

		doAnswer(invocation -> {
			HttpRequest request = invocation.getArgumentAt(0, HttpRequest.class);
			HttpResponse response = invocation.getArgumentAt(1, HttpResponse.class);

			String uri = request.getRequestLine().getUri();
			if (uri.contains(validPath)) {
				response.setStatusCode(Response.Status.OK.getStatusCode());
			} else {
				response.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			}

			return response;
		}).when(requestHandler).handle(any(), any(), any());

		return requestHandler;
	}

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetImageFull() {
		setIipServerAddress("/testGetImageFull/iip-server?IIIF=");

		InputStream image = iipServerImageProvider.getImage("emf:dummy");
		assertNotNull(image);
	}

	@Test
	public void testGetImageWithDimension() {
		setIipServerAddress("/testGetImageWithDimension/iip-server?IIIF=");

		Dimension<Integer> size = new Dimension<>(100, 100);
		InputStream image = iipServerImageProvider.getImage("emf:dummy", size, false);
		assertNotNull(image);
	}

	@Test
	public void testGetImageWithDimensionAndBestFit() {
		setIipServerAddress("/testGetImageWithDimensionAndBestFit/iip-server?IIIF=");

		Dimension<Integer> size = new Dimension<>(100, 100);
		InputStream image = iipServerImageProvider.getImage("emf:dummy", size, true);
		assertNotNull(image);
	}

	@Test
	public void testGetImageWithDimensionAndNullHeight() {
		setIipServerAddress("/testGetImageWithDimensionAndNullHeight/iip-server?IIIF=");

		Dimension<Integer> size = new Dimension<>(100, null);
		InputStream image = iipServerImageProvider.getImage("emf:dummy", size, false);
		assertNotNull(image);
	}

	@Test
	public void testGetImageWithDimensionAndNullWidth() {
		setIipServerAddress("/testGetImageWithDimensionAndNullWidth/iip-server?IIIF=");

		Dimension<Integer> size = new Dimension<>(null, 100);
		InputStream image = iipServerImageProvider.getImage("emf:dummy", size, true);
		assertNotNull(image);
	}

	private void setIipServerAddress(String address) {
		URI uri = HttpClientTestUtils.buildURI(server, "");
		imageServerConfigurations.setIiifServerAddress(uri.toString() + address);
	}

}
