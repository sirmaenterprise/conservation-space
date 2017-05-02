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
@SuppressWarnings("unchecked")
@Documentation("Event fired before an instance is moved to a new parent. "
		+ "<br> <b>NOTE</b>: after constructing the next event phase the "
		+ "caller needs to set the correct new {@link Instance} before firing the event.")
public class BeforeInstanceMoveEvent extends AbstractInstanceTwoPhaseEvent<Instance, AfterInstanceMoveEvent> {

	/** The source instance. */
	private final Instance sourceInstance;
	/** The target instance. */
	private final Instance targetInstance;

	/**
	 * Instantiates a new before instance move event.
	 *
	 * @param oldInstance
	 *            the old instance
	 * @param sourceInstance
	 *            the source instance from which the instance is moved
	 * @param targetInstance
	 *            the target instance where instance is moved
	 */
	public BeforeInstanceMoveEvent(Instance oldInstance, Instance sourceInstance, Instance targetInstance) {
		super(oldInstance);
		this.sourceInstance = sourceInstance;
		this.targetInstance = targetInstance;
	}

	@Override
	protected AfterInstanceMoveEvent createNextEvent() {
		return new AfterInstanceMoveEvent(getInstance(), getSourceInstance(), getTargetInstance());
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
