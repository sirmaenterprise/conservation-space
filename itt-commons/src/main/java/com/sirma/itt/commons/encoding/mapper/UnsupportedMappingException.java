/*
 * Created on 15.02.2008 @ 17:46:47
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.mapper;

/**
 * This exception is thrown if the file which contain the mapping is with syntax
 * which is not supported from the mapper.
 * 
 * @author Hristo Iliev
 * 
 */
public class UnsupportedMappingException extends Exception {

    /** serial version id. */
    private static final long serialVersionUID = -8675278987587285669L;

    /**
     * 
     */
    public UnsupportedMappingException() {
	// Default constructor
    }

    /**
     * @param message
     *                message for exception
     */
    public UnsupportedMappingException(String message) {
	super(message);
    }

    /**
     * @param cause
     *                throwable which cause this exception
     */
    public UnsupportedMappingException(Throwable cause) {
	super(cause);
    }

    /**
     * @param message
     *                message for exception
     * @param cause
     *                throwable which cause this exception
     */
    public UnsupportedMappingException(String message, Throwable cause) {
	super(message, cause);
    }

}
