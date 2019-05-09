package com.sirma.itt.seip.rest.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.testutil.rest.HttpClientTestUtils;

/**
 * Test for {@link HttpClientUtil}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class HttpClientUtilTest {

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
		Map<String, HttpRequestHandler> handlers = new HashMap<>(3);
		handlers.put("/get/connection-refused", HttpClientTestUtils.buildConnectionRefusedResponse());
		handlers.put("/get/bad-request", HttpClientTestUtils.buildBadRequestResponse());
		handlers.put("/get/successful", buildSuccessfulResponse());
		return handlers;
	}

	private static HttpRequestHandler buildSuccessfulResponse() throws HttpException, IOException {
		HttpRequestHandler successful = mock(HttpRequestHandler.class);
		doAnswer(a -> {
			HttpResponse response = a.getArgumentAt(1, HttpResponse.class);
			response.setStatusCode(Status.OK.getStatusCode());
			BasicHttpEntity httpEntity = new BasicHttpEntity();
			httpEntity.setContent(new ByteArrayInputStream("stream".getBytes()));
			response.setEntity(httpEntity);
			return response;
		}).when(successful).handle(any(), any(), any());
		return successful;
	}

	@Test(expected = NullPointerException.class)
	public void callRemoteService_nullRequest() {
		HttpClientUtil.callRemoteService(null);
	}

	@Test(expected = RuntimeException.class)
	public void callRemoteService_connectionRefused() {
		URI uri = HttpClientTestUtils.buildURI(server, "/get/connection-refused");
		HttpGet get = new HttpGet(uri);
		HttpClientUtil.callRemoteService(get);
	}

	@Test(expected = RuntimeException.class)
	public void callRemoteService_badRequest() {
		URI uri = HttpClientTestUtils.buildURI(server, "/get/bad-request");
		HttpGet get = new HttpGet(uri);
		HttpClientUtil.callRemoteService(get);
	}

	@Test
	public void callRemoteService_OK() throws IOException {
		URI uri = HttpClientTestUtils.buildURI(server, "/get/successful");
		HttpGet get = new HttpGet(uri);
		FileDescriptor descriptor = HttpClientUtil.callRemoteService(get);
		assertNotNull(descriptor);
		assertEquals("stream", descriptor.asString());
	}

	@Test
	public void callRemoteServiceLazy_OK() throws IOException {
		URI uri = HttpClientTestUtils.buildURI(server, "/get/successful");
		HttpGet get = new HttpGet(uri);
		FileDescriptor descriptor = HttpClientUtil.callRemoteServiceLazily(get);
		assertNotNull(descriptor);
		assertEquals("stream", descriptor.asString());
	}

}
