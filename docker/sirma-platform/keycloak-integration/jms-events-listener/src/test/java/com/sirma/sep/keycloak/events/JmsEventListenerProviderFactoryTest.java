package com.sirma.sep.keycloak.events;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.Config;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.sep.keycloak.events.exception.JmsEventListenerException;
import com.sirma.sep.keycloak.events.jms.EventsQueueMessageSender;

/**
 * Tests for {@link JmsEventListenerProviderFactory}.
 *
 * @author smustafov
 */
public class JmsEventListenerProviderFactoryTest {

	private JmsEventListenerProviderFactory factory;

	@Mock
	private Context context;

	@Mock
	private EventsQueueMessageSender messageSender;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
		factory = new JmsEventListenerProviderFactory();
	}

	@Test
	public void init_Should_ProperlyBuildEventsAndClientsConfig() {
		Config.Scope config = mockConfig("LOGIN,LOGOUT", "sep-ui,sep-android");

		factory.init(config);

		assertEventTypes(EventType.LOGIN, EventType.LOGOUT);
		assertClients("sep-ui", "sep-android");
	}

	@Test
	public void init_Should_ProperlyBuildEmptyConfig() {
		Config.Scope config = mockConfig("", " ");

		factory.init(config);

		assertEventTypes();
		assertClients();
	}

	@Test
	public void init_Should_ProperlyBuildNullConfig() {
		Config.Scope config = mockConfig(null, null);

		factory.init(config);

		assertEventTypes();
		assertClients();
	}

	@Test
	public void create_Should_CreateProvider() throws NamingException {
		mockProviderJndiLookup();

		KeycloakSession session = mock(KeycloakSession.class);

		factory.create(session);

		verify(session).users();
		verify(session).realms();
	}

	@Test(expected = JmsEventListenerException.class)
	public void create_Should_ThrowException_When_CannotLookupProvider() throws NamingException {
		mockProviderJndiLookup();
		when(context.lookup(anyString())).thenThrow(new NamingException());

		factory.create(null);
	}

	@Test
	public void should_HaveId() {
		assertEquals(JmsEventListenerProviderFactory.PROVIDER_ID, factory.getId());
	}

	private Config.Scope mockConfig(String events, String clients) {
		Config.Scope config = mock(Config.Scope.class);
		when(config.get(JmsEventListenerProviderFactory.INCLUDE_EVENTS)).thenReturn(events);
		when(config.get(JmsEventListenerProviderFactory.INCLUDE_CLIENTS)).thenReturn(clients);
		return config;
	}

	private void assertEventTypes(EventType... eventTypes) {
		Set<EventType> expectedEventTypes = new HashSet<>(Arrays.asList(eventTypes));
		assertEquals(expectedEventTypes, factory.getEventTypes());
	}

	private void assertClients(String... clients) {
		Set<String> expectedClients = new HashSet<>(Arrays.asList(clients));
		assertEquals(expectedClients, factory.getClients());
	}

	private JmsEventListenerProvider mockProviderJndiLookup() throws NamingException {
		InitialContextFactoryFake.context = context;
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, InitialContextFactoryFake.class.getName());
		JmsEventListenerProvider provider = new JmsEventListenerProvider();
		when(context.lookup(JmsEventListenerProviderFactory.PROVIDER_JNDI_NAME)).thenReturn(provider);
		when(context.lookup(JmsEventListenerProviderFactory.MESSAGE_SENDER_JNDI_NAME)).thenReturn(messageSender);
		return provider;
	}

}
