/*
 * Created on 09.02.2008 @ 16:40:28
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.context;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.sirma.itt.commons.context.AbstractContext;

/**
 * Connection point to the property file.
 * 
 * @author Hristo Iliev
 * 
 */
public final class Context extends AbstractContext {

    /** name of the property. */
    private static String propertyFileName;

    /** name of property file. */
    public static final String PROPERTY_FILE_NAME = "encoding.properties"; //$NON-NLS-1$

    /** suffix for variable encoding property. */
    public static final String VARIABLE_SUFFIX = ".variable"; //$NON-NLS-1$

    /** suffix for minimum bits required in the encoding. */
    public static final String MIN_BITS_SUFFIX = ".bits.min"; //$NON-NLS-1$

    /** suffix for maximum bits required in the encoding. */
    public static final String MAX_BITS_SUFFIX = ".bits.max"; //$NON-NLS-1$

    /** suffix of mapping file. */
    public static final String MAPPING_FILE_SUFFIX = ".mapping.file"; //$NON-NLS-1$

    /** suffix of mapping parser class name. */
    public static final String MAPPING_PARSER_SUFFIX = ".mapping.parser"; //$NON-NLS-1$

    /** suffix for invalid character handler class name. */
    public static final String INVALID_CHARACTER_HANDLER = ".invalid.character.handler"; //$NON-NLS-1$

    /** suffix for the byte with which to replace the invalid characters. */
    public static final String INVALID_CHARACTER_REPLACE = ".replace"; //$NON-NLS-1$

    /** default handler for invalid characters. */
    public static final String DEFAULT_INVALID_CHARACTER_HANDLER = "default.invalid.character.handler"; //$NON-NLS-1$

    /** suffix for translator class name. */
    public static final String TRANSLATOR_CLASS = ".translator.class"; //$NON-NLS-1$

    public static final String AUTOLOAD = "autoload"; //$NON-NLS-1$

    /** default encoding properties. */
    private static final String INNER_PROPERTY_FILE = "META-INF/encoding.properties"; //$NON-NLS-1$

    /** Singleton implementation of this class. */
    private static Context context;

    /**
     * Singleton constructor of context.
     */
    private Context() {
	// Singleton instance
    }

    /**
     * Retrieve the context.
     * 
     * @return Context, context to be retrieved
     */
    public static synchronized Context getContext() {
	if (context == null) {
	    context = new Context();
	}
	return context;
    }

    /**
     * Getter for name of the file which contains the properties.
     * 
     * @return String, file name
     */
    public static String getPropertyFileName() {
	return propertyFileName;
    }

    /**
     * Setter for name of the file which contains the properties.
     * 
     * @param propertyFileName
     *            String, file name
     */
    public static void setPropertyFileName(String propertyFileName) {
	Context.propertyFileName = propertyFileName;
    }

    @Override
    public String getExternalPropertiesFile() {
	return PROPERTY_FILE_NAME;
    }

    @Override
    public String getInternalPropertiesFile() {
	return INNER_PROPERTY_FILE;
    }

    @Override
    public InputStream getStreamProperties() throws IOException {
	if (propertyFileName != null) {
	    return new FileInputStream(propertyFileName);
	}
	return null;
    }
}
