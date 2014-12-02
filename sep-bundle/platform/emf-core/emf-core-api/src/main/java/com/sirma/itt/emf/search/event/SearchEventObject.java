package com.sirma.itt.emf.search.event;

import java.io.Serializable;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * The payload of the search event.
 * 
 * @author BBonev
 */
@Documentation("Event fired before and after search")
public class SearchEventObject implements Serializable, EmfEvent {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -107323004819632207L;

	/** The arguments. */
	private Object arguments;

	/**
	 * Instantiates a new search event object.
	 *
	 * @param arguments
	 *            the arguments
	 */
	public SearchEventObject(Object arguments) {
		this.arguments = arguments;
	}

	/**
	 * Getter method for arguments.
	 *
	 * @return the arguments
	 */
	public Object getArguments() {
		return arguments;
	}

	/**
	 * Setter method for arguments.
	 *
	 * @param arguments the arguments to set
	 */
	public void setArguments(Object arguments) {
		this.arguments = arguments;
	}


}
