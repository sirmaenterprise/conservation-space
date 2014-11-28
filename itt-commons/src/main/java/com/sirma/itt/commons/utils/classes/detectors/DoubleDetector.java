/**
 * Copyright (c) 2008 15.12.2008 , Sirma ITT. /* /**
 */
package com.sirma.itt.commons.utils.classes.detectors;

import java.util.regex.Pattern;

/**
 * This class check if some value is valid Double or not.
 * 
 * @author Hristo Iliev
 */
public class DoubleDetector extends AbstractClassDetector<Double> {

	/** pattern to check the double. */
	private static final Pattern PATTERN = Pattern
			.compile("\\s*^[-+]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][-+]?[0-9]+)?\\s*$"); //$NON-NLS-1$

	/**
	 * Constructor of detector.
	 * 
	 * @param detectorName
	 *            String, name of the detector
	 */
	public DoubleDetector(String detectorName) {
		super(detectorName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Double createNewInstance(String value) {
		if (value == null) {
			return null;
		}
		return Double.valueOf(value);
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
	public Class<Double> getDetectedClass() {
		return Double.class;
	}

}
