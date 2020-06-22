package com.sirma.itt.migration.register;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

/**
 * Helper class for creating CRC codes for file's content and name.
 * 
 * @author BBonev
 */
public class CheckBuilder {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(CheckBuilder.class);
	/** The node service. */
	private NodeService nodeService;
	/** The content service. */
	private ContentService contentService;

	/**
	 * Creates and unique code for the given file detonated by the given path.
	 * The code is created from the file's name and file's content.
	 * <p>
	 * <b>NOTE:</b> The method could be time and memory consuming if the file is
	 * too big it may throw and {@link IllegalArgumentException} if there was
	 * not enough available memory!
	 * 
	 * @param ref
	 *            the target node reference
	 * @return the unique code for the given file or <code>null</code> if there
	 *         was an error while reading file.
	 */
	public String createCheckCode(NodeRef ref) {
		ContentReader reader = contentService.getReader(ref,
				ContentModel.PROP_CONTENT);
		if (reader == null) {
			return null;
		}

		if (reader.getSize() > Runtime.getRuntime().freeMemory()) {
			// if called on the server this should not happen
			throw new IllegalArgumentException(
					"Trying to load a file bigger than the available memory in the current VM");
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
				(int) reader.getSize());
		try {
			reader.getContent(outputStream);
		} catch (ContentIOException e) {
			LOGGER.warn("Failed to read content of the node " + ref
					+ " due to " + e.getMessage());
			return null;
		}
		// create digest from the file's contents
		String contentDigest = DigestUtils.shaHex(outputStream.toByteArray());
		String nameDigest = createCheckCodeForName(ref);
		// merge the two digests into one
		String resultDigest = DigestUtils.shaHex((nameDigest + contentDigest)
				.getBytes());
		return resultDigest;
	}

	/**
	 * Creates a digest from the name of the given file detonated by the
	 * specified path.
	 * 
	 * @param ref
	 *            the target node reference
	 * @return the check code for the name of the file
	 */
	public String createCheckCodeForName(NodeRef ref) {
		byte[] data;
		Serializable property = nodeService.getProperty(ref, ContentModel.PROP_NAME);
		try {
			data = property.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			data = property.toString().getBytes();
		}
		// create digest from the name of the file
		String nameDigest = DigestUtils.shaHex(data);
		return nameDigest;
	}

	/**
	 * Getter method for nodeService.
	 *
	 * @return the nodeService
	 */
	public NodeService getNodeService() {
		return nodeService;
	}

	/**
	 * Setter method for nodeService.
	 *
	 * @param nodeService
	 *            the nodeService to set
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * Getter method for contentService.
	 *
	 * @return the contentService
	 */
	public ContentService getContentService() {
		return contentService;
	}

	/**
	 * Setter method for contentService.
	 *
	 * @param contentService
	 *            the contentService to set
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

}
