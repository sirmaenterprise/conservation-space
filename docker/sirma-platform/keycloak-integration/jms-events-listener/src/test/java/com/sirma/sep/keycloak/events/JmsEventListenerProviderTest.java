package com.sirma.sep.keycloak.events;

import static com.sirma.sep.keycloak.events.JmsEventListenerProvider.USERNAME_EVENT_DETAIL_KEY;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.sep.keycloak.events.jms.EventsQueueMessageSender;

/**
 * Tests for {@link JmsEventListenerProvider}.
 *
 * @author smustafov
 */
public class JmsEventListenerProviderTest {

	private JmsEventListenerProvider provider;

	@Mock
	private EventsQueueMessageSender messageSender;

	@Mock
	private UserProvider userProvider;

	@Mock
	private RealmProvider realmProvider;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
		provider = new JmsEventListenerProvider();
		provider.setMessageSender(messageSender);
	}

	@Test
	public void should_ProperlyInvokeMessageSender_WithAllowedEventAndClient() {
		setEventTypes(EventType.LOGIN, EventType.LOGOUT);
		setClients("sep-ui");

		Event event = createEvent(EventType.LOGIN, "sep-ui", "regularuser", null);
		provider.onEvent(event);

		verify(messageSender).sendMessage(event, "regularuser");
	}

	@Test
	public void should_FetchUsername_When_MissingInEvent() {
		setEventTypes(EventType.LOGIN, EventType.LOGOUT);
		setClients("sep-ui");
		mockUserProvider("1234", "regularuser", "sep.test");

		Event event = createEvent(EventType.LOGIN, "sep-ui", null, "1234");
		provider.onEvent(event);

		verify(messageSender).sendMessage(event, "regularuser");
	}

	@Test
	public void should_SendNullForUsername_When_NoUserAssociatedWithEvent() {
		setEventTypes(EventType.LOGIN_ERROR);
		setClients("sep-ui");

		Event event = createEvent(EventType.LOGIN_ERROR, "sep-ui", null, null);
		provider.onEvent(event);

		verify(messageSender).sendMessage(event, null);
	}

	@Test
	public void should_SkipEvent_When_EventNotAllowed() {
		setEventTypes(EventType.LOGIN, EventType.LOGOUT);
		setClients("sep-ui");

		Event event = createEvent(EventType.REGISTER, "sep-ui", "regularuser", "1234");
		provider.onEvent(event);

		verify(messageSender, never()).sendMessage(any(), any());
	}

	@Test
	public void should_SkipEvent_When_ClientNotAllowed() {
		setEventTypes(EventType.LOGIN, EventType.LOGOUT);
		setClients("sep-ui");

		Event event = createEvent(EventType.LOGIN, "admin-console", "regularuser", "1234");
		provider.onEvent(event);

		verify(messageSender, never()).sendMessage(any(), any());
	}

	@Test
	public void should_ProcessEvent_When_NoClient() {
		setEventTypes(EventType.LOGIN, EventType.LOGOUT);
		setClients("sep-ui");

		Event event = createEvent(EventType.LOGOUT, null, "regularuser", "1234");
		provider.onEvent(event);

		verify(messageSender).sendMessage(event, "regularuser");
	}

	private Event createEvent(EventType eventType, String clientId, String username, String userDbIDd) {
		Event event = new Event();
		event.setType(eventType);
		event.setClientId(clientId);
		event.setUserId(userDbIDd);
		Map<String, String> details = new HashMap<>();
		details.put(USERNAME_EVENT_DETAIL_KEY, username);
		event.setDetails(details);
		event.setRealmId("sep.test");
		return event;
	}

	private void mockUserProvider(String userDbId, String username, String realm) {
		RealmModel realmModel = mock(RealmModel.class);
		when(realmProvider.getRealm(realm)).thenReturn(realmModel);

		UserModel userModel = mock(UserModel.class);
		when(userModel.getUsername()).thenReturn(username);
		when(userProvider.getUserById(userDbId, realmModel)).thenReturn(userModel);

		provider.setUserProvider(userProvider);
		provider.setRealmProvider(realmProvider);
	}

	private void setEventTypes(EventType... eventTypes) {
		provider.setEventTypes(new HashSet<>(Arrays.asList(eventTypes)));
	}

	private void setClients(String... clients) {
		provider.setClients(new HashSet<>(Arrays.asList(clients)));
	}

}
