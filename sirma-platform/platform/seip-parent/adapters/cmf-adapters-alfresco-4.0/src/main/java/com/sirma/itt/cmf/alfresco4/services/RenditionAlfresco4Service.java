package com.sirma.itt.cmf.alfresco4.services;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import javax.inject.Inject;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.emf.adapter.CMFRenditionAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;

/**
 * The RenditionAlfresco4Service is the alfresco 4 implementation for {@link CMFRenditionAdapterService}.
 *
 * @author Borislav Banchev
 * @author BBonev
 */
// BB: Removed application scope, because used in application scope bean and does not have a state
public class RenditionAlfresco4Service implements CMFRenditionAdapterService {
	private static final Part[] EMPTY_PARTS = new Part[0];
	private static final Logger LOGGER = LoggerFactory.getLogger(RenditionAlfresco4Service.class);
	private static final boolean DEBUG_ENABLED = LOGGER.isDebugEnabled();

	@Inject
	private RESTClient restClient;

	@Override
	public String getPrimaryThumbnailURI(String dmsId) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public int downloadThumbnail(String dmsId, OutputStream buffer) throws DMSException {
		LOGGER.trace("Downloading thumbnail for {}", dmsId);
		int indexOf = dmsId.lastIndexOf('/');
		if (indexOf < 0) {
			return -1;
		}
		try (InputStream download = getRemoteContent(
				MessageFormat.format(ServiceURIRegistry.CONTENT_THUMBNAIL_ACCESS_URI, dmsId.substring(indexOf + 1)))) {
			int copied = 0;
			if (download != null) {
				copied = IOUtils.copy(download, buffer);
			}
			return copied;
		} catch (Exception e) {
			throw new DMSException(e);
		}
	}

	/**
	 * Gets the remote content.
	 *
	 * @param path
	 *            the path
	 * @return the remote content
	 */
	private InputStream getRemoteContent(String path) {
		HttpMethod createMethod = restClient.createMethod(new GetMethod(), EMPTY_PARTS, true);
		try {
			return restClient.request(createMethod, path);
		} catch (DMSClientException e) {
			if (DEBUG_ENABLED) {
				LOGGER.debug("Failed to retrieve thumbnail for path {}", path);
				LOGGER.trace("Failed to download thumpnail", e);
			}
		}
		return null;
	}

}
