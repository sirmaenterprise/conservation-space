package com.sirma.itt.emf.adapter;

import java.io.OutputStream;

/**
 * The CMFRenditionAdapterService provides access to rendition facilities in alfresco.
 */
public interface CMFRenditionAdapterService {

	/**
	 * Gets the primary thumbnail uri pointing to alfresco content store
	 *
	 * @param dmsId
	 *            the dms id of the element to get thumbnail for
	 * @return the primary thumbnail uri
	 * @throws DMSException
	 *             the dMS exception
	 */
	String getPrimaryThumbnailURI(String dmsId) throws DMSException;

	/**
	 * Download thumbnail for provided instance dms id. The result is stored in the provided buffer. Supported are only
	 * 'content' instances like documents
	 *
	 * @param dmsId
	 *            the dms id for node
	 * @param buffer
	 *            the buffer to use for output
	 * @return the count of bytes written in buffer
	 * @throws DMSException
	 *             the dMS exception on any error
	 */
	int downloadThumbnail(String dmsId, OutputStream buffer) throws DMSException;
}
