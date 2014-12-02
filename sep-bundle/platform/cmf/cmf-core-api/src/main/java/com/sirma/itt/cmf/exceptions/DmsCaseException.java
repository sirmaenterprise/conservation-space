package com.sirma.itt.cmf.exceptions;

import com.sirma.itt.emf.exceptions.DmsRuntimeException;

/**
 * Thrown when DMS system fail to complete case operation: create/update
 *
 * @author BBonev
 */
public class DmsCaseException extends DmsRuntimeException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -8571330981699197662L;

	/**
	 * Instantiates a new dms case exception.
	 */
	public DmsCaseException() {
		// nothing to do here
	}

	/**
	 * Instantiates a new dms case exception.
	 *
	 * @param arg0 the arg0
	 */
	public DmsCaseException(String arg0) {
		super(arg0);
	}

	/**
	 * Instantiates a new dms case exception.
	 *
	 * @param arg0 the arg0
	 */
	public DmsCaseException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * Instantiates a new dms case exception.
	 *
	 * @param arg0 the arg0
	 * @param arg1 the arg1
	 */
	public DmsCaseException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
