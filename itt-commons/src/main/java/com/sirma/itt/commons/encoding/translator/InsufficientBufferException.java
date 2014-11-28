/*
 * Created on 09.02.2008 @ 12:43:53
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.translator;

/**
 * Exception is thrown when the buffer is not enough to store the result.
 * 
 * @author Hristo Iliev
 * 
 */
public class InsufficientBufferException extends Exception {

    /** serial version ID. */
    private static final long serialVersionUID = -1015412219292517752L;

    /**
     * 
     */
    public InsufficientBufferException() {
	// Default constructor
    }

    /**
     * @param message
     *                message for exception
     */
    public InsufficientBufferException(String message) {
	super(message);
    }

    /**
     * @param cause
     *                throwable which cause this exception
     */
    public InsufficientBufferException(Throwable cause) {
	super(cause);
    }

    /**
     * @param message
     *                message for exception
     * @param cause
     *                throwable which cause this exception
     */
    public InsufficientBufferException(String message, Throwable cause) {
	super(message, cause);
    }

}
