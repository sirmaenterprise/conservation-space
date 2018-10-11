package com.sirma.itt.seip.instance.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Event fired before an instance is moved to a new parent. <br>
 * <b>NOTE</b>: after constructing the next event phase the caller needs to set the correct new {@link Instance} before
 * firing the event.
 *
 * @author BBonev
 */
@Documentation("Event fired before an instance is moved to a new parent. "
		+ "<br> <b>NOTE</b>: after constructing the next event phase the "
		+ "caller needs to set the correct new {@link Instance} before firing the event.")
public class BeforeInstanceMoveEvent extends AbstractInstanceTwoPhaseEvent<Instance, AfterInstanceMoveEvent> {

	private final Instance sourceInstance;

	private final Instance targetInstance;

	/**
	 * Instantiates a new before instance move event.
	 *
	 * @param instance the instance which is moved(for which is the operation)
	 * @param sourceInstance the source instance from which the instance is moved
	 * @param targetInstance the target instance where instance is moved
	 */
	public BeforeInstanceMoveEvent(Instance instance, Instance sourceInstance, Instance targetInstance) {
		super(instance);
		this.sourceInstance = sourceInstance;
		this.targetInstance = targetInstance;
	}

	@Override
	protected AfterInstanceMoveEvent createNextEvent() {
		return new AfterInstanceMoveEvent(getInstance(), getSourceInstance(), getTargetInstance());
	}

	public Instance getSourceInstance() {
		return sourceInstance;
	}

	public Instance getTargetInstance() {
		return targetInstance;
	}
}