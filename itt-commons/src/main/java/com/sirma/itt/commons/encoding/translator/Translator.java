/*
 * Created on 11.02.2008 @ 18:16:55
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.translator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.WeakHashMap;

import com.sirma.itt.commons.encoding.metainfo.MetaInformation;

/**
 * This class is base class of translators from one encoding to Unicode number.
 * All translators should implement the two way transformations - from the
 * encoding to Unicode number and vice versa. Translators require a bunch of
 * parameters which should be provided through {@link MetaInformation}
 * parameter.
 * 
 * Although this class stores cash of last {@link #toUnicode(byte[], int)}
 * operation result, it is thread-safe, because every thread have it's separate
 * instance in this cash.
 * 
 * @author Hristo Iliev
 * 
 */
public abstract class Translator {

    /** meta information of the translator. */
    private MetaInformation metaInfo;

    /**
     * buffer of results for toUnicode transformation. An buffer is created for
     * every used thread
     */
    private WeakHashMap<Thread, ToUnicodeResultPair> toUnicodeResultMap;

    /**
     * Initializer of the encoding translator. All the required information for
     * the encoding should be specified in the {@link Context}. If additional
     * property file should be used, then use
     * {@link #Translator(MetaInformation)} by constructing the
     * {@link MetaInformation} separately.
     * 
     * @param encodingName
     *                {@link String}, name of the encoding for which to load
     *                the translator
     */
    public Translator(String encodingName) {
	this(new MetaInformation(encodingName));
    }

    /**
     * Initializer of the encoding. The information for the encoding is stored
     * in the encodingMetaInfo parameter.
     * 
     * @param encodingMetaInfo
     *                {@link MetaInformation}, meta information which contains
     *                the required information for the encoding.
     */
    public Translator(MetaInformation encodingMetaInfo) {
	metaInfo = encodingMetaInfo;
	toUnicodeResultMap = new WeakHashMap<Thread, ToUnicodeResultPair>();
	TranslatorBunch.getInstance().put(encodingMetaInfo, this);
    }

    /**
     * Handle invalid Unicode numbers which are passed to be translated to the
     * encoding, but the Unicode number have no representation in this encoding.
     * 
     * @param unicodeNumber
     *                int, Unicode number
     * @param output
     *                byte[], output where the translated characters are stored
     * @param offset
     *                int, offset from the beginning of output where should be
     *                stored encoding representation of specified Unicode number
     * @return int, offset where the next characters will be placed after the
     *         actions of the handler
     * @throws UnsupportedUnicodeNumberException
     *                 if the specified Unicode number cannot be transformed
     *                 even with the handler
     */
    public int invalidUnicodeNumber(int unicodeNumber, byte[] output, int offset)
	    throws UnsupportedUnicodeNumberException {
	return metaInfo.getInvalidCharacterHandler().invalidUnicodeNumber(
		unicodeNumber, output, offset);
    }

    /**
     * Translate Unicode number to the encoding and put the encoded byte
     * sequence into the output at specified offset.
     * 
     * @param unicodeNumber
     *                int, number to be translated
     * @param output
     *                byte[], output to store the result
     * @param offset
     *                offset, offset of the output where will be stored the
     *                transformed byte sequence
     * @return int, the new offset after putting the value to the buffer
     * @throws InsufficientBufferException
     *                 thrown if the output array is smaller to store the result
     * @throws UnsupportedUnicodeNumberException
     *                 thrown if the specified Unicode number is not supported
     *                 by this encoding
     */
    public abstract int fromUnicode(int unicodeNumber, byte[] output, int offset)
	    throws InsufficientBufferException,
	    UnsupportedUnicodeNumberException;

    /**
     * Translate Unicode number to the encoding and write the encoded byte
     * sequence to the {@link OutputStream}.
     * 
     * @param unicodeNumber
     *                int, the Unicode number
     * @param output
     *                OutputStream, the stream where will be written
     * @return int, the number of written bytes
     * @throws IOException
     *                 thrown if error while writing to the output
     * @throws UnsupportedUnicodeNumberException
     *                 thrown if the specified Unicode number is not supported
     *                 by this encoding
     */
    public abstract int fromUnicode(int unicodeNumber, OutputStream output)
	    throws IOException, UnsupportedUnicodeNumberException;

