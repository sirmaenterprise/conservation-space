package com.sirma.itt.seip.instance.state;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.event.OperationEvent;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Event fired to notify that a concrete operation on an instance is going to be
 * executed. Note that when this event is fired the action shouldn't be
 * executed.
 *
 * @author Ivo Rusev
 */
@Documentation("Event fired to notify BEFORE a concrete operation on an instance is executed. Used to trigger an instance state change check.")
public class BeforeOperationExecutedEvent extends AbstractOperationExecutedEvent implements OperationEvent {

	/**
	 * Instantiates a new BeforeOperationExecutedEvent event.
	 *
	 * @param operation
	 *            the operation
	 * @param target
	 *            the target instance
	 */
	public BeforeOperationExecutedEvent(Operation operation, Instance target) {
		super(operation, target);
	}

	@Override
	public String toString() {
		return getEventInformation().insert(0, "BeforeOperationExecutedEvent [operation=").toString();
	}

	@Override
	protected AfterOperationExecutedEvent createNextEvent() {
		return new AfterOperationExecutedEvent(operation, getInstance());
	}

}
