/**
 * Copyright (c) 2009 26.09.2009 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.utils.classes.detectors;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;


/**
 * 
 * 
 * @author Hristo Iliev
 */
public class FileInputStreamClassDetector extends
	AbstractClassDetector<InputStream> {

    /**
     * Create the class detector and set the name of the detector.
     * 
     * @param detectorName
     *            {@link String}, name of detector
     */
    public FileInputStreamClassDetector(final String detectorName) {
	super(detectorName);
    }

    /**
     * @see com.sirma.itt.commons.utils.classes.detectors.AbstractClassDetector#createNewInstance(java.lang.String)
     */
    @Override
    public InputStream createNewInstance(final String value)
	    throws InstantiationException {
	InputStream result;
	try {
	    result = new FileInputStream(value);
	    return result;
	} catch (FileNotFoundException e) {
	    // Try other methods of loading
	}
	result = FileInputStreamClassDetector.class.getClassLoader()
		.getResourceAsStream(value);
	if (result != null) {
	    return result;
	}
	try {
	    URI uri = new URI(value);
	    return uri.toURL().openStream();
	} catch (URISyntaxException e) {
	    throw new InstantiationException(e.getMessage());
	} catch (MalformedURLException e) {
	    throw new InstantiationException(e.getMessage());
	} catch (IOException e) {
	    throw new InstantiationException(e.getMessage());
	}
    }

    /**
     * @see com.sirma.itt.commons.utils.classes.detectors.AbstractClassDetector#detect(java.lang.String)
     */
    @Override
    public boolean detect(final String value) {
	return true;
    }

    /**
     * @see com.sirma.itt.commons.utils.classes.detectors.AbstractClassDetector#getDetectedClass()
     */
    @Override
    public Class<InputStream> getDetectedClass() {
	return InputStream.class;
    }

}
