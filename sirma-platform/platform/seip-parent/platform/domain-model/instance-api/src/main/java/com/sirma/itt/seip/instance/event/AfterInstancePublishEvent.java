package com.sirma.itt.seip.instance.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.seip.domain.event.OperationEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.TwoPhaseEvent;

/**
 * Event fired after instance has been published and the revision instance has been created before it's save. The event
 * will carry also the operation that triggers the publish.
 *
 * @author BBonev
 * @param <I>
 *            the instance type
 * @param <E>
 *            the next event type
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired after instance has been published and the revision instance has been created before it's save. The event will carry also the operation that triggers the publish.")
public class AfterInstancePublishEvent<I extends Instance, E extends TwoPhaseEvent>
		extends AbstractInstanceTwoPhaseEvent<I, E>implements OperationEvent {

	private String operation;
	private Instance revision;

	/**
	 * Instantiates a new before instance persist event.
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @param revision
	 *            the revision
	 */
	public AfterInstancePublishEvent(I instance, String operation, Instance revision) {
		super(instance);
		this.operation = operation;
		this.setRevision(revision);
	}

	@Override
	protected E createNextEvent() {
		return null;
	}

	@Override
	public String getOperationId() {
		return operation;
	}

	/**
	 * Getter method for revision.
	 *
	 * @return the revision
	 */
	public Instance getRevision() {
		return revision;
	}

	/**
	 * Setter method for revision.
	 *
	 * @param revision
	 *            the revision to set
	 */
	public void setRevision(Instance revision) {
		this.revision = revision;
	}

}
