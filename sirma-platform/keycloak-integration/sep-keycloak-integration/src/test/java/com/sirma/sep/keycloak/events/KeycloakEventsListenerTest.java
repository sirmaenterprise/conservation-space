package com.sirma.sep.keycloak.events;

import static com.sirma.sep.keycloak.events.KeycloakEventsListener.AUTHENTICATED_USER_KEY;
import static com.sirma.sep.keycloak.events.KeycloakEventsListener.EVENT_TIMESTAMP_KEY;
import static com.sirma.sep.keycloak.events.KeycloakEventsListener.EVENT_TYPE_KEY;
import static com.sirma.sep.keycloak.events.KeycloakEventsListener.LOGIN_EVENT;
import static com.sirma.sep.keycloak.events.KeycloakEventsListener.LOGOUT_EVENT;
import static com.sirma.sep.keycloak.events.KeycloakEventsListener.UPDATE_PASSWORD;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import javax.jms.JMSException;
import javax.jms.Message;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.emf.security.event.UserAuthenticatedEvent;
import com.sirma.itt.emf.security.event.UserLogoutEvent;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.event.UserPasswordChangeEvent;
import com.sirma.itt.seip.security.UserStore;
import com.sirmaenterprise.sep.jms.api.JmsMessageInitializer;
import com.sirmaenterprise.sep.jms.api.MessageSender;

/**
 * Tests for {@link KeycloakEventsListener}.
 *
 * @author smustafov
 */
public class KeycloakEventsListenerTest {

	@InjectMocks
	private KeycloakEventsListener listener;

	@Mock
	private MessageSender messageSender;

	@Mock
	private EventService eventService;

	@Mock
	private UserStore userStore;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_FireProperLoginEvent() throws JMSException {
		when(userStore.loadByIdentityId("regularuser@sep.test")).thenReturn(new EmfUser());

		Message message = createMessage(LOGIN_EVENT, "regularuser@sep.test", 0);
		listener.onEvent(message);

		verify(eventService).fire(any(UserAuthenticatedEvent.class));
	}

	@Test
	public void should_FireProperLogoutEvent() throws JMSException {
		when(userStore.loadByIdentityId("user@sep.test")).thenReturn(new EmfUser("user@sep.test"));

		Message message = createMessage(LOGOUT_EVENT, "user@sep.test", 0);
		listener.onEvent(message);

		verify(eventService).fire(any(UserLogoutEvent.class));
	}

	@Test
	public void should_FireProperPasswordChangeEvent() throws JMSException {
		when(userStore.loadByIdentityId("user@sep.test")).thenReturn(new EmfUser("user@sep.test"));

		Message message = createMessage(UPDATE_PASSWORD, "user@sep.test", 0);
		listener.onEvent(message);

		verify(eventService).fire(any(UserPasswordChangeEvent.class));
	}

	@Test
	public void should_SendMessageToDLQ_When_EventTypeNotSupported() throws JMSException {
		when(userStore.loadByIdentityId("user@sep.test")).thenReturn(new EmfUser("user@sep.test"));

		long currentTime = new Date().getTime();
		Message message = createMessage("REGISTER", "user@sep.test", currentTime);
		listener.onEvent(message);

		verifyMessageSend("REGISTER", "user@sep.test", currentTime);
	}

	private void verifyMessageSend(String eventType, String username, long time) throws JMSException {
		ArgumentCaptor<JmsMessageInitializer> argumentCaptor = ArgumentCaptor.forClass(JmsMessageInitializer.class);
		verify(messageSender).send(argumentCaptor.capture());

		Message message = mock(Message.class);
		argumentCaptor.getValue().initialize(message);

		verify(message).setStringProperty(EVENT_TYPE_KEY, eventType);
		verify(message).setStringProperty(AUTHENTICATED_USER_KEY, username);
		verify(message).setLongProperty(EVENT_TIMESTAMP_KEY, time);
	}

	private Message createMessage(String eventType, String username, long time) throws JMSException {
		Message message = mock(Message.class);
		when(message.getStringProperty(EVENT_TYPE_KEY)).thenReturn(eventType);
		when(message.getStringProperty(AUTHENTICATED_USER_KEY)).thenReturn(username);
		when(message.getLongProperty(EVENT_TIMESTAMP_KEY)).thenReturn(time);
		return message;
	}

}
