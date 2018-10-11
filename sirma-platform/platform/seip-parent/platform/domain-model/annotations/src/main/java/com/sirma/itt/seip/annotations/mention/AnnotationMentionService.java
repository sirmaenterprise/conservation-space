package com.sirma.itt.seip.annotations.mention;

import java.io.Serializable;
import java.util.Collection;

/**
 * Annotation mention service interface.
 *
 * @author tdossev
 */
public interface AnnotationMentionService {

	/**
	 * Notifies mentioned users.
	 *
	 * @param mentionedUsers
	 *            to be notified
	 * @param commentedInstanceId
	 *            id of the instance holding the comment
	 * @param commentsOn
	 *            commented tab
	 * @param commentedBy
	 *            commented user
	 */
	void sendNotifications(Collection<Serializable> mentionedUsers, String commentedInstanceId, Serializable commentsOn,
			String commentedBy);

	/**
	 * Loads previously mentioned users
	 * 
	 * @param annotationId
	 *            annotation id
	 * @return collection of mentioned users
	 */
	Collection<Serializable> loadMentionedUsers(String annotationId);

}
