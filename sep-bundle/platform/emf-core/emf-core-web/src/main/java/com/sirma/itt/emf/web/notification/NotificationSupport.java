package com.sirma.itt.emf.web.notification;

import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Observes;

/**
 * The Interface NotificationSupport.
 * 
 * @author svelikov
 */
public interface NotificationSupport {

	MessageLevel DEFAULT_LEVEL = MessageLevel.INFO;

	/**
	 * Adds the message if provided. If none provided this method returns silently.
	 * 
	 * @param notificationMessage
	 *            the notification message
	 */
	void addMessage(NotificationMessage notificationMessage);

	/**
	 * Adds the message.
	 * 
	 * @param messageId
	 *            the message id
	 * @param message
	 *            the message
	 * @param messageLevel
	 *            the message level
	 */
	void addMessage(String messageId, String message, MessageLevel messageLevel);

	/**
	 * Observer that can add new message to the list.
	 * 
	 * @param event
	 *            the event
	 */
	void addMessageObserver(@Observes UpdateMessageQueueEvent event);

	/**
	 * Gets the notification messages.
	 * 
	 * @return the notification messages
	 */
	Map<MessageLevel, Set<NotificationMessage>> getNotificationMessages();

	/**
	 * Gets the keys.
	 * 
	 * @return the keys
	 */
	Set<MessageLevel> getKeys();

	/**
	 * Clear all notifications.
	 */
	void clearNotifications();
}
