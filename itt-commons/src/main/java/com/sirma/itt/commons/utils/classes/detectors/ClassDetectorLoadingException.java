/**
 * Copyright (c) 2008 15.12.2008 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.utils.classes.detectors;

import com.sirma.itt.commons.bunch.TwigLoadingException;

/**
 * Thrown if the class detector cannot be loaded.
 * 
 * @author Hristo Iliev
 */
public class ClassDetectorLoadingException extends TwigLoadingException {

    /**
     * Comment for serialVersionUID.
     */
    private static final long serialVersionUID = 2546717872947748490L;

    /**
     * Default constructor.
     */
    public ClassDetectorLoadingException() {
	// Default constructor
    }

    /**
     * Create exception with message.
     * 
     * @param message
     *            {@link String}, the message
     */
    public ClassDetectorLoadingException(String message) {
	super(message);
    }

    /**
     * Create exception with cause.
     * 
     * @param cause
     *            {@link Throwable}, the cause
     */
    public ClassDetectorLoadingException(Throwable cause) {
	super(cause);
    }

    /**
     * Create exception with message and cause.
     * 
     * @param message
     *            {@link String}, the message
     * @param cause
     *            {@link Throwable}, the cause
     */
    public ClassDetectorLoadingException(String message, Throwable cause) {
	super(message, cause);
    }

}
