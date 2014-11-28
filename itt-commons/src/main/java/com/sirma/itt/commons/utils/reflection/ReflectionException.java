/**
 * Copyright (c) 2009 04.01.2009 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.utils.reflection;

/**
 * Thrown if there is an error due to reflection method.
 * 
 * @author Hristo Iliev
 */
public class ReflectionException extends RuntimeException {

    /**
     * Comment for serialVersionUID.
     */
    private static final long serialVersionUID = 551489257460411400L;

    /**
     * Default constructor
     */
    public ReflectionException() {
	// Default constructor
    }

    /**
     * Initialize the exception with message.
     * 
     * @param message
     *            {@link String}, the message
     */
    public ReflectionException(String message) {
	super(message);
    }

    /**
     * Initialize the exception with cause.
     * 
     * @param cause
     *            {@link Throwable}, the cause
     */
    public ReflectionException(Throwable cause) {
	super(cause);
    }

    /**
     * Initialize the exception with message and cause.
     * 
     * @param message
     *            {@link String}, the message
     * @param cause
     *            {@link Throwable}, the cause
     */
    public ReflectionException(String message, Throwable cause) {
	super(message, cause);
    }

}
