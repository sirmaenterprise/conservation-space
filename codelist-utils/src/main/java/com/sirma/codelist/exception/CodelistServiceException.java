package com.sirma.codelist.exception;


/**
 * Runtime exception thrown to indicate that an error 
 * has occurred while contacting the back-end data service
 * 
 * @author Valeri.Tishev
 *
 */
public class CodelistServiceException extends CodelistException {
	
	/**
	 * Universal version identifier
	 */
	private static final long serialVersionUID = 6328053871775833132L;

	/**
     * Constructs a new runtime exception 
     */
	public CodelistServiceException() {
		super();
	}

	/**
	 * Constructs a new runtime exception with the specified 
	 * detail message
	 * 
	 * @param message the detail message
	 */
	public CodelistServiceException(String message) {
		super(message);
	}

	/**
	 * Constructs a new runtime exception with the specified 
	 * detail message and cause
	 * 
	 * @param message the detail message
	 * @param cause the cause (which is saved for later retrieval by the
	 *         {@link #getCause()} method).  (A <tt>null</tt> value is
	 *         permitted, and indicates that the cause is nonexistent or
	 *         unknown.)
	 */
	public CodelistServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new runtime exception with the specified cause 
	 * 
	 * @param cause the cause (which is saved for later retrieval by the
	 *         {@link #getCause()} method).  (A <tt>null</tt> value is
	 *         permitted, and indicates that the cause is nonexistent or
	 *         unknown.)
	 */
	public CodelistServiceException(Throwable cause) {
		super(cause);
	}

}
