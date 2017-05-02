package com.sirma.itt.seip.instance.notification;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.event.EmfEvent;

/**
 * Used to add new message to the messages queued for the user.
 *
 * @author svelikov
 */
@Documentation("Used to add new message to the messages queued for the user.")
public class UpdateMessageQueueEvent implements EmfEvent {

	private NotificationMessage notificationMessage;

	/**
	 * Instantiates a new update message queue event.
	 *
	 * @param notificationMessage
	 *            the notification message
	 */
	public UpdateMessageQueueEvent(NotificationMessage notificationMessage) {
		this.notificationMessage = notificationMessage;
	}

	/**
	 * Getter method for notificationMessage.
	 *
	 * @return the notificationMessage
	 */
	public NotificationMessage getNotificationMessage() {
		return notificationMessage;
	}

	/**
	 * Setter method for notificationMessage.
	 *
	 * @param notificationMessage
	 *            the notificationMessage to set
	 */
	public void setNotificationMessage(NotificationMessage notificationMessage) {
		this.notificationMessage = notificationMessage;
	}

}
