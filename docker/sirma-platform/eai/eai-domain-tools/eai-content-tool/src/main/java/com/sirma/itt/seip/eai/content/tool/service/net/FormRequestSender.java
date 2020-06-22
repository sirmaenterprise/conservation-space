package com.sirma.itt.seip.eai.content.tool.service.net;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.sirma.itt.seip.eai.content.tool.model.ContentInfo;


/**
 * This utility class provides an abstraction layer for sending multipart form requests to a web server. The binary data
 * is send via streaming manner not to occupy memory and cause OOM errors
 * 
 * @author BBonev
 */
public class FormRequestSender implements NetworkService, AutoCloseable {

	private static final ContentType CONTENT_TYPE = ContentType.create("text/plain", Consts.UTF_8);
	private final CloseableHttpClient httpClient;
	private final HttpPost httpPost;
	private final MultipartEntityBuilder entityBuilder;
	private CloseableHttpResponse response;

	/**
	 * This constructor initializes a new HTTP POST request with content type is set to multipart/form-data
	 *
	 * @param requestURL the request URI
	 * @param requestHeaders any additional request headers
	 */
	public FormRequestSender(URI requestURL, Map<String, String> requestHeaders) {
		httpClient = HttpClientBuilder.create()
				.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
				.setMaxConnTotal(1)
				.build();
		httpPost = new HttpPost(requestURL);
		requestHeaders.forEach(httpPost::addHeader);
		entityBuilder = MultipartEntityBuilder.create()
				.setCharset(Consts.UTF_8)
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
	}

	/**
	 * Adds a form value to the request
	 *
	 * @param fieldName
	 *            name attribute in <input type="file" name="..." />
	 * @param value
	 *            a File to be uploaded
	 */
	public void addPart(String fieldName, String value) {
		entityBuilder.addTextBody(fieldName, value, CONTENT_TYPE);
	}

	/**
	 * Adds a upload file section to the request
	 * 
	 * @param fieldName
	 *            name attribute in <input type="file" name="..." />
	 * @param uploadFile
	 *            a File to be uploaded
	 */
	public void addFilePart(String fieldName, File uploadFile) {
		entityBuilder.addBinaryBody(fieldName, uploadFile);
	}

	/**
	 * Completes the request and receives response from the server. The returned response is a copy of the original
	 * response. This is done some in order to allow the implementation close any resources
	 * 
	 * @return a list of Strings as response in case the server returned status OK, otherwise an exception is thrown.
	 * @throws IOException
	 *             on any communication error
	 */
	@Override
	public ContentInfo send() throws IOException {
		httpPost.setEntity(entityBuilder.build());
		response = httpClient.execute(httpPost);
		HttpEntity entity = response.getEntity();
		StatusLine statusLine = response.getStatusLine();
		if (statusLine.getStatusCode() == 200 && entity != null) {
			return new ContentInfo(entity.getContentType().getValue(), httpPost.getURI(), entity.getContent());
		}
		throw new IOException("Request finished with status code " + statusLine.getStatusCode() + " " + statusLine
				.getReasonPhrase());
	}

	@Override
	public void close() throws IOException {
		try {
			if (response != null) {
				response.close();
			}
		} finally {
			httpClient.close();
		}
	}
}
