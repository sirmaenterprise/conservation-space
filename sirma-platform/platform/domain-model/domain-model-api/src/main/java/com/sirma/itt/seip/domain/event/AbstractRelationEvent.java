package com.sirma.itt.seip.domain.event;

import com.sirma.itt.seip.annotation.Documentation;

/**
 * Base event class for events that are fired over a pair of instance object.
 *
 * @param <I>
 *            the instance type of the target
 * @param <I1>
 *            the instance type of the destination
 * @author nvelkov
 */
@Documentation("Base event class for events that are fired over a pair of instance object")
public class AbstractRelationEvent<I, I1> {

	/** The target. */
	private I from;

	/** The destination. */
	private I1 to;

	/**
	 * Instantiates a new abstract relation event.
	 *
	 * @param target
	 *            the target
	 * @param destination
	 *            the destination
	 */
	public AbstractRelationEvent(I target, I1 destination) {
		super();
		this.from = target;
		this.to = destination;
	}

	/**
	 * Gets the from.
	 *
	 * @return the from
	 */
	public I getFrom() {
		return from;
	}

	/**
	 * Sets the from.
	 *
	 * @param from
	 *            the new from
	 */
	public void setFrom(I from) {
		this.from = from;
	}

	/**
	 * Gets the to.
	 *
	 * @return the to
	 */
	public I1 getTo() {
		return to;
	}

	/**
	 * Sets the to.
	 *
	 * @param to
	 *            the new to
	 */
	public void setTo(I1 to) {
		this.to = to;
	}

}
