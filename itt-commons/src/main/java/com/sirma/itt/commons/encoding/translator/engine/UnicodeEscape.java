/**
 * Copyright (c) 2010 27.01.2010 , Sirma ITT. /* /**
 */
package com.sirma.itt.commons.encoding.translator.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sirma.itt.commons.encoding.metainfo.MetaInformation;
import com.sirma.itt.commons.encoding.translator.InsufficientBufferException;
import com.sirma.itt.commons.encoding.translator.Translator;
import com.sirma.itt.commons.encoding.translator.TranslatorBunch;
import com.sirma.itt.commons.encoding.translator.TranslatorLoadingException;
import com.sirma.itt.commons.encoding.translator.UnsupportedCodeException;

/**
 * @author Hristo Iliev
 */
public class UnicodeEscape extends Engine {

	/**
	 * Constructor with encoding name.
	 * 
	 * @param encodingName
	 *            {@link String}, encoding name
	 */
	public UnicodeEscape(final String encodingName) {
		super(encodingName);
	}

	/**
	 * Constructor with meta information.
	 * 
	 * @param metaInfo
	 *            {@link MetaInformation}, meta information
	 */
	public UnicodeEscape(final MetaInformation metaInfo) {
		super(metaInfo);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int fromUnicode(final int unicodeNumber, final byte[] output,
			final int offset) throws InsufficientBufferException {
		checkBuffer(output, offset);
		String st = "0000" + Integer.toHexString(unicodeNumber);
		st = st.substring(st.length() - 4);
		output[offset] = '\\';
		output[offset + 1] = 'u';
		output[offset + 2] = (byte) st.charAt(0);
		output[offset + 3] = (byte) st.charAt(1);
		output[offset + 4] = (byte) st.charAt(2);
		output[offset + 5] = (byte) st.charAt(3);
		return offset + 6;
	}

	/**
	 * Check buffer does have at least 6 bytes.
	 * 
	 * @param output
	 *            byte[], array to check
	 * @param offset
	 *            int, offset for the array
	 * @throws InsufficientBufferException
	 *             thrown if there is not enough bytes
	 */
	private void checkBuffer(final byte[] output, final int offset)
			throws InsufficientBufferException {
		if (output.length < offset + 6) {
			throw new InsufficientBufferException();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int fromUnicode(final int unicodeNumber, final OutputStream output)
			throws IOException {
		byte[] buffer = new byte[6];
		int result = 0;
		try {
			result = fromUnicode(unicodeNumber, buffer, 0);
		} catch (InsufficientBufferException e) {
			// Impossible
		}
		output.write(buffer);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ToUnicodeResultPair toUnicode(final byte[] input, final int offset)
			throws UnsupportedCodeException {
		ToUnicodeResultPair result = new ToUnicodeResultPair();
		if ((input[offset] == '\\') && (input[offset + 1] == 'u')) {
			String unicodeNumber = new String(input, offset + 2, 4);
			result.setUnicodeNumber(Integer.parseInt(unicodeNumber, 16));
			result.setPositionToMoveTo(offset + 6);
			return result;
		}
		try {
			Translator utf8 = TranslatorBunch.getInstance().getTwig("UTF-8");
			return utf8.toUnicode(input, offset);
		} catch (TranslatorLoadingException e) {
			throw new UnsupportedCodeException(input, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ToUnicodeResultPair toUnicode(final InputStream input,
			final int offset) {
		throw new AbstractMethodError(
				"Unicode escape engine does not support translating to unicode");
	}

}
