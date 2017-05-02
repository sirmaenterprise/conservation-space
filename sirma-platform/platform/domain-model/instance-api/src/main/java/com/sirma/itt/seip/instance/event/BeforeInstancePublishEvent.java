package com.sirma.itt.seip.instance.event;

import com.sirma.itt.seip.domain.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.seip.domain.event.OperationEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.TwoPhaseEvent;

/**
 * Event fired before publishing a instance revision. The event carry the operation triggered the publish and the target
 * location. Note the last argument of the constructor is same as the {@link AfterInstancePublishEvent} but it's
 * different in semantics.
 *
 * @author BBonev
 * @param <I>
 *            the instance type
 * @param <E>
 *            the after event type
 */
@SuppressWarnings("unchecked")
public class BeforeInstancePublishEvent<I extends Instance, E extends AfterInstancePublishEvent<I, TwoPhaseEvent>>
		extends AbstractInstanceTwoPhaseEvent<I, E>implements OperationEvent {

	/** The revision. */
	private final Instance revision;
	/** The operation. */
	private final String operation;

	/**
	 * Instantiates a new before instance publish event.
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @param revision
	 *            the revision that will be created
	 */
	public BeforeInstancePublishEvent(I instance, String operation, Instance revision) {
		super(instance);
		this.operation = operation;
		this.revision = revision;
	}

	@Override
	public String getOperationId() {
		return operation;
	}

	@Override
	protected E createNextEvent() {
		return (E) new AfterInstancePublishEvent<Instance, TwoPhaseEvent>(getInstance(), getOperationId(),
				getRevision());
	}

	/**
	 * Getter method for revision.
	 *
	 * @return the revision
	 */
	public Instance getRevision() {
		return revision;
	}
}
