package com.sirma.sep.keycloak.events.jms;

import static com.sirma.sep.keycloak.events.jms.CommunicationConstants.AUTHENTICATED_USER_KEY;
import static com.sirma.sep.keycloak.events.jms.CommunicationConstants.EVENT_TIMESTAMP_KEY;
import static com.sirma.sep.keycloak.events.jms.CommunicationConstants.EVENT_TYPE_KEY;
import static com.sirma.sep.keycloak.events.jms.CommunicationConstants.TENANT_ID_KEY;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;

import org.jboss.logging.Logger;

import com.sirma.sep.keycloak.events.exception.JmsEventListenerException;

/**
 * Listens for messages in the local events queue and forwards them to the remote topic.
 *
 * @author smustafov
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = Resources.DESTINATION_LOOKUP_KEY, propertyValue = Resources.EVENTS_QUEUE),
		@ActivationConfigProperty(propertyName = Resources.DESTINATION_TYPE_KEY, propertyValue = Resources.QUEUE), })
public class EventsQueueMessageListener implements MessageListener {

	private static final Logger LOGGER = Logger.getLogger(EventsQueueMessageListener.class);

	@Resource(mappedName = Resources.REMOTE_CONNECTION_FACTORY)
	private ConnectionFactory factory;

	@Resource(mappedName = Resources.REMOTE_EVENTS_TOPIC)
	private Topic topic;

	@Override
	public void onMessage(Message queueMessage) {
		try (JMSContext context = factory.createContext()) {
			Message topicMessage = context.createMessage();

			copyMessage(topicMessage, queueMessage);

			LOGGER.infof("Sending message to remote topic: tenant=%s, user=%s, event=%s, time=%d",
					topicMessage.getStringProperty(TENANT_ID_KEY),
					topicMessage.getStringProperty(AUTHENTICATED_USER_KEY),
					topicMessage.getStringProperty(EVENT_TYPE_KEY), topicMessage.getLongProperty(EVENT_TIMESTAMP_KEY));

			context.createProducer().send(topic, topicMessage);
		} catch (JMSException e) {
			throw new JmsEventListenerException("Failed to send message", e);
		}
	}

	private void copyMessage(Message topicMessage, Message queueMessage) throws JMSException {
		topicMessage.setStringProperty(TENANT_ID_KEY, queueMessage.getStringProperty(TENANT_ID_KEY));
		topicMessage.setStringProperty(AUTHENTICATED_USER_KEY, queueMessage.getStringProperty(AUTHENTICATED_USER_KEY));
		topicMessage.setStringProperty(EVENT_TYPE_KEY, queueMessage.getStringProperty(EVENT_TYPE_KEY));
		topicMessage.setLongProperty(EVENT_TIMESTAMP_KEY, queueMessage.getLongProperty(EVENT_TIMESTAMP_KEY));
	}

}
