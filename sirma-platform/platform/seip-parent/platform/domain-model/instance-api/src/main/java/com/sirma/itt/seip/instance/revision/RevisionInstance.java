package com.sirma.itt.seip.instance.revision;

import java.io.Serializable;
import java.util.Date;

/**
 * Interface that marks a object that supports revisions. Also provides some methods for working with the revision. If
 * an object implements the interface and the method {@link #isPublished()} returns <code>true</code> then the methods
 * {@link #getPublishDate()} and {@link #getPublishBy()} should return non <code>null</code> values.
 *
 * @author BBonev
 */
public interface RevisionInstance {

	/**
	 * Checks if is current instance published.
	 *
	 * @return true, if is published
	 */
	boolean isPublished();

	/**
	 * Gets the revision number. A number could be returned event the instance is not published, yet.
	 *
	 * @return the revision number
	 */
	String getRevisionNumber();

	/**
	 * Gets the publish date.
	 *
	 * @return the publish date
	 */
	Date getPublishDate();

	/**
	 * Gets the publish by.
	 *
	 * @return the publish by
	 */
	Serializable getPublishBy();

	/**
	 * Changes the state of the instance to published and update the properties associated with the publish operation.
	 *
	 * @param newRevisionNumber
	 *            the new revision number
	 * @param publishBy
	 *            the publish by
	 * @param publishDate
	 *            the publish date
	 */
	void publish(String newRevisionNumber, Serializable publishBy, Date publishDate);

}