    /**
     * Translate a encoding byte(s) to Unicode number. The encoded bytes are at
     * offset position in input byte array. The result is a couple of values
     * used to store the results. The first value in the result is the Unicode
     * number and the second is the new offset of input. No synchronization is
     * required of ToUnicodeResultPair, if multiple threads are used. Further
     * synchronization is required if there are fields such as buffers which
     * require synchronization.
     * 
     * @param input
     *                byte[], the input where are stored the encoded byte
     *                sequence
     * @param offset
     *                int, position of the input array where are stored
     * @return {@link ToUnicodeResultPair}, the unicode number and the new
     *         offset
     * @throws UnsupportedCodeException
     *                 thrown if the code at current position is not valid
     *                 character in this encoding
     */
    public abstract ToUnicodeResultPair toUnicode(byte[] input, int offset)
	    throws UnsupportedCodeException;

    /**
     * Translate a encoding byte(s) to Unicode number. The encoded bytes are at
     * offset position in input byte array. The result is a couple of values
     * used to store the results. The first value in the result is the Unicode
     * number and the second is the read bytes from the input. No
     * synchronization is required of ToUnicodeResultPair, if multiple threads
     * are used. Further synchronization is required if there are fields such as
     * buffers which require synchronization.
     * 
     * @param input
     *                {@link InputStream}, stream from which to read
     * @param offset
     *                int, the number of bytes to be skipped from the input
     * @return {@link ToUnicodeResultPair}, the unicode number and read bytes
     * @throws IOException
     *                 thrown if error while writing to the output
     * @throws UnsupportedCodeException
     *                 thrown if the code at code at specified position in the
     *                 input is not valid character in this encoding
     */
    public abstract ToUnicodeResultPair toUnicode(InputStream input, int offset)
	    throws IOException, UnsupportedCodeException;

    /**
     * Handle invalid encoding bytes which are passed to be translated to the
     * Unicode number, but the encoded bytes are not representable in Unicode
     * (or not supported).
     * 
     * @param input
     *                byte[],input where the bytes to be encoded are place
     * @param offset
     *                int, offset where the bytes are placed
     * @param result
     *                {@link ToUnicodeResultPair}, used to store the result of
     *                actions of handler
     * @throws UnsupportedCodeException
     *                 thrown if the code at specified position cannot be
     *                 handled by the handler
     */
    public void invalidNativeNumber(byte[] input, int offset,
	    ToUnicodeResultPair result) throws UnsupportedCodeException {
	metaInfo.getInvalidCharacterHandler().invalidNativeNumber(input,
		offset, result);
    }

    /**
     * @return the metaInfo
     */
    public final MetaInformation getMetaInfo() {
	return metaInfo;
    }

    /**
     * This class is used to store the result of "to unicode" operations which
     * require to be retrieved as the unicode number so the offset of movement.
     * 
     * @author Hristo Iliev
     * 
     */
    public class ToUnicodeResultPair {

	/** the unicode number. */
	private int unicodeNumber;

	/** position where the input should move after the transformation. */
	private int positionToMoveTo;

	/**
	 * @return the unicodeNumber
	 */
	public final int getUnicodeNumber() {
	    return unicodeNumber;
	}

	/**
	 * @param unicodeNumber
	 *                the unicodeNumber to set
	 */
	public final void setUnicodeNumber(int unicodeNumber) {
	    this.unicodeNumber = unicodeNumber;
	}

	/**
	 * @return the positionToMoveTo
	 */
	public final int getPositionToMoveTo() {
	    return positionToMoveTo;
	}

	/**
	 * @param positionToMoveTo
	 *                the positionToMoveTo to set
	 */
	public final void setPositionToMoveTo(int positionToMoveTo) {
	    this.positionToMoveTo = positionToMoveTo;
	}
    }

    /**
     * Retrieve the Unicode result for the current thread. If multiple threads
     * are used they store their results in separate {@link ToUnicodeResultPair}
     * objects. No further synchronization is required.
     * 
     * @return the toUnicodeResult
     */
    public ToUnicodeResultPair getToUnicodeResultPair() {
	ToUnicodeResultPair result = toUnicodeResultMap.get(Thread
		.currentThread());
	if (result == null) {
	    result = new ToUnicodeResultPair();
	    toUnicodeResultMap.put(Thread.currentThread(), result);
	}
	return result;
    }

    /**
     * Getter method for toUnicodeResultMap. This map is used to store the
     * thread-safe cash for results of {@link #toUnicode(byte[], int)}
     * operation.
     * 
     * @return the toUnicodeResultMap
     */
    protected WeakHashMap<Thread, ToUnicodeResultPair> getToUnicodeResultMap() {
	return toUnicodeResultMap;
    }

}
