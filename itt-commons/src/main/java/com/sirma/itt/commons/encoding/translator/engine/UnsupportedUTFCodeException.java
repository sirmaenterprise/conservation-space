/*
 * Created on 09.02.2008 @ 17:40:39
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.translator.engine;

import com.sirma.itt.commons.encoding.translator.UnsupportedCodeException;

/**
 * Thrown if specified an incorrect UTF code, i.e. start with 4-byte header but
 * only 3-bytes long character.
 * 
 * @author Hristo Iliev
 * 
 */
public class UnsupportedUTFCodeException extends UnsupportedCodeException {

    /**
     * 
     */
    private static final long serialVersionUID = -4289374382448332124L;

    /**
     * @param code
     *                byte[], code which is not supported
     * 
     */
    public UnsupportedUTFCodeException(byte[] code) {
	super(code);
    }

    /**
     * @param code
     *                byte[], code which is not supported
     * @param message
     *                message for exception
     */
    public UnsupportedUTFCodeException(byte[] code, String message) {
	super(code, message);
    }

    /**
     * @param code
     *                byte[], code which is not supported
     * @param cause
     *                throwable which cause this exception
     */
    public UnsupportedUTFCodeException(byte[] code, Throwable cause) {
	super(code, cause);
    }

    /**
     * @param code
     *                byte[], code which is not supported
     * @param message
     *                message for exception
     * @param cause
     *                throwable which cause this exception
     */
    public UnsupportedUTFCodeException(byte[] code, String message,
	    Throwable cause) {
	super(code, message, cause);
    }

}
