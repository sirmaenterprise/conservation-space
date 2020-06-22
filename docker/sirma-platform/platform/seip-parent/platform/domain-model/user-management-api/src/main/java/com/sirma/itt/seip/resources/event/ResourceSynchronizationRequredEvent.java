package com.sirma.itt.seip.resources.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.security.util.AbstractSecureEvent;

/**
 * Event fired when resource synchronization is required. The user could issue forced synchronization if needed.
 *
 * @author BBonev
 */
@Documentation("Event fired when resource synchronization is required. The user could issue forced synchronization if needed.")
public class ResourceSynchronizationRequredEvent extends AbstractSecureEvent {

	private final boolean allowDelete;

	/**
	 * Instantiates a new resource synchronization event.
	 * @param allowDelete
	 *            the allow delete
	 */
	public ResourceSynchronizationRequredEvent(boolean allowDelete) {
		this.allowDelete = allowDelete;
	}

	/**
	 * Checks if is allow delete.
	 *
	 * @return true, if is allow delete
	 */
	public boolean isAllowDelete() {
		return allowDelete;
	}

}
