package com.sirma.itt.emf.web.notification;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.web.EmfWebTest;

/**
 * Test for notification support service.
 * 
 * @author svelikov
 */
@Test
public class NotificationSupportImplTest extends EmfWebTest {

	private static final String NOTIFICATION = "notification";

	/** The action. */
	private final NotificationSupportImpl action;

	/**
	 * Instantiates a new notification support impl test.
	 */
	public NotificationSupportImplTest() {
		action = new NotificationSupportImpl() {

			private Set<MessageLevel> keys = new LinkedHashSet<MessageLevel>();

			private Map<MessageLevel, Set<NotificationMessage>> notificationMessages = new HashMap<MessageLevel, Set<NotificationMessage>>();

			@Override
			public Map<MessageLevel, Set<NotificationMessage>> getNotificationMessages() {
				return notificationMessages;
			}

			public void setNotificationMessages(
					Map<MessageLevel, Set<NotificationMessage>> notificationMessages) {
				this.notificationMessages = notificationMessages;
			}

			@Override
			public Set<MessageLevel> getKeys() {
				return keys;
			}

			public void setKeys(Set<MessageLevel> keys) {
				this.keys = keys;
			}
		};
	}

	/**
	 * Reset test.
	 */
	@BeforeMethod
	public void resetTest() {
		action.getKeys().clear();
		action.getNotificationMessages().clear();
	}

	/**
	 * Adds the message test.
	 */
	public void addMessageObjectTest() {
		// if message is not provided, then nothing should be added
		action.addMessage(null);
		assertTrue(action.getNotificationMessages().isEmpty());
		assertTrue(action.getKeys().isEmpty());

		NotificationMessage message = new NotificationMessage(NOTIFICATION, MessageLevel.ERROR);
		action.addMessage(message);
		// check if provided level is set
		assertTrue(action.getKeys().size() == 1);
		MessageLevel messageLevel = action.getKeys().iterator().next();
		assertEquals(messageLevel, MessageLevel.ERROR);
		// check if provided message is set
		assertTrue(action.getNotificationMessages().size() == 1);
		Set<NotificationMessage> mesagesForLevel = action.getNotificationMessages().get(
				MessageLevel.ERROR);
		assertTrue(mesagesForLevel.size() == 1);
		NotificationMessage notificationMessage = mesagesForLevel.iterator().next();
		assertEquals(notificationMessage, message);
	}

	/**
	 * Adds the message arguments test.
	 */
	public void addMessageArgumentsTest() {
		// if message is not provided, then nothing should be added
		action.addMessage(null, null, null);
		assertTrue(action.getNotificationMessages().isEmpty());
		assertTrue(action.getKeys().isEmpty());

		// if message is provided, then other attributes are set by default and message should be
		// added
		action.addMessage(null, NOTIFICATION, null);
		// check if provided level is set
		assertTrue(action.getKeys().size() == 1);
		MessageLevel messageLevel = action.getKeys().iterator().next();
		assertEquals(messageLevel, MessageLevel.INFO);
		// check if provided message is set
		assertTrue(action.getNotificationMessages().size() == 1);
		Set<NotificationMessage> mesagesForLevel = action.getNotificationMessages().get(
				MessageLevel.INFO);
		assertTrue(mesagesForLevel.size() == 1);
		NotificationMessage notificationMessage = mesagesForLevel.iterator().next();
		assertEquals(notificationMessage.getMessage(), NOTIFICATION);
	}

	/**
	 * Adds the message observer test.
	 */
	public void addMessageObserverTest() {
		// if message is not provided, then nothing should be added
		NotificationMessage message = new NotificationMessage(null, MessageLevel.ERROR);
		UpdateMessageQueueEvent event = new UpdateMessageQueueEvent(message);
		action.addMessageObserver(event);
		assertTrue(action.getNotificationMessages().isEmpty());
		assertTrue(action.getKeys().isEmpty());

		// if message is added, then message should be added
		message = new NotificationMessage(NOTIFICATION, MessageLevel.ERROR);
		event = new UpdateMessageQueueEvent(message);
		action.addMessageObserver(event);
		// check if provided level is set
		assertTrue(action.getKeys().size() == 1);
		MessageLevel messageLevel = action.getKeys().iterator().next();
		assertEquals(messageLevel, MessageLevel.ERROR);
		// check if provided message is set
		assertTrue(action.getNotificationMessages().size() == 1);
		Set<NotificationMessage> mesagesForLevel = action.getNotificationMessages().get(
				MessageLevel.ERROR);
		assertTrue(mesagesForLevel.size() == 1);
		NotificationMessage notificationMessage = mesagesForLevel.iterator().next();
		assertEquals(notificationMessage, message);
	}

	/**
	 * Clear notifications test.
	 */
	public void clearNotificationsTest() {
		NotificationMessage message = new NotificationMessage(NOTIFICATION, MessageLevel.ERROR);
		action.addMessage(message);
		action.clearNotifications();
		assertTrue(action.getKeys().size() == 0);
		assertTrue(action.getNotificationMessages().size() == 0);
	}
}
