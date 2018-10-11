package com.sirma.itt.seip.eai.content.tool.service.net;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import com.sirma.itt.seip.eai.content.tool.model.ContentInfo;
import com.sirma.itt.seip.eai.content.tool.model.EAIContentConstants;

/**
 * This utility class provides an abstraction layer for sending multipart HTTP POST requests to a web server.
 * 
 * @author gshevkedov
 * @author bbanchev
 */
public class MultipartSender extends PayloadRequestSender {
	private static final String LINE_FEED = "\r\n";
	private String boundary;
	private OutputStream outputStream;

	/**
	 * This constructor initializes a new HTTP POST request with content type is set to multipart/form-data
	 * 
	 * @param requestURL
	 *            the request URI
	 */
	public MultipartSender(URI requestURL) {
		super(requestURL, EAIContentConstants.METHOD_POST);
	}

	@Override
	protected void createConnection() throws IOException {
		// creates a unique boundary based on time stamp
		boundary = "===" + System.currentTimeMillis() + "===";
		super.createConnection();
		con.setUseCaches(false);
		con.setDoInput(true);
		addRequestHeaders(Collections.singletonMap(EAIContentConstants.HEADER_CONTENT_TYPE,
				"multipart/form-data; boundary=" + boundary));
	}

	@Override
	protected Writer createWriter() throws IOException {
		outputStream = con.getOutputStream();
		return new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
	}

	/**
	 * Adds a upload file section to the request
	 * 
	 * @param fieldName
	 *            name attribute in <input type="file" name="..." />
	 * @param uploadFile
	 *            a File to be uploaded
	 * @throws IOException
	 *             in case of any communication failure
	 */
	public void addFilePart(String fieldName, File uploadFile) throws IOException {
		String fileName = uploadFile.getName();
		appendRequestPayload("--" + boundary + LINE_FEED);
		addMultipartHeader("Content-Disposition",
				"form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"");
		addMultipartHeader("Content-Transfer-Encoding", "binary");
		addMultipartHeader(EAIContentConstants.HEADER_CONTENT_TYPE, URLConnection.guessContentTypeFromName(fileName));
		appendRequestPayload(LINE_FEED);
		try (InputStream inputStream = new BufferedInputStream(new FileInputStream(uploadFile))) {
			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			outputStream.flush();
		}
		appendRequestPayload(LINE_FEED);
	}

	/**
	 * Adds a header field to the request.
	 * 
	 * @param name
	 *            - name of the header field
	 * @param value
	 *            - value of the header field
	 */
	private void addMultipartHeader(String name, String value) throws IOException {
		appendRequestPayload(name + ":" + value + LINE_FEED);
	}

	/**
	 * Completes the request and receives response from the server.
	 * 
	 * @return a list of Strings as response in case the server returned status OK, otherwise an exception is thrown.
	 * @throws IOException
	 *             on any communication error
	 */
	@Override
	public ContentInfo send() throws IOException {
		appendRequestPayload(LINE_FEED + "--" + boundary + "--" + LINE_FEED);
		return super.send();
	}
}