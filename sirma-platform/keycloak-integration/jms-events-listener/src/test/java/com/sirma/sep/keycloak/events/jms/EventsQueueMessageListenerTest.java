package com.sirma.sep.keycloak.events.jms;

import static com.sirma.sep.keycloak.events.jms.CommunicationConstants.AUTHENTICATED_USER_KEY;
import static com.sirma.sep.keycloak.events.jms.CommunicationConstants.EVENT_TIMESTAMP_KEY;
import static com.sirma.sep.keycloak.events.jms.CommunicationConstants.EVENT_TYPE_KEY;
import static com.sirma.sep.keycloak.events.jms.CommunicationConstants.TENANT_ID_KEY;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.Topic;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.events.EventType;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.sep.keycloak.events.exception.JmsEventListenerException;

/**
 * Tests for {@link EventsQueueMessageListener}.
 *
 * @author smustafov
 */
public class EventsQueueMessageListenerTest {

	@InjectMocks
	private EventsQueueMessageListener messageListener;

	@Mock
	private ConnectionFactory factory;

	@Mock
	private Topic topic;

	@Mock
	private JMSContext jmsContext;

	@Mock
	private JMSProducer jmsProducer;

	@Mock
	private Message message;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
		mockJms();
	}

	@Test
	public void should_ForwardMessageToTopic() throws JMSException {
		long currentTime = new Date().getTime();
		Message messageFromQueue = mockMessage("sep.test", "regularuser@sep.test", EventType.LOGIN, currentTime);

		messageListener.onMessage(messageFromQueue);

		verify(jmsProducer).send(topic, message);
		verify(message).setStringProperty(TENANT_ID_KEY, "sep.test");
		verify(message).setStringProperty(AUTHENTICATED_USER_KEY, "regularuser@sep.test");
		verify(message).setStringProperty(EVENT_TYPE_KEY, EventType.LOGIN.name());
		verify(message).setLongProperty(EVENT_TIMESTAMP_KEY, currentTime);
	}

	@Test(expected = JmsEventListenerException.class)
	public void should_ThrowException_When_MessageBuildingFails() throws JMSException {
		doThrow(new JMSException("test")).when(message).setStringProperty(any(), any());

		Message messageFromQueue = mockMessage("sep.test", "regularuser@sep.test", EventType.LOGIN, 0);
		messageListener.onMessage(messageFromQueue);
	}

	private Message mockMessage(String tenant, String username, EventType eventType, long time) throws JMSException {
		Message mockMessage = mock(Message.class);
		when(mockMessage.getStringProperty(TENANT_ID_KEY)).thenReturn(tenant);
		when(mockMessage.getStringProperty(AUTHENTICATED_USER_KEY)).thenReturn(username);
		when(mockMessage.getStringProperty(EVENT_TYPE_KEY)).thenReturn(eventType.name());
		when(mockMessage.getLongProperty(EVENT_TIMESTAMP_KEY)).thenReturn(time);
		return mockMessage;
	}

	private void mockJms() {
		when(factory.createContext()).thenReturn(jmsContext);
		when(jmsContext.createMessage()).thenReturn(message);
		when(jmsContext.createProducer()).thenReturn(jmsProducer);
	}

}
