package com.sirma.itt.seip.instance.messaging;

/**
 * Properties that can describe specific {@link com.sirma.itt.seip.domain.instance.Instance} in communication between
 * different systems.
 *
 * @author Mihail Radkov
 */
public class InstanceCommunicationConstants {

	public static final String INSTANCE_ID = "instanceId";
	public static final String INSTANCE_VERSION_ID = "instanceVersionId";
	public static final String MIMETYPE = "mimetype";

	private InstanceCommunicationConstants() {
		// Private constructor to hide utility class.
	}
}
