package com.sirma.sep.keycloak.events;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import com.sirma.sep.keycloak.events.exception.JmsEventListenerException;
import com.sirma.sep.keycloak.events.jms.EventsQueueMessageSender;

/**
 * Factory that builds and provides {@link JmsEventListenerProvider}.
 * <p>
 * This factory accepts configurations for allowed events and allowed clients. These configurations are passed in the
 * standalone.xml
 *
 * @author smustafov
 */
public class JmsEventListenerProviderFactory implements EventListenerProviderFactory {

	private static final Logger LOGGER = Logger.getLogger(JmsEventListenerProviderFactory.class);

	static final String PROVIDER_JNDI_NAME =
			"java:global/jms-events-listener/" + JmsEventListenerProvider.class.getSimpleName();
	static final String MESSAGE_SENDER_JNDI_NAME =
			"java:global/jms-events-listener/" + EventsQueueMessageSender.class.getSimpleName();

	static final String PROVIDER_ID = "jms";
	static final String INCLUDE_EVENTS = "include-events";
	static final String INCLUDE_CLIENTS = "include-clients";

	private Set<EventType> eventTypes;
	private Set<String> clients;
	private EventsQueueMessageSender messageSender;
	private JmsEventListenerProvider provider;

	@Override
	public void init(Config.Scope config) {
		initAllowedEventTypes(config);
		initAllowedClients(config);

		LOGGER.infof("Initialized jms event provider with event types: %s and clients: %s", eventTypes, clients);
	}

	private void initAllowedEventTypes(Config.Scope config) {
		eventTypes = new HashSet<>();

		String eventsConfigValue = config.get(INCLUDE_EVENTS);
		if (eventsConfigValue != null && !eventsConfigValue.trim().isEmpty()) {
			String[] events = eventsConfigValue.split(",");
			for (String eventName : events) {
				eventTypes.add(EventType.valueOf(eventName));
			}
		}
	}

	private void initAllowedClients(Config.Scope config) {
		clients = new HashSet<>();

		String clientsConfigValue = config.get(INCLUDE_CLIENTS);
		if (clientsConfigValue != null && !clientsConfigValue.trim().isEmpty()) {
			String[] configuredClients = clientsConfigValue.split(",");
			clients.addAll(Arrays.asList(configuredClients));
		}
	}

	@Override
	public EventListenerProvider create(KeycloakSession session) {
		if (provider == null) {
			try {
				provider = InitialContext.doLookup(PROVIDER_JNDI_NAME);
				LOGGER.infof("Performed JNDI lookup of %s", PROVIDER_JNDI_NAME);

				if (messageSender == null) {
					messageSender = InitialContext.doLookup(MESSAGE_SENDER_JNDI_NAME);
					LOGGER.infof("Performed JNDI lookup of %s", MESSAGE_SENDER_JNDI_NAME);
				}
			} catch (NamingException e) {
				throw new JmsEventListenerException("Failed to lookup JmsEventListenerProvider", e);
			}
		}

		// set these here as for different sessions the state could be different
		provider.setUserProvider(session.users());
		provider.setRealmProvider(session.realms());
		provider.setEventTypes(eventTypes);
		provider.setClients(clients);
		provider.setMessageSender(messageSender);
		return provider;
	}

	@Override
	public void postInit(KeycloakSessionFactory factory) {
		// not needed
	}

	@Override
	public void close() {
		// not needed
	}

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	public Set<EventType> getEventTypes() {
		return eventTypes;
	}

	public Set<String> getClients() {
		return clients;
	}

}
