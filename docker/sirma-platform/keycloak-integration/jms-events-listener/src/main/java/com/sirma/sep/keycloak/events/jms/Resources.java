package com.sirma.sep.keycloak.events.jms;

/**
 * JMS resources constants used for jms resource lookups and configurations.
 *
 * @author smustafov
 */
class Resources {

	static final String EVENTS_QUEUE = "java:/jms.queue.EventsQueue";
	static final String DEFAULT_CONNECTION_FACTORY = "java:jboss/DefaultJMSConnectionFactory";

	static final String REMOTE_EVENTS_TOPIC = "java:jboss/exported/jms.topic.UserEventsTopic";
	static final String REMOTE_CONNECTION_FACTORY = "java:/jms/remoteCF";

	static final String QUEUE = "javax.jms.Queue";
	static final String DESTINATION_LOOKUP_KEY = "destinationLookup";
	static final String DESTINATION_TYPE_KEY = "destinationType";

	private Resources() {
		// constants only
	}

}
