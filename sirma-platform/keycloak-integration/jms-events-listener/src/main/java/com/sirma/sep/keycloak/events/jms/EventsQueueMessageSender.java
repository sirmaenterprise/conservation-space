package com.sirma.sep.keycloak.events.jms;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;

import org.jboss.logging.Logger;
import org.keycloak.events.Event;

import com.sirma.sep.keycloak.events.exception.JmsEventListenerException;

/**
 * Provides ability to send messages to the local events queue.
 *
 * @author smustafov
 */
@Stateless
public class EventsQueueMessageSender {

	private static final Logger LOGGER = Logger.getLogger(EventsQueueMessageSender.class);

	@Resource(mappedName = Resources.DEFAULT_CONNECTION_FACTORY)
	private ConnectionFactory factory;

	@Resource(mappedName = Resources.EVENTS_QUEUE)
	private Queue queue;

	/**
	 * Sends message to the local events queue with the given event and user name.
	 *
	 * @param event    the event fired by keycloak
	 * @param userName the username of the user that triggered the event. Without the tenant identifier.
	 */
	public void sendMessage(Event event, String userName) {
		try (JMSContext context = factory.createContext()) {
			Message message = context.createMessage();

			String tenantId = getTenantId(event);
			String username = buildUsername(userName, tenantId);
			String eventType = event.getType().name();

			message.setStringProperty(CommunicationConstants.TENANT_ID_KEY, tenantId);
			message.setStringProperty(CommunicationConstants.AUTHENTICATED_USER_KEY, username);
			message.setStringProperty(CommunicationConstants.EVENT_TYPE_KEY, eventType);
			message.setLongProperty(CommunicationConstants.EVENT_TIMESTAMP_KEY, event.getTime());

			LOGGER.debugf("Sending message to the queue: %s - %s - %s - %d", tenantId, username, eventType,
					event.getTime());

			context.createProducer().send(queue, message);
		} catch (JMSException e) {
			throw new JmsEventListenerException("Failed to send message", e);
		}
	}

	private String getTenantId(Event event) {
		String realmId = event.getRealmId();
		if ("master".equals(realmId)) {
			return "system.tenant";
		}
		return realmId;
	}

	private String buildUsername(String userName, String tenantId) {
		return userName + "@" + tenantId;
	}

}
