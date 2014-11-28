/*
 * Created on 09.02.2008 @ 12:34:01
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.translator;

/**
 * Exception is thrown when to UTF engine is given an unsupported value.
 * 
 * @author Hristo Iliev
 * 
 */
public class UnsupportedUnicodeNumberException extends Exception {

    /** Unicode number which is not supported. */
    private int unicodeNumber;

    /**
     * 
     */
    private static final long serialVersionUID = -6515383698054045334L;

    /**
     * @param unicodeNumber
     *                int, Unicode number which is not supported
     * 
     */
    public UnsupportedUnicodeNumberException(int unicodeNumber) {
	this.unicodeNumber = unicodeNumber;
    }

    /**
     * @param unicodeNumber
     *                int, Unicode number which is not supported
     * @param message
     *                message for exception
     */
    public UnsupportedUnicodeNumberException(int unicodeNumber, String message) {
	super(message);
	this.unicodeNumber = unicodeNumber;
    }

    /**
     * @param unicodeNumber
     *                int, Unicode number which is not supported
     * @param cause
     *                throwable which cause this exception
     */
    public UnsupportedUnicodeNumberException(int unicodeNumber, Throwable cause) {
	super(cause);
	this.unicodeNumber = unicodeNumber;
    }

    /**
     * @param unicodeNumber
     *                int, Unicode number which is not supported
     * @param message
     *                message for exception
     * @param cause
     *                throwable which cause this exception
     */
    public UnsupportedUnicodeNumberException(int unicodeNumber, String message,
	    Throwable cause) {
	super(message, cause);
	this.unicodeNumber = unicodeNumber;
    }

    /**
     * @return the unicodeNumber
     */
    public final int getUnicodeNumber() {
	return unicodeNumber;
    }

}
