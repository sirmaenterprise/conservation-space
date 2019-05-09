package com.sirma.sep.keycloak.events.jms;

import static com.sirma.sep.keycloak.events.jms.CommunicationConstants.AUTHENTICATED_USER_KEY;
import static com.sirma.sep.keycloak.events.jms.CommunicationConstants.EVENT_TIMESTAMP_KEY;
import static com.sirma.sep.keycloak.events.jms.CommunicationConstants.EVENT_TYPE_KEY;
import static com.sirma.sep.keycloak.events.jms.CommunicationConstants.TENANT_ID_KEY;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.Queue;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.sep.keycloak.events.exception.JmsEventListenerException;

/**
 * Tests for {@link EventsQueueMessageSender}.
 *
 * @author smustafov
 */
public class EventsQueueMessageSenderTest {

	@InjectMocks
	private EventsQueueMessageSender messageSender;

	@Mock
	private ConnectionFactory factory;

	@Mock
	private Queue queue;

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
	public void should_ProperlySendMessage() throws JMSException {
		long currentTime = new Date().getTime();
		Event event = createEvent(EventType.LOGIN, "sep.test", currentTime);
		messageSender.sendMessage(event, "regularuser");

		verifyMessage("sep.test", "regularuser@sep.test", EventType.LOGIN, currentTime);
	}

	@Test
	public void should_SetProperTenant_When_EventForMasterRealm() throws JMSException {
		Event event = createEvent(EventType.LOGOUT, "master", 54312);
		messageSender.sendMessage(event, "systemadmin");

		verifyMessage("system.tenant", "systemadmin@system.tenant", EventType.LOGOUT, 54312);
	}

	@Test(expected = JmsEventListenerException.class)
	public void should_ThrowException_When_MessageBuildingFails() throws JMSException {
		doThrow(new JMSException("test")).when(message).setStringProperty(any(), any());

		Event event = createEvent(EventType.LOGOUT, "master", 54312);
		messageSender.sendMessage(event, "systemadmin");
	}

	private Event createEvent(EventType eventType, String realmId, long time) {
		Event event = new Event();
		event.setType(eventType);
		event.setRealmId(realmId);
		event.setTime(time);
		return event;
	}

	private void verifyMessage(String tenant, String username, EventType eventType, long time) throws JMSException {
		verify(jmsProducer).send(queue, message);
		verify(message).setStringProperty(TENANT_ID_KEY, tenant);
		verify(message).setStringProperty(AUTHENTICATED_USER_KEY, username);
		verify(message).setStringProperty(EVENT_TYPE_KEY, eventType.name());
		verify(message).setLongProperty(EVENT_TIMESTAMP_KEY, time);
	}

	private void mockJms() {
		when(factory.createContext()).thenReturn(jmsContext);
		when(jmsContext.createMessage()).thenReturn(message);
		when(jmsContext.createProducer()).thenReturn(jmsProducer);
	}

}
