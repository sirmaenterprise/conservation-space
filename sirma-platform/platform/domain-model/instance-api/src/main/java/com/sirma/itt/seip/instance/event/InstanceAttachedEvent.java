package com.sirma.itt.seip.instance.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.event.OperationEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * Event fire when attach action is performed on the target instance. This means that an attach of the provided child
 * instance to the target instance is performed.
 *
 * @param <I>
 *            the target instance type
 * @author BBonev
 */
@Documentation("Event fire when attach action is performed on the target instance. This means that an attach of the provided child instance to the target instance is performed.")
public class InstanceAttachedEvent<I extends Instance> extends AbstractInstanceEvent<I>implements OperationEvent {

	/** The child. */
	private final Instance child;
	private String operationId;

	/**
	 * Instantiates a new instance attached event.
	 *
	 * @param target
	 *            the target
	 * @param child
	 *            the child
	 */
	public InstanceAttachedEvent(I target, Instance child) {
		super(target);
		this.child = child;
	}

	/**
	 * Getter method for child.
	 *
	 * @return the child
	 */
	public Instance getChild() {
		return child;
	}

	@Override
	public String getOperationId() {
		return operationId;
	}

	/**
	 * Setter method for operation.
	 *
	 * @param operation
	 *            the operation to set
	 */
	public void setOperationId(String operation) {
		this.operationId = operation;
	}
}
