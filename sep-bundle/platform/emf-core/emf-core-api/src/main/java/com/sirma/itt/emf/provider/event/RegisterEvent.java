package com.sirma.itt.emf.provider.event;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event object that will be fired when the registry provider register a new
 * object.
 *
 * @author BBonev
 */
@Documentation("Event object that will be fired when the registry provider register a new object.")
public class RegisterEvent implements EmfEvent {

	/** The payload. */
	private Object payload;

	/**
	 * Instantiates a new register event.
	 */
	public RegisterEvent() {
		// nothing to do here
	}

	/**
	 * Instantiates a new register event.
	 *
	 * @param payload
	 *            the payload
	 */
	public RegisterEvent(Object payload) {
		this.payload = payload;
	}

	/**
	 * Getter method for payload.
	 *
	 * @return the payload
	 */
	public Object getPayload() {
		return payload;
	}

	/**
	 * Setter method for payload.
	 *
	 * @param payload
	 *            the payload to set
	 */
	public void setPayload(Object payload) {
		this.payload = payload;
	}

}
