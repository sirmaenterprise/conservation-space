package com.sirma.itt.seip.exception;

/**
 * Thrown on invalid, missing or incomplete application configuration.
 *
 * @author BBonev
 */
public class EmfConfigurationException extends EmfRuntimeException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -5511863199977515463L;

	/**
	 * Instantiates a new cmf configuration exception.
	 */
	public EmfConfigurationException() {
		// nothing to do here
	}

	/**
	 * Instantiates a new cmf configuration exception.
	 *
	 * @param arg0
	 *            the arg0
	 */
	public EmfConfigurationException(String arg0) {
		super(arg0);
	}

	/**
	 * Instantiates a new cmf configuration exception.
	 *
	 * @param arg0
	 *            the arg0
	 */
	public EmfConfigurationException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * Instantiates a new cmf configuration exception.
	 *
	 * @param arg0
	 *            the arg0
	 * @param arg1
	 *            the arg1
	 */
	public EmfConfigurationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
