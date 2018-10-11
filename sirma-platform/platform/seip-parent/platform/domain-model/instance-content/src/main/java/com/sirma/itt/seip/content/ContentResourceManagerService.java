package com.sirma.itt.seip.content;

import java.io.Serializable;
import java.util.Map;

import com.sirma.sep.content.ContentInfo;

/**
 * Service for managing the content.
 *
 * @author Nikolay Ch
 */
public interface ContentResourceManagerService {

	/**
	 * Uploads the content to the given instanceId, where the key in the map is the purpose and the value is the
	 * content.
	 *
	 * @param instanceId
	 *            the id of the instance
	 * @param contentMapping
	 *            the mapping with the purpose and the content.
	 */
	public void uploadContent(Serializable instanceId, Map<Serializable, String> contentMapping);

	/**
	 * Gets the content with the given id and purpose.
	 *
	 * @param instanceId
	 *            the id of the instance
	 * @param purpose
	 *            the purpose
	 * @return the content
	 */
	public ContentInfo getContent(Serializable instanceId, String purpose);
}
