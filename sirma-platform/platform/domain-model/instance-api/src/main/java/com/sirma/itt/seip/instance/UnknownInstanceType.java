package com.sirma.itt.seip.instance;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Exception thrown to indicate missing or invalid instance type when type resolving should return a type.
 *
 * @author BBonev
 */
public class UnknownInstanceType extends EmfRuntimeException {

	private static final long serialVersionUID = 1368586901808178136L;

	/**
	 * Instantiates a new unknown instance type.
	 *
	 * @param message
	 *            the message
	 */
	public UnknownInstanceType(String message) {
		super(message);
	}

}
