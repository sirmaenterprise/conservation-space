/**
 * Copyright (c) 2014 29.05.2014 , Sirma ITT. /* /**
 */
package com.sirma.itt.seip.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Performs requests to EMF restful services.
 *
 * @author Adrian Mitev
 */
public class Rest {

	/**
	 * Performs a get request.
	 *
	 * @param serviceUrl
	 *            url of the rest service.
	 * @param cookie
	 *            cookie to use within the request
	 * @return query response.
	 */
	public static HttpResponse get(String serviceUrl, String cookie) {
		HttpGet request = new HttpGet(serviceUrl);

		return performRequest(request, cookie);
	}

	/**
	 * Performs a GET request.
	 *
	 * @param serviceUrl
	 *            url of the rest service.
	 * @param cookie
	 *            cookie to use within the request
	 * @param json
	 *            json data to post
	 * @return query response.
	 */
	public static HttpResponse jsonPost(String serviceUrl, String cookie, String json) {
		HttpPost request = new HttpPost(serviceUrl);
		request.addHeader("Content-Type", "application/json");

		request.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

		return performRequest(request, cookie);
	}

	/**
	 * Performs a PUT request.
	 *
	 * @param serviceUrl
	 *            url of the rest service.
	 * @param cookie
	 *            cookie to use within the request
	 * @param json
	 *            json data to post
	 * @return query response.
	 */
	public static HttpResponse jsonPut(String serviceUrl, String cookie, String json) {
		HttpPut request = new HttpPut(serviceUrl);
		request.addHeader("Content-Type", "application/json");

		request.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

		return performRequest(request, cookie);
	}

	/**
	 * Performs a POST request.
	 *
	 * @param serviceUrl
	 *            url of the rest service.
	 * @param cookie
	 *            cookie to use within the request
	 * @param data
	 *            key-value pairs to post.
	 * @return query response.
	 */
	public static HttpResponse post(String serviceUrl, String cookie, Map<String, String> data) {
		HttpPost request = new HttpPost(serviceUrl);

		try {
			List<NameValuePair> postPairs = new ArrayList<>();
			for (Entry<String, String> entry : data.entrySet()) {
				postPairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}

			request.setEntity(new UrlEncodedFormEntity(postPairs, StandardCharsets.UTF_8.name()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		return performRequest(request, cookie);
	}

	/**
	 * Performs an http request and adds a cookie if provided.
	 *
	 * @param request
	 *            request to execute
	 * @param cookie
	 *            cookie to use within the request
	 * @return response of the call.
	 */
	private static HttpResponse performRequest(HttpRequestBase request, String cookie) {
		if (cookie != null) {
			request.addHeader("Cookie", cookie);
		}

		try (CloseableHttpClient client = HttpClientBuilder.create().build();
				CloseableHttpResponse response = client.execute(request)) {
			String responseText = null;
			JsonObject resultJson = null;

			// parse json if available
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				responseText = EntityUtils.toString(entity, StandardCharsets.UTF_8);
				if (responseText != null
						&& entity.getContentType().getValue().contains("application/json")) {
					JsonElement parsed = new JsonParser().parse(responseText);
					if (parsed instanceof JsonObject) {
						resultJson = (JsonObject) parsed;
					} else if (parsed instanceof JsonArray) {
						resultJson = new JsonObject();
						resultJson.add("data", parsed);
					}
				}
			}

			String cookieToSet = null;
			if (response.getFirstHeader("Set-Cookie") != null) {
				cookieToSet = response.getFirstHeader("Set-Cookie").getValue();
			}

			return new HttpResponse(response.getStatusLine().getStatusCode(), responseText,
					resultJson, cookieToSet, response.getAllHeaders());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
