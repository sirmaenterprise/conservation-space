/**
 * Copyright (c) 2009 27.04.2009 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.utils.encoding;

/**
 * Utility class for basic operation with UTF8 {@link String}s.
 * 
 * @author Hristo Iliev
 */
public final class UTF8Utils {

    /** Byte-order mark. */
    private static final String BOM = "\uFEFF"; //$NON-NLS-1$

    /**
     * Hide utility constructor
     */
    private UTF8Utils() {
	// Hide utility constructor
    }

    /**
     * Retrieve the BOM.
     * 
     * @return {@link String}, the BOM.
     */
    public static final String getBOM() {
	return BOM;
    }

    /**
     * Test if the byte array starts with BOM.
     * 
     * @param array
     *            byte[], byte array to be tested
     * @return <code>true</code> if the array starts with BOM,
     *         <code>false if the {@link String} does not start with BOM
     */
    public static final boolean startWithBOM(byte[] array) {
	if (array.length < 3) {
	    return false;
	}
	if ((array[0] != (byte) 0xEF) || (array[1] != (byte) 0xBB)
		|| (array[2] != (byte) 0xBF)) {
	    return false;
	}
	return true;
    }

    /**
     * Test if the byte array starts with BOM.
     * 
     * @param text
     *            {@link String}, string to be tested
     * @return <code>true</code> if the string starts with BOM,
     *         <code>false if the {@link String} does not start with BOM
     */
    public static final boolean startWithBOM(String text) {
	return text.startsWith(BOM);
    }
}
