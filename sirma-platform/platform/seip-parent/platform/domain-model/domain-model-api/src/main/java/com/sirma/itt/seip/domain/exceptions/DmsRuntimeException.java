package com.sirma.itt.seip.domain.exceptions;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Thrown then error occur in the DMS sub system and CMF cannot handle it.
 *
 * @author BBonev
 */
public class DmsRuntimeException extends EmfRuntimeException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -3561071713413456088L;

	/**
	 * Instantiates a new dms runtime exception.
	 */
	public DmsRuntimeException() {
		// nothing to do here
	}

	/**
	 * Instantiates a new dms runtime exception.
	 *
	 * @param arg0
	 *            the arg0
	 */
	public DmsRuntimeException(String arg0) {
		super(arg0);
	}

	/**
	 * Instantiates a new dms runtime exception.
	 *
	 * @param arg0
	 *            the arg0
	 */
	public DmsRuntimeException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * Instantiates a new dms runtime exception.
	 *
	 * @param arg0
	 *            the arg0
	 * @param arg1
	 *            the arg1
	 */
	public DmsRuntimeException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
