package com.sirma.itt.seip.eai.content.tool.service.net;

import java.net.URI;

import com.sirma.itt.seip.eai.content.tool.model.EAIContentConstants;

/**
 * This class provides an abstraction layer for sending HTTP Get requests to a web server.
 * 
 * @author gshevkedov
 */
public class GetRequestSender extends RequestSender {

	/**
	 * Initialize instance of {@link GetRequestSender}
	 * 
	 * @param uri
	 *            the specified uri
	 */
	public GetRequestSender(URI uri) {
		super(uri);
	}

	@Override
	protected String getMethod() {
		return EAIContentConstants.METHOD_GET;
	}
}
