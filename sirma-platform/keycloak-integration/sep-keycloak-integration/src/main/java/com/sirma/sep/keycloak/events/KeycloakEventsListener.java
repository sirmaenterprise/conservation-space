package com.sirma.sep.keycloak.events;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.security.event.UserAuthenticatedEvent;
import com.sirma.itt.emf.security.event.UserLogoutEvent;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.resources.event.UserPasswordChangeEvent;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirmaenterprise.sep.jms.annotations.DestinationDef;
import com.sirmaenterprise.sep.jms.annotations.DestinationType;
import com.sirmaenterprise.sep.jms.annotations.JmsSender;
import com.sirmaenterprise.sep.jms.annotations.TopicListener;
import com.sirmaenterprise.sep.jms.api.MessageSender;

/**
 * Listens for messages from {@link javax.jms.Topic} and fires the appropriate event for audit logging.
 * Keycloak is sending JMS messages remotely to this topic.
 *
 * @author smustafov
 */
@Singleton
public class KeycloakEventsListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	static final String EVENT_TYPE_KEY = "event_type";
	static final String AUTHENTICATED_USER_KEY = "authenticated_user";
	static final String EVENT_TIMESTAMP_KEY = "event_timestamp";

	static final String LOGIN_EVENT = "LOGIN";
	static final String LOGOUT_EVENT = "LOGOUT";
	static final String UPDATE_PASSWORD = "UPDATE_PASSWORD";  // NOSONAR

	@DestinationDef(type = DestinationType.TOPIC, deadLetterAddress = "java:/jms.queue.UserEventsTopic_DLQ", expiryAddress = "java:/jms.queue.UserEventsTopic_EQ")
	private static final String USER_EVENTS_TOPIC = "java:/jms.topic.UserEventsTopic";

	@Inject
	@JmsSender(destination = "java:/jms.queue.UserEventsTopic_DLQ")
	private MessageSender messageSender;

	@Inject
	private EventService eventService;

	@Inject
	private UserStore userStore;

	@TopicListener(jndi = USER_EVENTS_TOPIC, subscription = "userEvents")
	public void onEvent(Message message) throws JMSException {
		String eventType = message.getStringProperty(EVENT_TYPE_KEY);
		String username = message.getStringProperty(AUTHENTICATED_USER_KEY);
		long timestamp = message.getLongProperty(EVENT_TIMESTAMP_KEY);

		LOGGER.debug("Received event {} from user {} on {}", eventType, username, timestamp);

		User user = userStore.loadByIdentityId(username);
		processEvent(eventType, user, timestamp);
	}

	private void processEvent(String eventType, User user, long timestamp) {
		switch (eventType) {
		case LOGIN_EVENT:
			eventService.fire(new UserAuthenticatedEvent(user));
			break;
		case LOGOUT_EVENT:
			eventService.fire(new UserLogoutEvent(user));
			break;
		case UPDATE_PASSWORD:
			eventService.fire(new UserPasswordChangeEvent(user.getIdentityId(), null));
			break;
		default:
			sendMessageToDLQ(eventType, user.getIdentityId(), timestamp);
		}
	}

	private void sendMessageToDLQ(String eventType, String username, long timestamp) {
		LOGGER.warn("Received unsupported event type {} from user {}. Sending the message to the DLQ", eventType,
				username);

		messageSender.send(message -> {
			message.setStringProperty(EVENT_TYPE_KEY, eventType);
			message.setLongProperty(EVENT_TIMESTAMP_KEY, timestamp);
			message.setStringProperty(AUTHENTICATED_USER_KEY, username);
		});
	}

}
