package com.sirma.itt.seip.resources.security;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Thrown on fail of security checks
 *
 * @author BBonev
 */
public class SecurityException extends EmfRuntimeException {

	/**
	 * Instantiates a new cmf security exception.
	 */
	public SecurityException() {
		super();
	}

	/**
	 * Instantiates a new cmf security exception.
	 *
	 * @param arg0
	 *            the arg0
	 * @param arg1
	 *            the arg1
	 */
	public SecurityException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * Instantiates a new cmf security exception.
	 *
	 * @param arg0
	 *            the arg0
	 */
	public SecurityException(String arg0) {
		super(arg0);
	}

	/**
	 * Instantiates a new cmf security exception.
	 *
	 * @param arg0
	 *            the arg0
	 */
	public SecurityException(Throwable arg0) {
		super(arg0);
	}

}
