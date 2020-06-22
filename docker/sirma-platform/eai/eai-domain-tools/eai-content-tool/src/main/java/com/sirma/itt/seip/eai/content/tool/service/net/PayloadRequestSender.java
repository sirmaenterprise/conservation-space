package com.sirma.itt.seip.eai.content.tool.service.net;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.sirma.itt.seip.eai.content.tool.model.ContentInfo;

/**
 * This class provides an abstraction layer for sending HTTP PATCH/POST requests to a web server.
 * 
 * @author gshevkedov
 * @author bbanchev
 */
public class PayloadRequestSender extends RequestSender {
	protected Writer writer;
	protected String method;

	/**
	 * Initialize instance of {@link PayloadRequestSender}
	 * 
	 * @param uri
	 *            the specified uri
	 * @param method
	 *            the specific method type - POST, PATCH, etc.
	 */
	public PayloadRequestSender(URI uri, String method) {
		super(uri);
		this.method = method;
	}

	@Override
	public PayloadRequestSender init(Map<String, String> headers) throws IOException {
		super.init(headers);
		writer = createWriter();
		return this;
	}

	protected Writer createWriter() throws IOException {
		return new OutputStreamWriter(con.getOutputStream(), StandardCharsets.UTF_8);
	}

	@Override
	protected void createConnection() throws IOException {
		super.createConnection();
		con.setDoOutput(true);
	}

	@Override
	protected String getMethod() {
		return method;
	}

	/**
	 * Adds the request payload to http request
	 * 
	 * @param payload
	 *            the payload
	 * @throws IOException
	 *             when payload could not be streamed
	 */
	public void appendRequestPayload(String payload) throws IOException {
		writer.append(payload);
		writer.flush();
	}

	@Override
	public ContentInfo send() throws IOException {
		writer.flush();
		writer.close();
		return super.send();
	}

}
