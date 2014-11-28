/**
 * Copyright (c) 2008 15.12.2008 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.utils.classes.detectors;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.sirma.itt.commons.context.AbstractContext;

/**
 * Context which will store the configuration of the ClassDetector.
 * 
 * @author Hristo Iliev
 */
public class Context extends AbstractContext {

    /** name of the file if stored in the jar. */
    private static final String INTERNAL_PROPERTIES_FILE = "META-INF/classdetector.properties"; //$NON-NLS-1$
    /** name of the file loaded outside the jar. */
    private static final String EXTERNAL_PROPERTIES_FILE = "classdetector.properties"; //$NON-NLS-1$
    /** name of the file defined by the user. */
    private static String userDefinedProperties;

    /**
     * Holds the context in order to provide lazy initialization of user defined
     * properties.
     * 
     * @author Hristo Iliev
     */
    private static final class ContextHolder {
	/** instance of the context. */
	static final Context INSTANCE = new Context();
    }

    /**
     * Singleton constructor.
     */
    Context() {
	// Singleton implementation
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getExternalPropertiesFile() {
	return EXTERNAL_PROPERTIES_FILE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInternalPropertiesFile() {
	return INTERNAL_PROPERTIES_FILE;
    }

    @Override
    public InputStream getStreamProperties() throws IOException {
	if (userDefinedProperties != null) {
	    return new FileInputStream(userDefinedProperties);
	}
	return null;
    }

    /**
     * Retrieve the context
     * 
     * @return {@link Context}, the context
     */
    public static Context getContext() {
	return ContextHolder.INSTANCE;
    }

    /**
     * Setter method for userDefinedProperties.
     * 
     * @param userDefinedProperties
     *            the userDefinedProperties to set
     */
    public static void setUserDefinedProperties(String userDefinedProperties) {
	Context.userDefinedProperties = userDefinedProperties;
    }

}
