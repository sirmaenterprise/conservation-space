package com.sirmaenterprise.sep.bpm.camunda.schedule;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.state.AbstractOperationExecutedEvent;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Wrapper event for {@link com.sirma.itt.seip.event.EmfEvent}, used for bpm schedule to wrap corner cases like {@link com.sirma.itt.seip.instance.relation.LinkAddedEvent}.
 *
 * @author hlungov
 */
public class BPMScheduleWrapperEvent extends AbstractOperationExecutedEvent {

	/**
	 * Constructor.
	 *
	 * @param operation
	 * 		the operation for execution
	 * @param target
	 * 		instance for which operation is executed
	 */
	public BPMScheduleWrapperEvent(Operation operation, Instance target) {
		super(operation, target);
	}

	@Override
	public String toString() {
		return getEventInformation().insert(0, this.getClass().getName() +" [operation=").toString();
	}

}
