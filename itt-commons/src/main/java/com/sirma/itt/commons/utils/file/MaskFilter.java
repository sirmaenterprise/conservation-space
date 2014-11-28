/**
 * Copyright (c) 2008 08.09.2008 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.utils.file;

/**
 * This filter is used for matching files with names which use general
 * convention for matching files. Use * for many characters and ? for single
 * character.
 * 
 * @author Hristo Iliev
 */
public class MaskFilter extends PatternFilter {

    /** mask for names. */
    private String mask;

    /**
     * Getter for the mask.
     * 
     * @return String, the mask
     */
    public String getMask() {
	return mask;
    }

    /**
     * Initialize the filter with the specified mask.
     * 
     * @param mask
     *                String, mask for matching file names
     */
    public MaskFilter(String mask) {
	super(maskToPattern(mask));
	this.mask = mask;
    }

    /**
     * Transform mask to RegEx pattern. Mask contain the mask which contains *
     * as replace for many characters and ? for single character.
     * 
     * @param mask
     *                String, mask to be transformed
     * @return String, transformed mask to RegEx pattern
     */
    private static String maskToPattern(String mask) {
	StringBuilder regex = new StringBuilder();
	char charToCheck;
	for (int i = 0; i < mask.length(); i++) {
	    charToCheck = mask.charAt(i);
	    if (charToCheck == '*') {
		regex.append(".*"); //$NON-NLS-1$
	    } else if (charToCheck == '?') {
		regex.append('.');
	    } else if (charToCheck == '.') {
		regex.append("\\."); //$NON-NLS-1$
	    } else if (charToCheck == '\\') {
		regex.append("\\\\"); //$NON-NLS-1$
	    } else {
		regex.append(charToCheck);
	    }
	}
	return regex.toString();
    }

}
