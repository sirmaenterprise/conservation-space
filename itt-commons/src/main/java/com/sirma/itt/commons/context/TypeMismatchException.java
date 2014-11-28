/**
 * Copyright (c) 2008 24.12.2008 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.context;

/**
 * Thrown if the type of the property is not with the same type as expected.
 * 
 * @author Hristo Iliev
 */
public class TypeMismatchException extends RuntimeException {

    /**
     * Comment for serialVersionUID.
     */
    private static final long serialVersionUID = -1620926310966655610L;

    /**
     * @param message
     */
    private TypeMismatchException(String message) {
	super(message);
    }

    /**
     * @param message
     * @param cause
     */
    private TypeMismatchException(String message, Throwable cause) {
	super(message, cause);
    }

    /**
     * Factory method for creating exception for specified value.
     * 
     * @param value
     *            {@link String}, value which type is mismatched
     * @param type
     *            {@link Class}, type which is mismatched
     * @return {@link TypeMismatchException}, constructed exception
     */
    public static TypeMismatchException forValue(String value, Class<?> type) {
	StringBuilder stringBuilder = new StringBuilder();
	stringBuilder.append(value);
	stringBuilder.append(" is not valid "); //$NON-NLS-1$
	stringBuilder.append(type.getName());
	TypeMismatchException exception = new TypeMismatchException(
		stringBuilder.toString());
	return exception;
    }

    /**
     * Factory method for creating exception for specified value.
     * 
     * @param value
     *            {@link String}, value which type is mismatched
     * @param type
     *            {@link Class}, type which is mismatched
     * @param cause
     *            {@link Throwable}, cause of this exception
     * @return {@link TypeMismatchException}, constructed exception
     */
    public static TypeMismatchException forValue(String value, Class<?> type,
	    Throwable cause) {
	TypeMismatchException exception = forValue(value, type);
	exception.initCause(cause);
	return exception;
    }
}
