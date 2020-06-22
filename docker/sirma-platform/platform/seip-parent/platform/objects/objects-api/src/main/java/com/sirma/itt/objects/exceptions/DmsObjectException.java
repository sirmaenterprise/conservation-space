package com.sirma.itt.objects.exceptions;

import com.sirma.itt.seip.domain.exceptions.DmsRuntimeException;

/**
 * Thrown when DMS system fail to complete object operation: create/update.
 *
 * @author BBonev
 */
public class DmsObjectException extends DmsRuntimeException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -8571330981699197662L;

	/**
	 * Instantiates a new dms object exception.
	 */
	public DmsObjectException() {
		// nothing to do here
	}

	/**
	 * Instantiates a new dms object exception.
	 *
	 * @param arg0
	 *            the arg0
	 */
	public DmsObjectException(String arg0) {
		super(arg0);
	}

	/**
	 * Instantiates a new dms object exception.
	 *
	 * @param arg0
	 *            the arg0
	 */
	public DmsObjectException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * Instantiates a new dms object exception.
	 *
	 * @param arg0
	 *            the arg0
	 * @param arg1
	 *            the arg1
	 */
	public DmsObjectException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
