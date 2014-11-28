/**
 * Copyright (c) 2008 09.09.2008 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.utils.file;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

/**
 * This filter is used for matching filenames using Regular Expression pattern.
 * 
 * @author Hristo Iliev
 */
public class PatternFilter implements FilenameFilter {

    /** pattern for matching filenames. */
    private Pattern pattern;

    /**
     * Initialize the filter with the pattern.
     * 
     * @param regex
     *                String, RegEx for matching filenames
     */
    public PatternFilter(String regex) {
	pattern = Pattern.compile(regex);
    }

    /**
     * {@inheritDoc}
     */
    public boolean accept(File dir, String name) {
	return pattern.matcher(name).find();
    }
}
