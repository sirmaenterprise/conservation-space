package com.sirma.itt.seip.eai.content.tool.service.net;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sirma.itt.seip.eai.content.tool.exception.EAINetworkException;
import com.sirma.itt.seip.eai.content.tool.model.ContentInfo;

/**
 * Abstract class for sending http request.
 * 
 * @author gshevkedov
 * @author bbanchev
 */
public abstract class RequestSender implements NetworkService {
	protected HttpURLConnection con;
	protected URI uri;
	protected Map<String, List<String>> headerFields;

	/**
	 * Builds new service based on provided uri
	 * 
	 * @param uri
	 *            is the connection endpoint address
	 */
	protected RequestSender(URI uri) {
		this.uri = uri;
	}

	/**
	 * Initialize the request with provided headers, builds a new connection for the current uri and may provide
	 * additional configurations for specific sub class.
	 * 
	 * @param headers
	 *            are the headers to set
	 * @return the configured service
	 * @throws IOException
	 *             on any error during communication
	 */
	public RequestSender init(Map<String, String> headers) throws IOException {
		createConnection();
		addRequestHeaders(headers);
		return this;
	}

	/**
	 * Creates the connection for this service
	 * 
	 * @throws IOException
	 *             on any error during communication
	 */
	protected void createConnection() throws IOException {
		this.con = (HttpURLConnection) uri.toURL().openConnection();
		this.con.setRequestMethod(getMethod());
	}

	/**
	 * Gets the method type for this connection
	 * 
	 * @return the method as GET, POST, etc.
	 */
	protected abstract String getMethod();

	/**
	 * Adds the provided headers to the connection
	 * 
	 * @param headers
	 *            the headers to add
	 */
	protected void addRequestHeaders(Map<String, String> headers) {
		if (headers == null) {
			return;
		}
		for (Entry<String, String> entry : headers.entrySet()) {
			con.setRequestProperty(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public ContentInfo send() throws IOException {
		headerFields = con.getHeaderFields();
		if (con.getResponseCode() == 200) {
			return new ContentInfo(con.getContentType(), uri, con.getInputStream());
		}
		throw new EAINetworkException("Request failed with message: " + con.getResponseMessage());
	}

	/**
	 * Gets the response headers that are populated after sending request
	 * 
	 * @return the request header after invoking {@link #send()}
	 */
	public Map<String, List<String>> getResponseHeaders() {
		return headerFields;
	}
}
