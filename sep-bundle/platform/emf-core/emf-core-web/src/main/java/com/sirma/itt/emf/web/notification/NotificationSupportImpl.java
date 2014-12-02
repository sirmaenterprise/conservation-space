package com.sirma.itt.emf.web.notification;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.faces.bean.SessionScoped;
import javax.inject.Named;

import com.sirma.itt.commons.utils.string.StringUtils;

/**
 * Implementation for ui notifications functionality.
 * 
 * @author svelikov
 */
@Named
@SessionScoped
public class NotificationSupportImpl implements NotificationSupport, Serializable {

	private static final long serialVersionUID = -7703558511125808567L;

	/** The notification messages. */
	private Set<MessageLevel> keys;

	/** The notification messages. */
	private Map<MessageLevel, Set<NotificationMessage>> notificationMessages;

	/**
	 * Init bean.
	 */
	@PostConstruct
	public void init() {
		keys = new LinkedHashSet<MessageLevel>();
		notificationMessages = new HashMap<MessageLevel, Set<NotificationMessage>>();
	}

	@Override
	public void addMessage(NotificationMessage notificationMessage) {
		if ((notificationMessage == null) || (notificationMessage.getMessage() == null)) {
			return;
		}

		updateMessages(notificationMessage);
	}

	@Override
	public void addMessage(String messageId, String message, MessageLevel messageLevel) {
		if (message == null) {
			return;
		}
		NotificationMessage notificationMessage = new NotificationMessage(message, messageLevel);
		if (StringUtils.isNotNullOrEmpty(messageId)) {
			notificationMessage.setMessageId(messageId);
		}
		updateMessages(notificationMessage);
	}

	@Override
	public void addMessageObserver(@Observes UpdateMessageQueueEvent event) {
		if (event.getNotificationMessage() == null) {
			return;
		}
		addMessage(event.getNotificationMessage());
	}

	/**
	 * Update messages.
	 * 
	 * @param notificationMessage
	 *            the notification message
	 */
	protected void updateMessages(NotificationMessage notificationMessage) {
		MessageLevel actualLevel = DEFAULT_LEVEL;
		if (notificationMessage.getLevel() != null) {
			actualLevel = notificationMessage.getLevel();
		}

		getKeys().add(actualLevel);
		notificationMessage.setLevel(actualLevel);
		if (getNotificationMessages().containsKey(actualLevel)) {
			getNotificationMessages().get(actualLevel).add(notificationMessage);
		} else {
			Set<NotificationMessage> newLevelSet = new LinkedHashSet<NotificationMessage>();
			newLevelSet.add(notificationMessage);
			getNotificationMessages().put(actualLevel, newLevelSet);
		}
	}

	@Override
	public void clearNotifications() {
		if (getNotificationMessages() != null) {
			getNotificationMessages().clear();
		}
		if (getKeys() != null) {
			getKeys().clear();
		}
	}

	@Override
	public Map<MessageLevel, Set<NotificationMessage>> getNotificationMessages() {
		return notificationMessages;
	}

	@Override
	public Set<MessageLevel> getKeys() {
		return keys;
	}

}
