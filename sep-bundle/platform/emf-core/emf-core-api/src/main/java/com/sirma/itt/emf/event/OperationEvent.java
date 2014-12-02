package com.sirma.itt.emf.event;

/**
 * Event that provides an operation identifier. These events can be used for instance state
 * management
 * 
 * @author BBonev
 */
public interface OperationEvent extends EmfEvent {

	/**
	 * Gets the operation id that this event corresponds to.
	 * 
	 * @return the operation id
	 */
	String getOperationId();

}
