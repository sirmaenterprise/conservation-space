package com.sirma.itt.emf.event.instance;



/**
 * Enum that lists all common lifecycle events of an instance. Could be used for implementing
 * generic implementations with the {@link com.sirma.itt.emf.instance.dao.InstanceEventProvider}.
 * 
 * @author BBonev
 */
public enum InstanceEventType {

	/** A constant for the create event of an instance. */
	CREATE,

	/**
	 * A constant for the first persist event of an instance. The event is
	 * {@link com.sirma.itt.emf.event.TwoPhaseEvent}
	 */
	FIRST_PERSIST,

	/** A constant for the change event of an instance. */
	CHANGE,

	/** A constant for the persist event of an instance. */
	PERSIST,

	/** A constant for the open event of an instance. */
	OPEN,

	/**
	 * A constant for the delete event of an instance. The event is
	 * {@link com.sirma.itt.emf.event.TwoPhaseEvent}
	 */
	DELETE,

	/**
	 * A constant for the stop/cancel event of an instance. The event is
	 * {@link com.sirma.itt.emf.event.TwoPhaseEvent}
	 */
	STOP,

	/** A constant for the attach event of an instance. */
	ATTACH,

	/** A constant for the detach event of an instance. */
	DETACH;
}
