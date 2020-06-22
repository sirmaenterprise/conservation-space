package com.sirma.sep.keycloak.events;

import java.util.Map;
import java.util.Set;

import javax.ejb.Local;
import javax.ejb.Remove;
import javax.ejb.Stateless;

import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;

import com.sirma.sep.keycloak.events.jms.EventsQueueMessageSender;

/**
 * Event listener provider that sends messages to a queue about occurred events.
 * The events are filtered only to allowed events and clients.
 *
 * @author smustafov
 */
@Stateless
@Local(JmsEventListenerProvider.class)
public class JmsEventListenerProvider implements EventListenerProvider {

	private static final Logger LOGGER = Logger.getLogger(JmsEventListenerProvider.class);

	static final String USERNAME_EVENT_DETAIL_KEY = "username";

	private Set<EventType> eventTypes;
	private Set<String> clients;

	private UserProvider userProvider;
	private RealmProvider realmProvider;
	private EventsQueueMessageSender messageSender;

	@Override
	public void onEvent(Event event) {
		if (isApplicable(event)) {
			messageSender.sendMessage(event, getUserName(event));
			LOGGER.debugf("Sent event: %s - %s - %s", event.getClientId(), event.getType().name(), event.getRealmId());
		}
	}

	private boolean isApplicable(Event event) {
		// on logout client id is null, probably because its a single sign out
		return (event.getClientId() == null || isClientAllowed(event.getClientId())) && isEventTypeAllowed(
				event.getType());
	}

	private boolean isClientAllowed(String clientId) {
		return clients.contains(clientId);
	}

	private boolean isEventTypeAllowed(EventType eventType) {
		return eventTypes.contains(eventType);
	}

	private String getUserName(Event event) {
		String username = getUserNameFromEventDetails(event.getDetails());
		if (username != null) {
			// get username from the event if exists
			return username;
		}

		// fallback to retrieving the username by db id
		String userDbId = event.getUserId();
		if (userDbId != null) {
			UserModel userModel = userProvider.getUserById(userDbId, realmProvider.getRealm(event.getRealmId()));
			return userModel.getUsername();
		}

		// user could be null if the event type is error - login error, register error, etc
		return null;
	}

	private static String getUserNameFromEventDetails(Map<String, String> details) {
		if (details != null) {
			return details.get(USERNAME_EVENT_DETAIL_KEY);
		}
		return null;
	}

	@Override
	public void onEvent(AdminEvent event, boolean includeRepresentation) {
		// not needed
	}

	@Remove
	@Override
	public void close() {
		// not needed
	}

	public void setEventTypes(Set<EventType> eventTypes) {
		this.eventTypes = eventTypes;
	}

	public void setClients(Set<String> clients) {
		this.clients = clients;
	}

	public void setUserProvider(UserProvider userProvider) {
		this.userProvider = userProvider;
	}

	public void setRealmProvider(RealmProvider realmProvider) {
		this.realmProvider = realmProvider;
	}

	public void setMessageSender(EventsQueueMessageSender messageSender) {
		this.messageSender = messageSender;
	}
}
