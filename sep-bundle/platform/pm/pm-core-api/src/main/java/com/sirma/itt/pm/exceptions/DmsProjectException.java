package com.sirma.itt.pm.exceptions;

import com.sirma.itt.emf.exceptions.DmsRuntimeException;

/**
 * Thrown when DMS system fail to complete project operation: create/update.
 * 
 * @author BBonev
 */
public class DmsProjectException extends DmsRuntimeException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -8571330981699197662L;

	/**
	 * Instantiates a new dms project exception.
	 */
	public DmsProjectException() {
		// nothing to do here
	}

	/**
	 * Instantiates a new dms project exception.
	 * 
	 * @param arg0
	 *            the arg0
	 */
	public DmsProjectException(String arg0) {
		super(arg0);
	}

	/**
	 * Instantiates a new dms project exception.
	 * 
	 * @param arg0
	 *            the arg0
	 */
	public DmsProjectException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * Instantiates a new dms case exception.
	 *
	 * @param arg0 the arg0
	 * @param arg1 the arg1
	 */
	public DmsProjectException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
