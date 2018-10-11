package com.sirma.itt.seip.permissions;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.event.OperationEvent;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * The RestorePermissionTrigger is event to start asynch permission restoration
 *
 * @author bbanchev
 */
@Documentation("Event to start asynch permission restoration")
public class RestorePermissionTrigger extends AbstractInstanceEvent<InstanceReference>implements OperationEvent {

	/**
	 * Instantiates a new restore permission trigger.
	 *
	 * @param target
	 *            the target
	 */
	public RestorePermissionTrigger(InstanceReference target) {
		super(target);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOperationId() {
		return "RestorePermissionsEventTrigger";
	}

}
