package com.sirma.cmf.web.digitalsign;

/**
 * DigitalSignException is specific exception thrown when error occurs on document signing.
 * 
 * @author svelikov
 */
public class DigitalSignException extends Exception {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -6408998543506709934L;

	/**
	 * Error codes.
	 */
	public enum Code {
		//
		/** The invalid digital signature. */
		INVALID_DIGITAL_SIGNATURE,
		//
		/** The failed digital signature extraction. */
		FAILED_DIGITAL_SIGNATURE_EXTRACTION,
		//
		/** The authentication failed for ds. */
		AUTHENTICATION_FAILED_FOR_DS
	}

	/**
	 * Error code.
	 */
	private Code code;

	/**
	 * Default constructor.
	 */
	public DigitalSignException() {
		super();
	}

	/**
	 * Constructor for message.
	 * 
	 * @param message
	 *            message
	 */
	public DigitalSignException(String message) {
		super(message);
	}

	/**
	 * Constructor for code.
	 * 
	 * @param code
	 *            code
	 */
	public DigitalSignException(Code code) {
		super();
		this.code = code;
	}

	/**
	 * Constructor for code and message.
	 * 
	 * @param code
	 *            code
	 * @param message
	 *            message
	 */
	public DigitalSignException(Code code, String message) {
		super(message);
		this.code = code;
	}

	/**
	 * Gets the value of the code property.
	 * 
	 * @return the value for the field code.
	 */
	public Code getCode() {
		return code;
	}

}