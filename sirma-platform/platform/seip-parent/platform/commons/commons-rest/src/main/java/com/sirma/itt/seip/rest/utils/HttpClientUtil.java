package com.sirma.itt.seip.rest.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.sirma.itt.seip.io.ChainClosingInputStream;
import com.sirma.itt.seip.io.FileDescriptor;

/**
 * Utility containing common logic for retrieving files by remote HTTP request.Uses <code>ChainClosingInputStream</code>
 * to build <code>FileDescriptor</code>, also handles connection closing in cases of request failing.
 *
 * @author Borislav Bonev
 */
public class HttpClientUtil {

	/**
	 * Calls remote service with the given request. If the request execution is successful, from the response stream
	 * will be build and returned<code>FileDescriptor</code>. In case of any errors, while execution or response
	 * processing, all connections will be handled and closed properly.
	 *
	 * @param request
	 *            the request that should be executed
	 * @return new <code>FileDescriptor</code> containing the response stream from the request
	 * @throws RuntimeException
	 *             when the response code from the request is different then <code>200</code> or any error occurs while
	 *             request execution or response processing
	 * @see ChainClosingInputStream
	 * @see CloseableHttpClient
	 * @see CloseableHttpResponse
	 */
	@SuppressWarnings("resource")
	public static FileDescriptor callRemoteService(HttpUriRequest request) {
		Objects.requireNonNull(request, "Request is required!");
		CloseableHttpClient client = HttpClientBuilder.create().build();
		CloseableHttpResponse response = execute(request, client);

		Closeable closeConnections = () -> {
			try {
				client.close();
			} finally {
				response.close();
			}
		};

		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		if (Status.OK.getStatusCode() != statusCode) {
			IOUtils.closeQuietly(closeConnections);
			throw new RuntimeException("Failed to retrieve from - " + request.getURI() + ". Service response - status: "
					+ statusCode + ", reason: " + statusLine.getReasonPhrase());
		}

		try {
			InputStream stream = new ChainClosingInputStream(response.getEntity().getContent(), closeConnections);
			return FileDescriptor.create(() -> stream, -1);
		} catch (IOException e) {
			IOUtils.closeQuietly(closeConnections);
			throw new RuntimeException(e);
		}
	}

	private static CloseableHttpResponse execute(HttpUriRequest request, CloseableHttpClient client) {
		try {
			return client.execute(request);
		} catch (IOException e) {
			IOUtils.closeQuietly(client);
			throw new RuntimeException(e);
		}
	}

}
