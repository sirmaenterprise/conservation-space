/*
 * Created on 09.02.2008 @ 13:17:23
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.translator.engine;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sirma.itt.commons.encoding.metainfo.MetaInformation;
import com.sirma.itt.commons.encoding.translator.InsufficientBufferException;
import com.sirma.itt.commons.encoding.translator.UnsupportedCodeException;
import com.sirma.itt.commons.encoding.translator.UnsupportedUnicodeNumberException;

/**
 * This engine is specific engine which transform Unicode number to UTF-8.
 * 
 * @author Hristo Iliev
 * 
 */
public class UTF8Engine extends Engine implements UTFEngine {

    /** buffer for output while using streams. */
    private byte[] outputBuffer = new byte[1];

    /**
     * Create UTF8 engine, used to translate from Unicode number to UTF-8 code.
     * 
     * @param encodingName
     *                String, name of the encoding
     * @see com.sirma.itt.commons.encoding.translator.Translator#Translator(String)
     */
    public UTF8Engine(String encodingName) {
	super(encodingName);
    }

    /**
     * Create UTF8 engine, used to translate from Unicode number to UTF-8 code.
     * 
     * @param metaInfo
     *                {@link MetaInformation}, meta information for the engine
     * @see com.sirma.itt.commons.encoding.translator.Translator#Translator(MetaInformation)
     */
    public UTF8Engine(MetaInformation metaInfo) {
	super(metaInfo);
    }

