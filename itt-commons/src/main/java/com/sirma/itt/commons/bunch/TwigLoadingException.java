/**
 * Copyright (c) 2008 15.08.2008 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.bunch;

/**
 * 
 * 
 * @author Hristo Iliev
 */
public class TwigLoadingException extends Exception {

    /**
     * Comment for serialVersionUID.
     */
    private static final long serialVersionUID = 9022993835285130322L;

    /**
     * 
     */
    public TwigLoadingException() {
	// Default constructor
    }

    /**
     * @param message
     *                message for exception
     */
    public TwigLoadingException(String message) {
	super(message);
    }

    /**
     * @param cause
     *                throwable which cause this exception
     */
    public TwigLoadingException(Throwable cause) {
	super(cause);
    }

    /**
     * @param message
     *                message for exception
     * @param cause
     *                throwable which cause this exception
     */
    public TwigLoadingException(String message, Throwable cause) {
	super(message, cause);
    }

}
