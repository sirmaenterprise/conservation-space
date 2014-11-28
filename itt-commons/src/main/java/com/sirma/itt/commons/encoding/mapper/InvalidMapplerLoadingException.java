/**
 * Copyright (c) 2008 03.09.2008 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.encoding.mapper;

import com.sirma.itt.commons.bunch.TwigLoadingException;

/**
 * 
 * 
 * @author Hristo Iliev
 */
public class InvalidMapplerLoadingException extends TwigLoadingException {

    /** serial version ID. */
    private static final long serialVersionUID = -2031711872397145252L;

    /**
     * 
     */
    public InvalidMapplerLoadingException() {
	// Default constructor
    }

    /**
     * @param message
     *                message for exception
     */
    public InvalidMapplerLoadingException(String message) {
	super(message);
    }

    /**
     * @param cause
     *                throwable which cause this exception
     */
    public InvalidMapplerLoadingException(Throwable cause) {
	super(cause);
    }

    /**
     * @param message
     *                message for exception
     * @param cause
     *                throwable which cause this exception
     */
    public InvalidMapplerLoadingException(String message, Throwable cause) {
	super(message, cause);
    }

}
