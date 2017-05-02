package com.sirma.itt.emf.notification.script;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.instance.notification.MessageLevel;
import com.sirma.itt.seip.instance.notification.NotificationMessage;
import com.sirma.itt.seip.instance.notification.NotificationSupport;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.script.GlobalBindingsExtension;

/**
 * Script extension to provider access to notification API
 *
 * @author BBonev
 */
@Extension(target = GlobalBindingsExtension.TARGET_NAME, order = 18)
public class NotificationScriptProvider implements GlobalBindingsExtension {

	private static final Logger LOGGER = LoggerFactory.getLogger(NotificationScriptProvider.class);

	@Inject
	private Instance<NotificationSupport> notificationSupport;
	private boolean enabled = true;

	@Inject
	private LabelProvider labelProvider;

	/**
	 * Initialize the bean
	 */
	@PostConstruct
	public void initialize() {
		enabled = !notificationSupport.isUnsatisfied();
		if (!enabled) {
			LOGGER.warn("Notification service not installed. Script notification integration is disabled!");
		}
	}

	@Override
	public Map<String, Object> getBindings() {
		if (!enabled) {
			return Collections.emptyMap();
		}
		return Collections.<String, Object> singletonMap("notification", this);
	}

	@Override
	public Collection<String> getScripts() {
		return Collections.emptyList();
	}

	/**
	 * Display a message to the user via web UI
	 *
	 * @param level
	 *            Specifies the message level type to be displayed.
	 * @param message
	 *            The message to be displayed to the user
	 */
	public void notifyUser(String level, String message) {
		MessageLevel messageLevel = buildMessageLevel(level);
		NotificationMessage notificationMessage = new NotificationMessage(message, messageLevel);
		sendNotification(notificationMessage);

	}

	/**
	 * Provides localized user notification via WEB.
	 *
	 * @param level
	 *            message notification level
	 * @param message
	 *            notification message
	 * @param label
	 *            bundle label for localization
	 */
	public void notifyUser(String level, String label, String message) {
		MessageLevel messageLevel = buildMessageLevel(level);
		NotificationMessage notificationMessage = new NotificationMessage(
				MessageFormat.format(labelProvider.getValue(label), message), messageLevel);
		sendNotification(notificationMessage);

	}

	/**
	 * Provides proper message level for user notification.
	 *
	 * @param messageLevel
	 *            message level that will be displayed to user
	 */
	private MessageLevel buildMessageLevel(String messageLevel) {
		String level = messageLevel;
		try {
			if (level != null) {
				level = level.toUpperCase();
			}
			return MessageLevel.valueOf(level);
		} catch (RuntimeException e) {
			LOGGER.warn("Unknown message level [{}]. Using default.", messageLevel);
			LOGGER.trace("Unknown message level.", e);
			return null;
		}

	}

	/**
	 * Sends message to Notification API.
	 *
	 * @param message
	 *            the message passed to NotificationAPI
	 */
	private void sendNotification(NotificationMessage message) {
		try {
			notificationSupport.get().addMessage(message);
		} catch (ContextNotActiveException e) {
			LOGGER.error("Failed to add notification to user due to non active session context. The message is: {}",
					message);
			LOGGER.trace("No active session for notification support", e);
		}
	}

}
