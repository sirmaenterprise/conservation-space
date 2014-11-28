/*
 * Created on 12.02.2008 @ 17:40:03
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.invalidhandler;

import com.sirma.itt.commons.bunch.TwigLoadingException;

/**
 * This exception is thrown if the invalid character handler cannot be loaded.
 * 
 * @author Hristo Iliev
 * 
 */
public class InvalidCharacterHandlerLoadingException extends
	TwigLoadingException {

    /** serial version ID. */
    private static final long serialVersionUID = -2031711872397145252L;

    /**
     * Default constructor.
     */
    public InvalidCharacterHandlerLoadingException() {
	// Default constructor
    }

    /**
     * @param message
     *                message for exception
     */
    public InvalidCharacterHandlerLoadingException(String message) {
	super(message);
    }

    /**
     * @param cause
     *                throwable which cause this exception
     */
    public InvalidCharacterHandlerLoadingException(Throwable cause) {
	super(cause);
    }

    /**
     * @param message
     *                message for exception
     * @param cause
     *                throwable which cause this exception
     */
    public InvalidCharacterHandlerLoadingException(String message,
	    Throwable cause) {
	super(message, cause);
    }

}
