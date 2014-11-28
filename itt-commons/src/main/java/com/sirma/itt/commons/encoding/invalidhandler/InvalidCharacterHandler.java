/*
 * Created on 12.02.2008 @ 17:14:23
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.invalidhandler;

import com.sirma.itt.commons.encoding.translator.UnsupportedCodeException;
import com.sirma.itt.commons.encoding.translator.UnsupportedUnicodeNumberException;
import com.sirma.itt.commons.encoding.translator.Translator.ToUnicodeResultPair;

/**
 * Handler of invalid characters. This is base class of all such handlers and
 * all such handlers should extend this class.
 * 
 * @author Hristo Iliev
 * 
 */
public abstract class InvalidCharacterHandler {

    /** name of the handler. */
    private String handlerName;

    /**
     * Constructor of the handler. This constructor must be the public
     * constructor used to construct the handler, because the mapper is using
     * this constructor to construct the handlers.
     * 
     * @param handlerName
     *                String, name of the handler used in the property file to
     *                specify it's characteristics
     */
    public InvalidCharacterHandler(String handlerName) {
	this.handlerName = handlerName;
    }

    /**
     * This method should be called when an invalid Unicode number is required
     * to be translated to character which does not exist in specified encoding.
     * 
     * @param unicodeNumber
     *                int, number to be translated
     * @param output
     *                byte[], output to store the result
     * @param offset
     *                int, offset of the output from where will be stored the
     *                result
     * @return int, new offset to move to
     * @throws UnsupportedUnicodeNumberException
     *                 thrown if this Unicode number cannot be handled
     */
    public abstract int invalidUnicodeNumber(int unicodeNumber, byte[] output,
	    int offset) throws UnsupportedUnicodeNumberException;

    /**
     * This method should be called when an invalid native number is required to
     * be translated to Unicode number.
     * 
     * @param input
     *                byte[], input where the bytes in the native encoding are
     *                placed
     * @param offset
     *                int, offset from where the input is tried to be read
     * @param result
     *                ToUnicodeResultPair,
     * @throws UnsupportedCodeException
     *                 thrown if the handler cannot handle the code at specified
     *                 position
     */
    public abstract void invalidNativeNumber(byte[] input, int offset,
	    ToUnicodeResultPair result) throws UnsupportedCodeException;

    /**
     * @return the handlerName
     */
    public String getHandlerName() {
	return handlerName;
    }

    /**
     * @param handlerName
     *                the handlerName to set
     */
    public void setHandlerName(String handlerName) {
	this.handlerName = handlerName;
    }
}
