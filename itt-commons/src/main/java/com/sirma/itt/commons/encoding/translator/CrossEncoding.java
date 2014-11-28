/*
 * Created on 09.02.2008 @ 12:01:24
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.translator;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import com.sirma.itt.commons.encoding.translator.Translator.ToUnicodeResultPair;

/**
 * Output point for the translation. All translations are made through this
 * class which contains the translators for the encodings.
 * 
 * @author Hristo Iliev
 * 
 */
public class CrossEncoding {

    /** transform from this encoding. */
    private Translator fromEncoding;

    /** transform to this encoding. */
    private Translator toEncoding;

    /**
     * Create transformer from one encoding to another.
     * 
     * @param fromEncodingName
     *                String, name of encoding from which to transform
     * @param toEncodingName
     *                String, name of encoding to which to transform
     * @throws TranslatorLoadingException
     *                 thrown if no such translator can be loaded or an
     *                 configuration error occur
     */
    public CrossEncoding(String fromEncodingName, String toEncodingName)
	    throws TranslatorLoadingException {
	fromEncoding = TranslatorBunch.getInstance().getTwig(fromEncodingName);
	toEncoding = TranslatorBunch.getInstance().getTwig(toEncodingName);
    }

    /**
     * Translate input from one encoding to the other.
     * 
     * @param input
     *                byte[], input to transform
     * @return byte[], transformed output
     * @throws UnsupportedUnicodeNumberException
     *                 thrown if the output encoding does not support specific
     *                 Unicode number from input
     * @throws UnsupportedCodeException
     *                 thrown if the input contains character which have no
     *                 mapping to Unicode number. This can be either because the
     *                 mapping file is not fully written or because one of the
     *                 characters are invalid for this encoding
     */
    public byte[] translate(byte[] input)
	    throws UnsupportedUnicodeNumberException, UnsupportedCodeException {
	return translate(input, 0, input.length);
    }

    /**
     * Translate input from one encoding to the other.
     * 
     * @param input
     *                byte[], input to transform
     * @param inputOffset
     *                int, the index from where to start translation
     * @param inputLimit
     *                int, the limit where to stop translation
     * @return byte[], result
     * @throws UnsupportedUnicodeNumberException
     *                 thrown if the output encoding does not support specific
     *                 Unicode number from input
     * @throws UnsupportedCodeException
     *                 thrown if the input contains character which have no
     *                 mapping to Unicode number. This can be either because the
     *                 mapping file is not fully written or because one of the
     *                 characters are invalid for this encoding
     */
    public byte[] translate(byte[] input, int inputOffset, int inputLimit)
	    throws UnsupportedUnicodeNumberException, UnsupportedCodeException {
	LinkedList<Byte> result = new LinkedList<Byte>();
	int offset = inputOffset;
	ToUnicodeResultPair unicodeResult = null;
	byte[] output = new byte[1];
	int bytesToAdd;

	/*
	 * Iterate over the input from the inputOffset position to the
	 * inputLimit position.
	 */
	while (offset < inputLimit) {
	    bytesToAdd = 0;
	    try {
		unicodeResult = fromEncoding.toUnicode(input, offset);
	    } catch (ArrayIndexOutOfBoundsException e) {
		break;
	    } catch (UnsupportedCodeException e) {
		unicodeResult = fromEncoding.getToUnicodeResultPair();
		toEncoding.invalidNativeNumber(input, offset, unicodeResult);
	    }
	    try {
		bytesToAdd = toEncoding.fromUnicode(unicodeResult
			.getUnicodeNumber(), output, 0);
	    } catch (InsufficientBufferException e) {
		output = new byte[output.length * 2];
		continue;
	    } catch (UnsupportedUnicodeNumberException e) {
		bytesToAdd = toEncoding.invalidUnicodeNumber(unicodeResult
			.getUnicodeNumber(), output, 0);
	    }
	    if (offset <= unicodeResult.getPositionToMoveTo()) {
		offset = unicodeResult.getPositionToMoveTo();
	    } else {
		// If the offset is not changed or it is changed backwards
		offset += fromEncoding.getMetaInfo().getMinSizeInBytes();
	    }
	    for (int index = 0; index < bytesToAdd; index++) {
		result.add(Byte.valueOf(output[index]));
	    }
	}
	return byteListToArray(result);
    }

    /**
     * Translate input from one encoding to the other.
     * 
     * @param input
     *                {@link InputStream}, the stream from which will be read
     *                the input
     * @param output
     *                {@link OutputStream}, the stream where will be written
     *                the output
     * @return number of translated characters
     * @throws IOException
     *                 thrown if I/O exception occur while manipulating streams
     * @throws UnsupportedUnicodeNumberException
     *                 thrown if the output encoding does not support specific
     *                 Unicode number from input
     * @throws UnsupportedCodeException
     *                 thrown if the input contains character which have no
     *                 mapping to Unicode number. This can be either because the
     *                 mapping file is not fully written or because one of the
     *                 characters are invalid for this encoding
     */
    public int translate(InputStream input, OutputStream output)
	    throws IOException, UnsupportedCodeException,
	    UnsupportedUnicodeNumberException {
	ToUnicodeResultPair unicodeResult;
	int count = 0;
	while (true) {
	    try {
		unicodeResult = fromEncoding.toUnicode(input, 0);
	    } catch (EOFException e) {
		break;
	    }
	    toEncoding.fromUnicode(unicodeResult.getUnicodeNumber(), output);
	    count++;
	}
	return count;
    }

    /**
     * Translate input from one encoding to the other.
     * 
     * @param input
     *                {@link InputStream}, the stream from which will be read
     *                the input
     * @param offset
     *                bytes to be skipped from the beginning input
     * @param count
     *                number of characters to be transformed
     * @param output
     *                {@link OutputStream}, the stream where will be written
     *                the output
     * @return number of translated characters
     * @throws IOException
     *                 thrown if I/O exception occur while manipulating streams
     * @throws UnsupportedUnicodeNumberException
     *                 thrown if the output encoding does not support specific
     *                 Unicode number from input
     * @throws UnsupportedCodeException
     *                 thrown if the input contains character which have no
     *                 mapping to Unicode number. This can be either because the
     *                 mapping file is not fully written or because one of the
     *                 characters are invalid for this encoding
     */
    public int translate(InputStream input, int offset, int count,
	    OutputStream output) throws IOException, UnsupportedCodeException,
	    UnsupportedUnicodeNumberException {
	input.skip(offset);
	if (count < 0) {
	    return translate(input, output);
	}
	ToUnicodeResultPair unicodeResult;
	for (int i = 0; i < count; i++) {
	    try {
		unicodeResult = fromEncoding.toUnicode(input, 0);
	    } catch (EOFException e) {
		return i;
	    }
	    toEncoding.fromUnicode(unicodeResult.getUnicodeNumber(), output);
	}
	return count;
    }

    /**
     * Converts the list of bytes to array of bytes.
     * 
     * @param list
     *                the list of bytes
     * @return array with the bytes
     */
    private byte[] byteListToArray(List<Byte> list) {
	byte[] byteArrayResult = new byte[list.size()];
	int position = 0;
	for (Byte item : list) {
	    byteArrayResult[position++] = item.byteValue();
	}
	return byteArrayResult;
    }

}
