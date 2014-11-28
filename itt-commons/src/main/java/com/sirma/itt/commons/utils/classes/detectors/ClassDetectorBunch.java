/**
 * Copyright (c) 2008 15.12.2008 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.utils.classes.detectors;

import java.util.Set;

import com.sirma.itt.commons.bunch.Bunch;

/**
 * Bunch of class detectors. Every detector is stored with the class which can
 * detect.
 * 
 * @author Hristo Iliev
 */
public class ClassDetectorBunch
	extends
	Bunch<Class<?>, AbstractClassDetector<?>, ClassDetectorLoadingException> {

    /**
     * Comment for serialVersionUID.
     */
    private static final long serialVersionUID = -7235031178789805482L;

    /** singleton instance of bunch. */
    private static final ClassDetectorBunch INSTANCE = new ClassDetectorBunch();

    /**
     * Constructor for bunch.
     */
    private ClassDetectorBunch() {
	// Singleton implementation
    }

    /**
     * Retrieve the singleton instance.
     * 
     * @return {@link ClassDetectorBunch}, bunch for class detectors
     */
    public static ClassDetectorBunch getInstance() {
	return INSTANCE;
    }

    @Override
    protected Class<?> createTwigKey(String twigName,
	    AbstractClassDetector<?> twig) {
	return twig.getDetectedClass();
    }

    @Override
    protected String getTwigClass(String twigName) {
	return Context.getContext().getProperty(twigName);
    }

    @Override
    protected Class<?> getTwigKey(String twigName) {
	Set<Class<?>> keys = keySet();
	for (Class<?> key : keys) {
	    if (key.getName().equals(twigName)) {
		return key;
	    }
	}
	return null;
    }

    @Override
    protected boolean isValidTwigClass(Class<?> twigClass) {
	return AbstractClassDetector.class.isAssignableFrom(twigClass);
    }

    @Override
    protected AbstractClassDetector<?> manageException(String twigName,
	    Exception cause) throws ClassDetectorLoadingException {
	if (cause != null) {
	    throw new ClassDetectorLoadingException(cause);
	}
	throw new ClassDetectorLoadingException();
    }

}
