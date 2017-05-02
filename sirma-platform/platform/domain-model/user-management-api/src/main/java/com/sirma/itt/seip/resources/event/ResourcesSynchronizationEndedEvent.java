package com.sirma.itt.seip.resources.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.resources.ResourceType;

/**
 * ResourcesSynchronizationStartedEvent is fired on synchronization ended
 *
 * @author bbanchev
 */
@Documentation("Event fired when resource synchronization is just ended. Invoked per ResourceType.")
public class ResourcesSynchronizationEndedEvent implements EmfEvent {

	/** The resource type. */
	private ResourceType resourceType;

	/**
	 * Instantiates a new resources synchronization started event.
	 *
	 * @param resourceType
	 *            the resource type event occured on
	 */
	public ResourcesSynchronizationEndedEvent(ResourceType resourceType) {
		super();
		this.resourceType = resourceType;
	}

	/**
	 * Gets the resource type event occured on
	 *
	 * @return the resource type
	 */
	public ResourceType getResourceType() {
		return resourceType;
	}
}
