package com.sirma.itt.seip.instance.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.TwoPhaseEvent;

/**
 * Event fired after an instance has been moved to a new destination. The instance has been attached to its new parent
 * before the event has been fired.
 *
 * @author BBonev
 */
@Documentation("Event fired after an instance has been moved to a new destination. "
		+ "The instance has been attached to its new parent before the event has been fired.")
public class AfterInstanceMoveEvent extends AbstractInstanceTwoPhaseEvent<Instance, TwoPhaseEvent> {

	private final Instance sourceInstance;

	private final Instance targetInstance;

	/**
	 * Instantiates a new after instance move event.
	 *
	 * @param instance which is moved
	 * @param sourceInstance the source instance from which is moved
	 * @param targetInstance the target instance to which is moved
	 */
	public AfterInstanceMoveEvent(Instance instance, Instance sourceInstance, Instance targetInstance) {
		super(instance);
		this.sourceInstance = sourceInstance;
		this.targetInstance = targetInstance;
	}

	@Override
	protected TwoPhaseEvent createNextEvent() {
		return null;
	}

	public Instance getSourceInstance() {
		return sourceInstance;
	}

	public Instance getTargetInstance() {
		return targetInstance;
	}
}