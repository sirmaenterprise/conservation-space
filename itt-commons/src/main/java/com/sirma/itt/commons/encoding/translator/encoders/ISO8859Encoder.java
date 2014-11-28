/*
 * Created on 11.02.2008 @ 20:09:50
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.translator.encoders;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sirma.itt.commons.encoding.translator.InsufficientBufferException;
import com.sirma.itt.commons.encoding.translator.UnsupportedCodeException;
import com.sirma.itt.commons.encoding.translator.UnsupportedUnicodeNumberException;

/**
 * This class is encoder of ISO-8859 group of encodings. All encoding from this
 * group (ISO-8859-1, ISO-8859-2...) can be transformed via this encoder.
 * 
 * @author Hristo Iliev
 * 
 */
public class ISO8859Encoder extends Encoder {

    /** ISO8859 character used for searching in map as key. */
    private byte[] nativeBuffer;

    /**
     * Create ISO8859 mapping translator.
     * 
     * @param encodingName
     *                String, encoding name used in property file as property
     *                header
     */
    public ISO8859Encoder(String encodingName) {
	super(encodingName);
	nativeBuffer = new byte[1];
    }

    /**
     * {@inheritDoc}
     */
    public int fromUnicode(int input, byte[] output, int offset)
	    throws InsufficientBufferException,
	    UnsupportedUnicodeNumberException {
	ArrayKey nativeEncodingKey = getBackward().get(new Integer(input));
	if (nativeEncodingKey == null) {
	    throw new UnsupportedUnicodeNumberException(input,
		    "Unicode number " + input + " unsupported in " //$NON-NLS-1$ //$NON-NLS-2$
			    + getMetaInfo().getNames()[0] + " encoding."); //$NON-NLS-1$
	}
	byte[] nativeEncoding = nativeEncodingKey.getArray();
	try {
	    System.arraycopy(nativeEncoding, 0, output, offset,
		    nativeEncoding.length);
	} catch (IndexOutOfBoundsException e) {
	    throw new InsufficientBufferException(e);
	}
	return offset + nativeEncoding.length;
    }

    /**
     * {@inheritDoc}
     */
    public ToUnicodeResultPair toUnicode(byte[] input, int offset)
	    throws UnsupportedCodeException {
	Integer nativeValue;
	synchronized (nativeBuffer) {
	    nativeBuffer[0] = input[offset];
	    nativeValue = getStraight().get(new ArrayKey(nativeBuffer));
	    if (nativeValue == null) {
		throw new UnsupportedCodeException(
			nativeBuffer,
			nativeBuffer[0]
				+ " unsupported in " + getMetaInfo().getNames()[0] //$NON-NLS-1$
				+ " encoding."); //$NON-NLS-1$
	    }
	}
	ToUnicodeResultPair toUnicodeResult = getToUnicodeResultPair();
	toUnicodeResult.setUnicodeNumber(nativeValue.intValue());
	toUnicodeResult.setPositionToMoveTo(offset + 1);
	return toUnicodeResult;
    }

    @Override
    public int fromUnicode(int input, OutputStream output) throws IOException,
	    UnsupportedUnicodeNumberException {
	ArrayKey nativeEncodingKey = getBackward().get(new Integer(input));
	if (nativeEncodingKey == null) {
	    throw new UnsupportedUnicodeNumberException(input,
		    "Unicode number " + input + " unsupported in " //$NON-NLS-1$ //$NON-NLS-2$
			    + getMetaInfo().getNames()[0] + " encoding."); //$NON-NLS-1$
	}
	byte[] nativeEncoding = nativeEncodingKey.getArray();
	output.write(nativeEncoding);
	return nativeEncoding.length;
    }

    @Override
    public ToUnicodeResultPair toUnicode(InputStream input, int offset)
	    throws IOException, UnsupportedCodeException {
	input.skip(offset);
	int inputValue = input.read();
	if (inputValue == -1) {
	    throw new EOFException();
	}
	Integer nativeValue;
	synchronized (nativeBuffer) {
	    nativeBuffer[0] = (byte) inputValue;
	    nativeValue = getStraight().get(new ArrayKey(nativeBuffer));
	    if (nativeValue == null) {
		throw new UnsupportedCodeException(
			nativeBuffer,
			nativeBuffer[0]
				+ " unsupported in " + getMetaInfo().getNames()[0] //$NON-NLS-1$
				+ " encoding."); //$NON-NLS-1$
	    }
	}
	ToUnicodeResultPair toUnicodeResult = getToUnicodeResultPair();
	toUnicodeResult.setUnicodeNumber(nativeValue.intValue());
	toUnicodeResult.setPositionToMoveTo(offset + 1);
	return toUnicodeResult;
    }

}
