package com.sirma.itt.seip.eai.content.tool.service.net;

import java.io.IOException;

import com.sirma.itt.seip.eai.content.tool.model.ContentInfo;

/**
 * This interface provides an abstraction layer for sending HTTP requests to a web server.
 * 
 * @author gshevkedov
 * @author bbanchev
 */
@FunctionalInterface
public interface NetworkService {

	/**
	 * Sends http requests to web servera and transforms the code as {@link ContentInfo}
	 * 
	 * @param uri
	 *            the uri
	 * @return the {@link ContentInfo} object which includes inputstream and content type of the request
	 * @throws IOException
	 *             in case of failure or unexpected result as code !=200
	 */
	public ContentInfo send() throws IOException;

}
