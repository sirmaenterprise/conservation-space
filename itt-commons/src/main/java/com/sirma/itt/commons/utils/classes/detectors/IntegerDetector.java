/**
 * Copyright (c) 2008 15.12.2008 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.utils.classes.detectors;

import java.util.regex.Pattern;

/**
 * This class check if some value is valid integer or not.
 * 
 * @author Hristo Iliev
 */
public class IntegerDetector extends AbstractClassDetector<Integer> {

    /** pattern to check the integer. */
    private static final Pattern PATTERN = Pattern.compile("^[-+]?\\d+$"); //$NON-NLS-1$

    /**
     * Constructor of detector.
     * 
     * @param detectorName
     *            String, name of the detector
     */
    public IntegerDetector(String detectorName) {
	super(detectorName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer createNewInstance(String value)
	    throws InstantiationException {
	if (value == null) {
	    return null;
	}
	if (!detect(value)) {
	    throw new InstantiationException(value + " is not valid Integer."); //$NON-NLS-1$
	}
	/* use cache of Integer.valueOf(int) (Integer.valueOf(String) does not) */
	return Integer.valueOf(Integer.parseInt(value.trim()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean detect(String value) {
	if (value == null) {
	    return true;
	}
	return PATTERN.matcher(value).find();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Integer> getDetectedClass() {
	return Integer.class;
    }

}
