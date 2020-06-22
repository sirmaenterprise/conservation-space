package com.sirma.sep.keycloak.events.jms;

/**
 * Communication constants used for exchanging messages in the provider.
 *
 * @author smustafov
 */
class CommunicationConstants {

	/**
	 * Key used to specify the tenant identifier in the messages.
	 */
	static final String TENANT_ID_KEY = "tenant_id";

	/**
	 * Key used to specify the type of the event that occurred in the messages.
	 */
	static final String EVENT_TYPE_KEY = "event_type";

	/**
	 * Key used to specify the timestamp when the event happened.
	 */
	static final String EVENT_TIMESTAMP_KEY = "event_timestamp";

	/**
	 * Key used to specify the authenticated user for the message. In format: username@tenantId.
	 */
	static final String AUTHENTICATED_USER_KEY = "authenticated_user";

	private CommunicationConstants() {
		// constants only
	}
}
