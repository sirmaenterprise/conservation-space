/*
 * Created on 12.02.2008 @ 18:53:27
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.invalidhandler;

import com.sirma.itt.commons.encoding.context.Context;
import com.sirma.itt.commons.encoding.translator.Translator.ToUnicodeResultPair;

/**
 * Replace character if found invalid for the specified encoding. The replace
 * character is within 00 to FF range.
 * 
 * @author Hristo Iliev
 * 
 */
public class CharacterReplaceHandler extends InvalidCharacterHandler {

    /** byte to replace with. */
    private byte byteToReplaceWith;

    /**
     * In internal representation this type of classes are create by means of
     * reflection. To create such handler use
     * {@link InvalidCharacterHandlerBunch} to load the handler.
     * 
     * @param handlerName
     *                String, name of the handler
     */
    public CharacterReplaceHandler(String handlerName) {
	this(handlerName, Byte.parseByte(Context.getContext().getProperty(
		handlerName + Context.INVALID_CHARACTER_REPLACE)));
    }

    /**
     * Handler for replacing an invalid character with specified byte. This
     * constructor is for programming purpose without using Context. If the
     * property is specified in the context with key
     * <code><i>handlerName</i>.replace</code> where <i>handlerName</i> is
     * the name of the handler key, then {@link InvalidCharacterHandlerBunch}
     * should be used instead creating with this constructor.
     * 
     * @param handlerName
     *                String, name of the handler
     * @param byteToReplaceWith
     *                byte, byte to replace with
     */
    public CharacterReplaceHandler(String handlerName, byte byteToReplaceWith) {
	super(handlerName);
	this.byteToReplaceWith = byteToReplaceWith;
	InvalidCharacterHandlerBunch.getInstance().put(
		CharacterReplaceHandler.class.getName(), this);
    }

    /**
     * {@inheritDoc}
     */
    public int invalidUnicodeNumber(int unicodeNumber, byte[] output, int offset) {
	output[offset] = byteToReplaceWith;
	return offset + 1;
    }

    /**
     * {@inheritDoc}
     */
    public void invalidNativeNumber(byte[] input, int offset,
	    ToUnicodeResultPair result) {
	result.setUnicodeNumber(byteToReplaceWith);
	result.setPositionToMoveTo(offset + 1);
    }
}
