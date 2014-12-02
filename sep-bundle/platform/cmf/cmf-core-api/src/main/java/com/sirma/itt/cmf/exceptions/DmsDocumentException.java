package com.sirma.itt.cmf.exceptions;

import com.sirma.itt.emf.exceptions.DmsRuntimeException;

/**
 * Thrown when DMS system failed to perform a document operation: upload/delete/update
 *
 * @author BBonev
 */
public class DmsDocumentException extends DmsRuntimeException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 2218620514633532175L;

	/**
	 * Instantiates a new dms document exception.
	 */
	public DmsDocumentException() {
		// nothing to do here
	}

	/**
	 * Instantiates a new dms document exception.
	 *
	 * @param arg0 the arg0
	 */
	public DmsDocumentException(String arg0) {
		super(arg0);
	}

	/**
	 * Instantiates a new dms document exception.
	 *
	 * @param arg0 the arg0
	 */
	public DmsDocumentException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * Instantiates a new dms document exception.
	 *
	 * @param arg0 the arg0
	 * @param arg1 the arg1
	 */
	public DmsDocumentException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
