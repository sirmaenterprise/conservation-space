package com.sirma.itt.emf.exceptions;

/**
 * Thrown on fail of security checks
 *
 * @author BBonev
 */
public class CmfSecurityException extends EmfRuntimeException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -1691177020313131628L;

	/**
	 * Instantiates a new cmf security exception.
	 */
	public CmfSecurityException() {
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
	public CmfSecurityException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * Instantiates a new cmf security exception.
	 *
	 * @param arg0
	 *            the arg0
	 */
	public CmfSecurityException(String arg0) {
		super(arg0);
	}

	/**
	 * Instantiates a new cmf security exception.
	 *
	 * @param arg0
	 *            the arg0
	 */
	public CmfSecurityException(Throwable arg0) {
		super(arg0);
	}

}
