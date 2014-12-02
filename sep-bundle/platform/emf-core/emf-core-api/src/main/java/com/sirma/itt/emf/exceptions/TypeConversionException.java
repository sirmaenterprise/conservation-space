package com.sirma.itt.emf.exceptions;

/**
 * Base Exception of Type Converter Exceptions.
 * 
 * @authorBBonev
 */
public class TypeConversionException extends RuntimeException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -5982905519096578067L;

	/**
	 * Instantiates a new type conversion exception.
	 *
	 * @param msg the msg
	 */
	public TypeConversionException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a new type conversion exception.
	 *
	 * @param msg the msg
	 * @param cause the cause
	 */
	public TypeConversionException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
