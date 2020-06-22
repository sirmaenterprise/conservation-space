package com.sirma.itt.seip.instance.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.event.OperationEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * Event fire when detach action is performed on the target instance. This means that the provided child instance has
 * been removed as a child of the target instance.
 *
 * @param <I>
 *            the target instance type
 * @author BBonev
 */
@Documentation("Event fire when detach action is performed on the target instance. This means that the provided child instance has been removed as a child of the target instance.")
public class InstanceDetachedEvent<I extends Instance> extends AbstractInstanceEvent<I>implements OperationEvent {

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
	public InstanceDetachedEvent(I target, Instance child) {
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

	/**
	 * Getter method for operationId.
	 *
	 * @return the operationId
	 */
	@Override
	public String getOperationId() {
		return operationId;
	}

	/**
	 * Setter method for operationId.
	 *
	 * @param operationId
	 *            the operationId to set
	 */
	public void setOperationId(String operationId) {
		this.operationId = operationId;
	}
}