    /**
     * {@inheritDoc}
     */
    public int fromUnicode(int unicodeNumber, byte[] output, int offset)
	    throws UnsupportedUnicodeNumberException,
	    InsufficientBufferException {
	if ((unicodeNumber >= 0x0000) && (unicodeNumber <= 0x007F)) {
	    
	    if (output.length < offset + 1) {
		throw new InsufficientBufferException();
	    }
	    // 00000000 00000000 0zzzzzzz => 0zzzzzzz
	    output[offset + 0] = (byte) unicodeNumber;
	    return offset + 1;
	}

	if ((unicodeNumber >= 0x0080) && (unicodeNumber <= 0x07FF)) {
	    
	    if (output.length < offset + 2) {
		throw new InsufficientBufferException();
	    }
	    // 00000000 00000yyy yyzzzzzz => 110yyyyy 10zzzzzz
	    output[offset + 1] = (byte) ((unicodeNumber & 0x3F) | 0x80);
	    output[offset + 0] = (byte) (((unicodeNumber >>> 6) & 0x1F) | 0xC0);
	    return offset + 2;
	}

	if (((unicodeNumber >= 0x0800) && (unicodeNumber <= 0xD7FF))
		|| ((unicodeNumber >= 0xE000) && (unicodeNumber <= 0xFFFF))) {
	    
	    
	    if (output.length < offset + 3) {
		throw new InsufficientBufferException();
	    }
	    // 00000000 xxxxyyyy yyzzzzzz => 1110xxxx 10yyyyyy 10zzzzzz
	    output[offset + 2] = (byte) ((unicodeNumber & 0x3F) | 0x80);
	    output[offset + 1] = (byte) (((unicodeNumber >>> 6) & 0x3F) | 0x80);
	    output[offset + 0] = (byte) (((unicodeNumber >>> 12) & 0x0F) | 0xE0);
	    return offset + 3;
	}

	if ((unicodeNumber >= 0x010000) && (unicodeNumber <= 0x10FFFF)) {
	    // transform 010000�10FFFF
	    if (output.length < offset + 4) {
		throw new InsufficientBufferException();
	    }
	    // 000wwwxx xxxxyyyy yyzzzzzz => 11110www 10xxxxxx 10yyyyyy 10zzzzzz
	    output[offset + 3] = (byte) ((unicodeNumber & 0x3F) | 0x80);
	    output[offset + 2] = (byte) (((unicodeNumber >>> 6) & 0x3F) | 0x80);
	    output[offset + 1] = (byte) (((unicodeNumber >>> 12) & 0x3F) | 0x80);
	    output[offset + 0] = (byte) (((unicodeNumber >>> 18) & 0x07) | 0xF0);
	    return offset + 4;
	}

	throw new UnsupportedUnicodeNumberException(unicodeNumber,
		"Unicode number " + unicodeNumber + " is not supported number."); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public int fromUnicode(int unicodeNumber, OutputStream output)
	    throws IOException, UnsupportedUnicodeNumberException {
	int byteCount = 0;
	while (true) {
	    synchronized (outputBuffer) {
		try {
		    byteCount = fromUnicode(unicodeNumber, outputBuffer, 0);
		    output.write(outputBuffer, 0, byteCount);
		    break;
		} catch (InsufficientBufferException e) {
		    outputBuffer = new byte[outputBuffer.length * 2];
		}
	    }
	}
	return byteCount;
    }

    /**
     * {@inheritDoc}
     */
    public ToUnicodeResultPair toUnicode(byte[] input, int offset)
	    throws UnsupportedUTFCodeException {
	if (offset >= input.length) {
	    throw new ArrayIndexOutOfBoundsException();
	}
	ToUnicodeResultPair toUnicodeResult;
	int byteCount = byteCount(input[offset]);
	switch (byteCount) {
	case 1:
	    toUnicodeResult = getToUnicodeResultPair();
	    toUnicodeResult.setUnicodeNumber(input[offset]);
	    toUnicodeResult.setPositionToMoveTo(offset + 1);
	    return toUnicodeResult;
	case 2:
	    toUnicodeResult = getToUnicodeResultPair();
	    toUnicodeResult.setUnicodeNumber(twoDigitNumber(input, offset));
	    toUnicodeResult.setPositionToMoveTo(offset + 2);
	    return toUnicodeResult;
	case 3:
	    toUnicodeResult = getToUnicodeResultPair();
	    toUnicodeResult.setUnicodeNumber(threeDigitNumber(input, offset));
	    toUnicodeResult.setPositionToMoveTo(offset + 3);
	    return toUnicodeResult;
	case 4:
	    toUnicodeResult = getToUnicodeResultPair();
	    toUnicodeResult.setUnicodeNumber(fourDigitNumber(input, offset));
	    toUnicodeResult.setPositionToMoveTo(offset + 4);
	    return toUnicodeResult;
	default:
	    throw new UnsupportedUTFCodeException(new byte[] { input[offset] });
	}
    }

    @Override
    public ToUnicodeResultPair toUnicode(InputStream input, int offset)
	    throws IOException, UnsupportedCodeException {
	int header = input.read();
	if (header == -1) {
	    throw new EOFException();
	}
	ToUnicodeResultPair toUnicodeResult;
	int byteCount = byteCount(header);
	int tail1, tail2, tail3;
	switch (byteCount) {
	case 1:
	    toUnicodeResult = getToUnicodeResultPair();
	    toUnicodeResult.setUnicodeNumber(header);
	    toUnicodeResult.setPositionToMoveTo(offset + 1);
	    return toUnicodeResult;
	case 2:
	    tail1 = input.read();
	    if (tail1 == -1) {
		throw new EOFException();
	    }

	    toUnicodeResult = getToUnicodeResultPair();
	    toUnicodeResult.setUnicodeNumber(twoDigitNumber((byte) header,
		    (byte) tail1));
	    toUnicodeResult.setPositionToMoveTo(offset + 2);
	    return toUnicodeResult;
	case 3:
	    tail1 = input.read();
	    if (tail1 == -1) {
		throw new EOFException();
	    }
	    tail2 = input.read();
	    if (tail2 == -1) {
		throw new EOFException();
	    }
	    toUnicodeResult = getToUnicodeResultPair();
	    toUnicodeResult.setUnicodeNumber(threeDigitNumber((byte) header,
		    (byte) tail1, (byte) tail2));
	    toUnicodeResult.setPositionToMoveTo(offset + 3);
	    return toUnicodeResult;
	case 4:
	    tail1 = input.read();
	    if (tail1 == -1) {
		throw new EOFException();
	    }
	    tail2 = input.read();
	    if (tail2 == -1) {
		throw new EOFException();
	    }
	    tail3 = input.read();
	    if (tail3 == -1) {
		throw new EOFException();
	    }
	    toUnicodeResult = getToUnicodeResultPair();
	    toUnicodeResult.setUnicodeNumber(fourDigitNumber((byte) header,
		    (byte) tail1, (byte) tail2, (byte) tail3));
	    toUnicodeResult.setPositionToMoveTo(offset + 4);
	    return toUnicodeResult;
	default:
	    throw new UnsupportedUTFCodeException(new byte[] { (byte) header });
	}
    }

    /**
     * Return the number of bytes which are used for storing the UTF8 number.
     * Returned value can be from 1 to 4 for valid UTF8 header, or 0 and -1 for
     * invalid headers. If -1 is returned then the header is consisted from 5 or
     * more high order set bits (11111xxx) which is invalid UTF8 header. If 0 is
     * returned then the header is invalid as header but the byte is valid code
     * in UTF8 for the tail part of the number (10xxxxxx is valid for second,
     * third and forth bytes from the number).
     * 
     * @param header
     *                int, header of the UTF8 code
     * @return number of UTF8 code bytes - 1 to 4 for valid header, 0 for
     *         invalid header but valid code and -1 for invalid header and code
     */
    private int byteCount(int header) {
	if ((header & 0x80) != 0) { // 1xxxxxxx
	    if ((header & 0x40) == 0) { // 10xxxxxx - invalid header, valid byte
		return 0;
	    }
	    if ((header & 0x20) == 0) { // 110xxxxx - two bytes
		return 2;
	    }
	    if ((header & 0x10) == 0) { // 1110xxxx - three bytes
		return 3;
	    }
	    if ((header & 0x08) == 0) { // 11110xxx - four bytes
		return 4;
	    }
	    return -1; // 11111xxx - invalid header
	}
	return 1; // 0xxxxxxx - one byte
    }

    /**
     * Transform 4-byte UTF-8 code to Unicode number.
     * 
     * @param header
     *                byte, header of the UTF8 code
     * @param tail1
     *                byte, second byte
     * @param tail2
     *                byte, third byte
     * @param tail3
     *                byte, forth byte
     * @return int, the Unicode number
     * @throws UnsupportedUTFCodeException
     *                 thrown if the UTF-8 header is incorrect for 4-byte long
     *                 character and is not followed by 3-bytes
     */
    private int fourDigitNumber(byte header, byte tail1, byte tail2, byte tail3)
	    throws UnsupportedUTFCodeException {
	if (((header & 0xF8) != 0xF0) || ((tail1 & 0xC0) != 0x80)
		|| ((tail2 & 0xC0) != 0x80) || ((tail3 & 0xC0) != 0x80)) {
	    throw new UnsupportedUTFCodeException(new byte[] { header, tail1,
		    tail2, tail3 });
	}
	int result = ((((((header & 0x07) << 6) | (tail1 & 0x3F)) << 6) | (tail2 & 0x3F)) << 6)
		| (tail3 & 0x3F);
	if ((result >= 0xD800) && (result <= 0xDFFF)) {
	    throw new UnsupportedUTFCodeException(new byte[] { header, tail1,
		    tail2, tail3 });
	}
	return result;
    }

    /**
     * Transform 4-byte UTF-8 code to Unicode number.
     * 
     * @param buffer
     *                byte[], buffer where the UTF character is placed
     * @param offset
     *                int, position in the buffer where the character is placed
     * @return int, the Unicode number
     * @throws UnsupportedUTFCodeException
     *                 thrown if the UTF-8 header is incorrect for 4-byte long
     *                 character and is not followed by 3-bytes
     */
    private int fourDigitNumber(byte[] buffer, int offset)
	    throws UnsupportedUTFCodeException {
	if (buffer.length <= offset + 3) {
	    throw new ArrayIndexOutOfBoundsException();
	}
	return fourDigitNumber(buffer[offset], buffer[offset + 1],
		buffer[offset + 2], buffer[offset + 3]);
    }

    /**
     * Transform 3-byte UTF-8 code to Unicode number.
     * 
     * @param header
     *                byte, header of the UTF8 code
     * @param tail1
     *                byte, second byte
     * @param tail2
     *                byte, third byte
     * @return int, the Unicode number
     * @throws UnsupportedUTFCodeException
     *                 thrown if the UTF-8 header is incorrect for 4-byte long
     *                 character and is not followed by 3-bytes
     */
    private int threeDigitNumber(byte header, byte tail1, byte tail2)
	    throws UnsupportedUTFCodeException {
	if (((header & 0xF0) != 0xE0) || ((tail1 & 0xC0) != 0x80)
		|| ((tail2 & 0xC0) != 0x80)) {
	    throw new UnsupportedUTFCodeException(new byte[] { header, tail1,
		    tail2 });
	}
	int result = ((((header & 0x0F) << 6) | (tail1 & 0x3F)) << 6)
		| (tail2 & 0x3F);
	return result;
    }

    /**
     * Transform 3-byte UTF-8 code to Unicode number.
     * 
     * @param buffer
     *                byte[], buffer where the UTF character is placed
     * @param offset
     *                int, position in the buffer where the character is placed
     * @return int, the Unicode number
     * @throws UnsupportedUTFCodeException
     *                 thrown if the UTF-8 header is incorrect for 3-byte long
     *                 character and is not followed by 2-bytes
     */
    private int threeDigitNumber(byte[] buffer, int offset)
	    throws UnsupportedUTFCodeException {
	if (buffer.length <= offset + 2) {
	    throw new ArrayIndexOutOfBoundsException();
	}
	return threeDigitNumber(buffer[offset], buffer[offset + 1],
		buffer[offset + 2]);
    }

    /**
     * Transform 2-byte UTF-8 code to Unicode number.
     * 
     * @param header
     *                byte, header of the UTF8 code
     * @param tail1
     *                byte, second byte
     * @return int, the Unicode number
     * @throws UnsupportedUTFCodeException
     *                 thrown if the UTF-8 header is incorrect for 2-byte long
     *                 character and is not followed by one byte
     */
    private int twoDigitNumber(byte header, byte tail1)
	    throws UnsupportedUTFCodeException {
	if (((header & 0xE0) != 0xC0) || ((tail1 & 0xC0) != 0x80)) {
	    throw new UnsupportedUTFCodeException(new byte[] { header, tail1 });
	}
	int result = ((header & 0x1F) << 6) | (tail1 & 0x3F);
	return result;
    }

    /**
     * Transform 2-byte UTF-8 code to Unicode number.
     * 
     * @param buffer
     *                byte[], buffer where the UTF character is placed
     * @param offset
     *                int, position in the buffer where the character is placed
     * @return int, the Unicode number
     * @throws UnsupportedUTFCodeException
     *                 thrown if the UTF-8 header is incorrect for 2-byte long
     *                 character and is not followed by one byte
     */
    private int twoDigitNumber(byte[] buffer, int offset)
	    throws UnsupportedUTFCodeException {
	if (buffer.length <= offset + 1) {
	    throw new ArrayIndexOutOfBoundsException();
	}
	return twoDigitNumber(buffer[offset], buffer[offset + 1]);
    }
}
