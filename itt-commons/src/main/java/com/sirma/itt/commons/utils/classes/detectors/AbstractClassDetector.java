/**
 * Copyright (c) 2008 15.12.2008 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.utils.classes.detectors;

/**
 * Base class for class detectors. Class detectors are classes which detect if
 * the provided String contains valid value which can be used to instantiate an
 * object by the <code>T</code> class.
 * 
 * @author Hristo Iliev
 * @param <T>
 *            type which the detector will detect
 */
public abstract class AbstractClassDetector<T> {
    /** name of the detector. */
    private final String detectorName;

    /**
     * Create the class detector and set the name of the detector.
     * 
     * @param detectorName
     *            {@link String}, name of detector
     */
    public AbstractClassDetector(String detectorName) {
	this.detectorName = detectorName;
    }

    /**
     * Retrieve the class which this detector will detect.
     * 
     * @return {@link Class}, the class which detector will detect
     */
    public abstract Class<T> getDetectedClass();

    /**
     * Check if the specified value contains enough information to produce an
     * object from type <code>T</code>. Note that <code>null</code> is valid
     * value for every class reference so null should return always true.
     * 
     * @param value
     *            {@link String}, the value which contains the information for
     *            producing an instance
     * @return <code>true</code> if the <code>value</code> contains enough
     *         information and the information is for proposed for
     *         <code>T</code> type, <code>false</code> otherwise
     */
    public abstract boolean detect(String value);

    /**
     * Create an instance of type <code>T</code> with provided value
     * information. Note that <code>null</code> is valid object reference for
     * every type so providing <code>null</code> should lead to returning
     * <code>null</code>.
     * 
     * Implementation of this method is not obligate to produce always a new
     * instance, but to use an already instantiated.
     * 
     * @param value
     *            {@link String}, value needed for creating an instance from
     *            type <code>T</code>
     * @return T, instance initialized with provided information by value
     * @throws InstantiationException
     *             thrown if the specified information by <code>value</code> is
     *             not enough or it is proposed for different type in order to
     *             create instance from detected class.
     */
    public abstract T createNewInstance(String value)
	    throws InstantiationException;

    /**
     * Getter method for detectorName.
     * 
     * @return the detectorName
     */
    public String getDetectorName() {
	return detectorName;
    }

}
