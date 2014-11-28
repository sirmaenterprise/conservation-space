/*
 * Created on 12.02.2008 @ 12:30:02
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.translator;

/**
 * This exception is thrown if the specified code is not supported by the
 * encoding.
 * 
 * @author Hristo Iliev
 * 
 */
public class UnsupportedCodeException extends Exception {

    /** Unsupported code. */
    private byte[] code;

    /**
     * 
     */
    private static final long serialVersionUID = -5297264715128783863L;

    /**
     * @param code
     *                byte[], code which is not supported
     * 
     */
    public UnsupportedCodeException(byte[] code) {
	this.code = new byte[code.length];
	System.arraycopy(code, 0, this.code, 0, code.length);
    }

    /**
     * @param code
     *                byte[], code which is not supported
     * @param message
     *                message for exception
     */
    public UnsupportedCodeException(byte[] code, String message) {
	super(message);
	this.code = new byte[code.length];
	System.arraycopy(code, 0, this.code, 0, code.length);
    }

    /**
     * @param code
     *                byte[], code which is not supported
     * @param cause
     *                throwable which cause this exception
     */
    public UnsupportedCodeException(byte[] code, Throwable cause) {
	super(cause);
	this.code = new byte[code.length];
	System.arraycopy(code, 0, this.code, 0, code.length);
    }

    /**
     * @param code
     *                byte[], code which is not supported
     * @param message
     *                message for exception
     * @param cause
     *                throwable which cause this exception
     */
    public UnsupportedCodeException(byte[] code, String message, Throwable cause) {
	super(message, cause);
	this.code = new byte[code.length];
	System.arraycopy(code, 0, this.code, 0, code.length);
    }

    /**
     * @return the code
     */
    public final byte[] getCode() {
	return code;
    }

}
