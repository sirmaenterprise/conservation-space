/**
 * Copyright (c) 2008 15.12.2008 , Sirma ITT. /* /**
 */
package com.sirma.itt.commons.utils.classes.detectors;

/**
 * This class check if some value is valid String or not.
 * 
 * @author Hristo Iliev
 */
public class StringDetector extends AbstractClassDetector<String> {

	/**
	 * Constructor of detector.
	 * 
	 * @param detectorName
	 *            String, name of the detector
	 */
	public StringDetector(String detectorName) {
		super(detectorName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String createNewInstance(String value) {
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean detect(String value) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<String> getDetectedClass() {
		return String.class;
	}

}
