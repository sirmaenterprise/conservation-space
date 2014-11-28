/**
 * Copyright (c) 2008 16.12.2008 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.utils.classes.detectors;

/**
 * This class check if some value is valid Boolean or not.
 * 
 * @author Hristo Iliev
 */
public class BooleanDetector extends AbstractClassDetector<Boolean> {

    /**
     * Constructor of detector.
     * 
     * @param detectorName
     *            String, name of the detector
     */
    public BooleanDetector(String detectorName) {
	super(detectorName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean createNewInstance(String value)
	    throws InstantiationException {
	if (value == null) {
	    return null;
	}
	String trimmed = value.trim();
	if ("true".equalsIgnoreCase(trimmed)) { //$NON-NLS-1$
	    return Boolean.TRUE;
	}
	if ("false".equalsIgnoreCase(trimmed)) { //$NON-NLS-1$
	    return Boolean.FALSE;
	}
	throw new InstantiationException("\"" + value //$NON-NLS-1$
		+ "\" is not valid boolean value."); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean detect(String value) {
	if (value == null) {
	    return true;
	}
	String trimmed = value.trim();
	return "true".equalsIgnoreCase(trimmed) //$NON-NLS-1$
		|| "false".equalsIgnoreCase(trimmed); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Boolean> getDetectedClass() {
	return Boolean.class;
    }

}
