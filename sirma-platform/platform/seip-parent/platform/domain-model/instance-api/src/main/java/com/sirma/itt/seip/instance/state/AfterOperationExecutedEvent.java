package com.sirma.itt.seip.instance.state;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Event fired to notify that a concrete operation on an instance is executed.
 * Used to trigger an instance state change check.
 *
 * @author BBonev
 */
@Documentation("Event fired to notify that a concrete operation on an instance is executed. Used to trigger an instance state change check.")
public class AfterOperationExecutedEvent extends AbstractOperationExecutedEvent {

	/**
	 * Instantiates a new operation executed event.
	 *
	 * @param operation
	 *            the operation
	 * @param target
	 *            the target
	 */
	public AfterOperationExecutedEvent(Operation operation, Instance target) {
		super(operation, target);
	}

	@Override
	public String toString() {
		return getEventInformation().insert(0, "AfterOperationExecutedEvent [operation=").toString();
	}

}
