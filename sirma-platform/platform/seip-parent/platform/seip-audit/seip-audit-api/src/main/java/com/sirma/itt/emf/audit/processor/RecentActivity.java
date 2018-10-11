package com.sirma.itt.emf.audit.processor;

import java.util.Date;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Represents a recent activity that will be returned to the user.
 *
 * @author nvelkov
 */
public class RecentActivity {

	private Instance user;
	private Date timestamp;
	private String sentence;

	/**
	 * Create a new recent activity
	 *
	 * @param user
	 *            the user that performed the activity
	 * @param timestamp
	 *            the date at wich the activity was performed
	 * @param sentence
	 *            the activity in a more user-friendly format.
	 */
	public RecentActivity(Instance user, Date timestamp, String sentence) {
		this.user = user;
		this.timestamp = timestamp;
		this.sentence = sentence;
	}

	/**
	 * Getter method for user.
	 *
	 * @return the user
	 */
	public Instance getUser() {
		return user;
	}

	/**
	 * Setter method for user.
	 *
	 * @param user
	 *            the user to set
	 */
	public void setUser(Instance user) {
		this.user = user;
	}

	/**
	 * Getter method for timestamp.
	 *
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * Setter method for timestamp.
	 *
	 * @param timestamp
	 *            the timestamp to set
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Getter method for sentence.
	 *
	 * @return the sentence
	 */
	public String getSentence() {
		return sentence;
	}

	/**
	 * Setter method for sentence.
	 *
	 * @param sentence
	 *            the sentence to set
	 */
	public void setSentence(String sentence) {
		this.sentence = sentence;
	}
}
