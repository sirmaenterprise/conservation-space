package com.sirma.sep.content;

import java.io.Serializable;

/**
 * {@link ContentPersister} provides means of storing the extracted content to instance for indexing
 *
 * @author BBonev
 */
public interface ContentPersister {

	/**
	 * Shows whether the content could be persisted or not for the passed instance.
	 *
	 * @param instanceId to check
	 * @return <code>true<code> if the persister can handle the passed instance, <code>false<code> otherwise
	 */
	default boolean canPersist(Serializable instanceId) {
		return true;
	}

	/**
	 * Saves the content of the primary view for instance identified by the given instance.
	 *
	 * @param instanceId the instance id to assign the extracted content
	 * @param content the content to assign. if <code>null</code> it will remove any content already assigned
	 */
	void savePrimaryView(Serializable instanceId, String content);

	/**
	 * Saves a primary content for instance identified by the given instance.
	 *
	 * @param instanceId the instance id to assign the extracted content
	 * @param content the content to assign. if <code>null</code> it will remove any content already assigned
	 */
	void savePrimaryContent(Serializable instanceId, String content);

	/**
	 * Saves a widgets content for instance identified by the given instance.
	 *
	 * @param instanceId the instance id to assign the extracted content
	 * @param content the content to assign. if <code>null</code> it will remove any content already assigned
	 */
	void saveWidgetsContent(Serializable instanceId, String content);

	/**
	 * Saves a ocr content for instance identified by the given instance.
	 *
	 * @param instanceId the instance id to assign the extracted content
	 * @param content the content to assign. if <code>null</code> it will remove any content already assigned
	 */
	void saveOcrContent(Serializable instanceId, String content);
}