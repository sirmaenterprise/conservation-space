package com.sirma.itt.emf.forum.event;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired to notify that a user has read a certain topic.
 * 
 * @author BBonev
 */
@Documentation("Event fired to notify that a user has read a certain topic.")
public class TopicReadEvent implements EmfEvent {

	/** The topic id. */
	private final String topicId;

	/** The user id. */
	private final User user;

	/** The timestamp. */
	private final long timestamp;

	/**
	 * Instantiates a new topic read event.
	 * 
	 * @param topicId
	 *            the topic id
	 * @param user
	 *            the user
	 */
	public TopicReadEvent(String topicId, User user) {
		this.topicId = topicId;
		this.user = user;
		timestamp = System.currentTimeMillis();
	}

	/**
	 * Getter method for topicId.
	 * 
	 * @return the topicId
	 */
	public String getTopicId() {
		return topicId;
	}

	/**
	 * Getter method for userId.
	 * 
	 * @return the userId
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Getter method for timestamp.
	 * 
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

}
