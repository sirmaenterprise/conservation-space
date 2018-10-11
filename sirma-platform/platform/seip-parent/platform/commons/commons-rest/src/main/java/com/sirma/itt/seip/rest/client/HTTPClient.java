package com.sirma.itt.seip.rest.client;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for apache httpclient library for invoking requests
 *
 * @author BBonev
 */
public class HTTPClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Execute an HTTP call using the provided information and response handler. <br>
	 * Note that this method will return {@code null} if there is a communication error, for better exception handling
	 * use the {@link #execute(HttpUriRequest, HttpClientContext, HttpHost, BiFunction, Function)}
	 *
	 * @param request
	 *            the request to execute
	 * @param context
	 *            the context to use for the execution, or {@code null} to use the default context
	 * @param targetHost
	 *            the target host for the request. Implementations may accept {@code null} if they can still determine a
	 *            route, for example to a default target or by inspecting the request.
	 * @param responseHandler
	 *            the response handler
	 * @return the value from the given response handler or {@code null} on client communication error.
	 */
	public <T> T execute(HttpUriRequest request, HttpClientContext context, HttpHost targetHost,
			BiFunction<Integer, HttpResponse, T> responseHandler) {
		return execute(request, context, targetHost, responseHandler, e -> {
			LOGGER.warn(e.getMessage(), e);
			return null;
		});
	}

	/**
	 * Execute an HTTP call using the provided information and response handler.
	 *
	 * @param request
	 *            the request to execute
	 * @param context
	 *            the context to use for the execution, or {@code null} to use the default context
	 * @param targetHost
	 *            the target host for the request. Implementations may accept {@code null} if they can still determine a
	 *            route, for example to a default target or by inspecting the request.
	 * @param responseHandler
	 *            the response handler
	 * @param onFail
	 *            function to provide the default value on exception during the communication process
	 * @return the value from the given response handler or {@code null} on client communication error.
	 */
	@SuppressWarnings({ "boxing", "static-method" })
	public <T> T execute(HttpUriRequest request, HttpClientContext context, HttpHost targetHost,
			BiFunction<Integer, HttpResponse, T> responseHandler, Function<IOException, T> onFail) {
		try (CloseableHttpClient client = HttpClientBuilder.create().disableAutomaticRetries().build()) {
			return client.execute(targetHost, request,
					response -> responseHandler.apply(response.getStatusLine().getStatusCode(), response), context);
		} catch (IOException e) {
			return onFail.apply(e);
		}
	}
}
