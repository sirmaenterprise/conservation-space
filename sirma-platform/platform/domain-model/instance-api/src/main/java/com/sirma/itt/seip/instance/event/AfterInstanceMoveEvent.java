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

	/** The source instance. */
	private final Instance sourceInstance;
	/** The target instance. */
	private final Instance targetInstance;

	/**
	 * Instantiates a new after instance move event.
	 *
	 * @param instance
	 *            is moved instance
	 * @param sourceInstance
	 *            the source instance
	 * @param targetInstance
	 *            the target instance
	 */
	public AfterInstanceMoveEvent(Instance instance, Instance sourceInstance, Instance targetInstance) {
		super(instance);
		this.sourceInstance = sourceInstance;
		this.targetInstance = targetInstance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TwoPhaseEvent createNextEvent() {
		return null;
	}

	/**
	 * Getter method for sourceInstance.
	 *
	 * @return the sourceInstance
	 */
	public Instance getSourceInstance() {
		return sourceInstance;
	}

	/**
	 * Getter method for targetInstance.
	 *
	 * @return the targetInstance
	 */
	public Instance getTargetInstance() {
		return targetInstance;
	}

}
