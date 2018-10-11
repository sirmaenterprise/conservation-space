package com.sirma.itt.seip.domain.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * Event that contains operation ID and the affected instance.
 *
 * @author Mihail Radkov
 */
@Documentation("Event fired to notify that something related to an instance has occurred.")
public class AuditableEvent extends AbstractInstanceEvent<Instance>implements OperationEvent {

	private String operationID;

	/**
	 * Class constructor. Sets the following:
	 *
	 * @param instance
	 *            - the affected instance
	 * @param operationID
	 *            - the operation's ID
	 */
	public AuditableEvent(Instance instance, String operationID) {
		super(instance);
		this.operationID = operationID;
	}

	@Override
	public String getOperationId() {
		return operationID;
	}

}
