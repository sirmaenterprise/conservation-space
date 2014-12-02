package com.sirma.itt.emf.web.notification;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * The event MessageHandledEvent can be risen when user clicks on notification message popup action
 * button.
 * 
 * @author svelikov
 */
@Documentation("The event MessageHandledEvent can be risen when user clicks on notification message popup action button")
public class MessageHandledEvent implements EmfEvent {

	/** The message id. */
	private String messageId;

	/**
	 * Instantiates a new message handled event.
	 * 
	 * @param messageId
	 *            the message id
	 */
	public MessageHandledEvent(String messageId) {
		this.messageId = messageId;
	}

	/**
	 * Getter method for messageId.
	 * 
	 * @return the messageId
	 */
	public String getMessageId() {
		return messageId;
	}

	/**
	 * Setter method for messageId.
	 * 
	 * @param messageId
	 *            the messageId to set
	 */
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
}
