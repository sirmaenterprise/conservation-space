package com.sirma.itt.seip.testutil.rest;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpRequestHandler;

/**
 * Utility class for common logic used in unit tests that require mocked HTTP server.
 *
 * @author A. Kunchev
 */
public class HttpClientTestUtils {

	/**
	 * Creates new test server. The server is started on address 'localhost' and the port is generated between 10000 and
	 * 11000. The input map is used to set test endpoints and their handlers. The handlers are registered before the
	 * actual server create. <br>
	 * This method will no start the server!
	 *
	 * @param handlers
	 *            {@link Map} containing {@link HttpRequestHandler} for specific request paths. Every {@link Entry}
	 *            represents specific path bind to specific handler
	 * @return {@link HttpServer} with registered handlers for specific endpoints
	 * @see RandomPortGenerator
	 */
	public static HttpServer createTestServer(Map<String, HttpRequestHandler> handlers) {
		Objects.requireNonNull(handlers, "The input map is required!");
		InetSocketAddress address = new InetSocketAddress("localhost", RandomPortGenerator.generatePort(10000, 11000));
		ServerBootstrap bootstrap = ServerBootstrap.bootstrap().setLocalAddress(address.getAddress());
		handlers.forEach(bootstrap::registerHandler);
		return bootstrap.create();
	}

	/**
	 * Builds handlers which will cause connection refuse by throwing {@link IOException}, when
	 * {@link HttpRequestHandler#handle} is called.
	 *
	 * @return request handler that will throw {@link IOException} when it handle method is called
	 */
	public static HttpRequestHandler buildConnectionRefusedResponse() throws HttpException, IOException {
		HttpRequestHandler connectionRefused = mock(HttpRequestHandler.class);
		doThrow(new IOException()).when(connectionRefused).handle(any(), any(), any());
		return connectionRefused;
	}

	/**
	 * Builds handlers which response code will be 400.
	 *
	 * @return request handler which response will be {@link Status#BAD_REQUEST}
	 */
	public static HttpRequestHandler buildBadRequestResponse() throws HttpException, IOException {
		HttpRequestHandler badRequest = mock(HttpRequestHandler.class);
		doAnswer(a -> {
			HttpResponse response = a.getArgumentAt(1, HttpResponse.class);
			response.setStatusCode(Status.BAD_REQUEST.getStatusCode());
			return response;
		}).when(badRequest).handle(any(), any(), any());
		return badRequest;
	}

	/***
	 * The {@link URI} that is build is based on the given {@link HttpServer} host address and port. For protocol is
	 * used 'http'. The path after the base address could be configured
	 *
	 * @param server
	 *            from which will be retrieved the address and the port for the generated URI
	 * @param path
	 *            the path that should be appended after the address and the port
	 * @return new URI based on the given server address and path
	 * @see URI
	 */
	public static URI buildURI(HttpServer server, String path) {
		Objects.requireNonNull(server, "Server is required!");
		try {
			String address = server.getInetAddress().getHostAddress();
			int port = server.getLocalPort();
			return new URI("http", null, address, port, path, null, null);
		} catch (URISyntaxException e) {
			throw new AssertionError("Error while building test URI.", e);
		}
	}

	private HttpClientTestUtils() {
		// utility class
	}
}
