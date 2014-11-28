/*
 * Created on 09.02.2008 @ 19:22:43
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.metainfo;

import java.util.Properties;

import com.sirma.itt.commons.encoding.context.Context;
import com.sirma.itt.commons.encoding.invalidhandler.InvalidCharacterHandler;
import com.sirma.itt.commons.encoding.invalidhandler.InvalidCharacterHandlerBunch;
import com.sirma.itt.commons.encoding.invalidhandler.InvalidCharacterHandlerLoadingException;
import com.sirma.itt.commons.encoding.mapper.InvalidMapplerLoadingException;
import com.sirma.itt.commons.encoding.mapper.Mapper;
import com.sirma.itt.commons.encoding.mapper.MapperBunch;

/**
 * Meta information for encoding. This class contains the basic information
 * which can be specified for an encoding. Parameters required by the
 * {@link MetaInformation} class can be set either through the setters or the
 * {@link Properties}.
 * 
 * {@link Properties} instance should contains following properties:
 * <ul>
 * <li><b><i>encoding_name</i></b> - this value should contain the aliases
 * of the encoding. Although this aliases can be used for retrieving the
 * encoding, they can not be used for loading the encoding. For loading the
 * encoding, encoding_name should be used, which is the key for specifying this
 * aliases. Once the loading is done all the aliases can be used for retrieving
 * the encoding {@link Translator} from the {@link TranslatorBunch}
 * <li><b><i>encoding_name</i>.variable</b> - true if the encoding is with
 * variable-length characters, false if encoding is with concrete character
 * length. For example ISO8859 encoding family is with concrete character length -
 * 8 bits, but UTF-8 is with variable character length 8 - 32 bits.
 * <li><b><i>encoding_name</i>.bits.max</b> - number of bits used for the
 * longest character
 * <li><b><i>encoding_name</i>.bits.min</b> - number of bits used for the
 * shortest character
 * <li><b><i>encoding_name</i>.invalid.character.handler</b> - name of class
 * which will handle the invalid characters. Handling of invalid characters is
 * functionality used when the input contains characters which are invalid for
 * the encoding or no mapping for this character is specified.
 * </ul>
 * <ul>
 * Optional properties for an translator:
 * <li><b><i>encoding_name</i>.mapping.file</b> - file which contains the
 * mapping of characters from the encoding to the Unicode numbers. This is
 * optional parameter, because not all {@link Translator}s are using mapping
 * file to translate its characters to Unicode number. For instance
 * {@link Engine}s (like {@link UTF8Engine}) does not use mapping file.
 * <li><b><i>encoding_name</i>.mapping.parser</b> - name of the class which
 * will parse the mapping file
 * </ul>
 * Property file should have following parameter which are required for default
 * behavior:
 * <ul>
 * <li><b>default.invalid.character.handle</b>r - default handler used if the
 * specified handler cannot be loaded
 * </ul>
 * 
 * @author Hristo Iliev
 * @see Properties
 * @see Translator
 * @see TranslatorBunch
 * @see InvalidCharacterHandler
 */
public class MetaInformation {

    /** names with which the encoding is known. */
    private String[] names;

    /** is the size of the characters is variable. */
    private boolean variable;

    /** size of longest characters in bytes. */
    private int maxSizeInBytes = 1;

    /** size of longest characters in bits. */
    private int maxSizeInBits = 8;

    /** size of shortest characters in bytes. */
    private int minSizeInBytes = 1;

    /** size of shortest characters in bits. */
    private int minSizeInBits = 8;

    /** file which contains the mapping table. */
    private String mappingFile;

    /** mapper used to parse the mapping file. */
    private Mapper mapper;

    /** handler of invalid characters. */
    private InvalidCharacterHandler invalidCharacterHandler;

    /** properties used to load the meta-information. */
    private Properties properties;

    /** name of the property key used to specify the encoding meta information. */
    private final String propertyEncodingName;

    /**
     * Default constructor. Used for programming creation of meta-information.
     */
    public MetaInformation() {
	propertyEncodingName = null;
    }

    /**
     * Construct the meta-information from the default context. The encoding
     * name must be the base key used in the file for the encoding.
     * 
     * @param encodingName
     *                String, name of the encoding used as base key in the
     *                Context
     */
    public MetaInformation(String encodingName) {
	this(encodingName, Context.getContext().getProperty());
    }

    /**
     * Construct the meta information from the specified properties. The keys
     * and values should be as described in the header of this class.
     * 
     * @param encodingName
     *                String, name of the encoding used as base key in the
     *                properties
     * @param properties
     *                {@link Properties}, properties which contains the
     *                meta-information
     */
    public MetaInformation(String encodingName, Properties properties) {
	this.propertyEncodingName = encodingName;
	this.properties = properties;
	setNames((encodingName + " " + properties.getProperty(encodingName)) //$NON-NLS-1$
		.split("\\s+")); //$NON-NLS-1$
	String property = properties.getProperty(encodingName
		+ Context.VARIABLE_SUFFIX);
	if (property != null) {
	    setVariable(Boolean.getBoolean(property));
	}
	property = properties.getProperty(encodingName
		+ Context.MIN_BITS_SUFFIX);
	if (property != null) {
	    setMinSizeInBits(Integer.parseInt(property));
	}
	property = properties.getProperty(encodingName
		+ Context.MAX_BITS_SUFFIX);
	setMaxSizeInBits(Integer.parseInt(property));
	try {
	    setInvalidCharacterHandler(InvalidCharacterHandlerBunch
		    .getInstance().getTwig(
			    properties.getProperty(encodingName
				    + Context.INVALID_CHARACTER_HANDLER)));
	} catch (InvalidCharacterHandlerLoadingException e) {
	    try {
		setInvalidCharacterHandler(InvalidCharacterHandlerBunch
			.getInstance()
			.getTwig(
				properties
					.getProperty(Context.DEFAULT_INVALID_CHARACTER_HANDLER)));
	    } catch (InvalidCharacterHandlerLoadingException e1) {
		throw new ExceptionInInitializerError();
	    }
	}
    }

    /**
     * Add alias to the array of names.
     * 
     * @param alias
     *                {@link String}, alias to be added
     */
    public final void addAlias(String alias) {
	String[] newNames = new String[names.length + 1];
	System.arraycopy(names, 0, newNames, 0, names.length);
	newNames[names.length] = alias;
	names = newNames;
    }

    /**
     * the encoding have this name as an alias.
     * 
     * @param alias
     *                {@link String}, alias to check
     * @return true if the alias is valid for this encoding, false if the alias
     *         is not supported
     */
    public final boolean asKnownAs(String alias) {
	for (int index = 0; index < names.length; index++) {
	    if (alias.equals(names[index])) {
		return true;
	    }
	}
	return false;
    }

    /**
     * @return the names
     */
    public final String[] getNames() {
	return names;
    }

    /**
     * @param names
     *                the names to set
     */
    public final void setNames(String[] names) {
	this.names = names;
    }

    /**
     * @return the variable
     */
    public final boolean isVariable() {
	return variable;
    }

    /**
     * @param variable
     *                the variable to set
     */
    public final void setVariable(boolean variable) {
	this.variable = variable;
    }

    /**
     * @return the sizeInBytes
     */
    public final int getMaxSizeInBytes() {
	return maxSizeInBytes;
    }

    /**
     * @param maxSizeInBytes
     *                the sizeInBytes to set
     */
    public final void setMaxSizeInBytes(int maxSizeInBytes) {
	this.maxSizeInBytes = maxSizeInBytes;
	this.maxSizeInBits = maxSizeInBytes * 8;
	if (!variable) {
	    this.minSizeInBytes = maxSizeInBytes;
	    this.minSizeInBits = maxSizeInBits;
	}
    }

    /**
     * @return the maxSizeInBits
     */
    public final int getMaxSizeInBits() {
	return maxSizeInBits;
    }

    /**
     * @param maxSizeInBits
     *                the sizeInBits to set
     */
    public final void setMaxSizeInBits(int maxSizeInBits) {
	this.maxSizeInBits = maxSizeInBits;
	if ((maxSizeInBits % 8) == 0) {
	    this.maxSizeInBytes = maxSizeInBits / 8;
	} else {
	    this.maxSizeInBytes = (maxSizeInBits / 8) + 1;
	}
	if (!variable) {
	    this.minSizeInBytes = maxSizeInBytes;
	    this.minSizeInBits = maxSizeInBits;
	}
    }

    /**
     * @return the minSizeInBytes
     */
    public final int getMinSizeInBytes() {
	return minSizeInBytes;
    }

    /**
     * @param minSizeInBytes
     *                the minSizeInBytes to set
     */
    public final void setMinSizeInBytes(int minSizeInBytes) {
	if (variable) {
	    this.minSizeInBytes = minSizeInBytes;
	    this.minSizeInBits = minSizeInBytes * 8;
	} else {
	    setMaxSizeInBytes(minSizeInBytes);
	}
    }

    /**
     * @return the minSizeInBits
     */
    public final int getMinSizeInBits() {
	return minSizeInBits;
    }

    /**
     * @param minSizeInBits
     *                the minSizeInBits to set
     */
    public final void setMinSizeInBits(int minSizeInBits) {
	if (variable) {
	    this.minSizeInBits = minSizeInBits;
	    if ((minSizeInBits % 8) == 0) {
		this.minSizeInBytes = minSizeInBits / 8;
	    } else {
		this.minSizeInBytes = (minSizeInBits / 8) + 1;
	    }
	} else {
	    setMaxSizeInBytes(minSizeInBits);
	}
    }

    /**
     * @param mappingFile
     *                {@link String}, file which contains the mapping
     */
    public void setMappingFile(String mappingFile) {
	this.mappingFile = mappingFile;
    }

    /**
     * @return the mappingFile
     */
    public final String getMappingFile() {
	if ((mappingFile == null) && (propertyEncodingName != null)) {
	    mappingFile = properties.getProperty(propertyEncodingName
		    + Context.MAPPING_FILE_SUFFIX);
	}
	return mappingFile;
    }

    /**
     * Getter method for invalidCharacterHandler.
     * 
     * @return the invalidCharacterHandler
     */
    public InvalidCharacterHandler getInvalidCharacterHandler() {
	return invalidCharacterHandler;
    }

    /**
     * Setter method for invalidCharacterHandler.
     * 
     * @param invalidCharacterHandler
     *                the invalidCharacterHandler to set
     */
    public void setInvalidCharacterHandler(
	    InvalidCharacterHandler invalidCharacterHandler) {
	this.invalidCharacterHandler = invalidCharacterHandler;
    }

    /**
     * Getter method for properties.
     * 
     * @return the properties
     */
    public Properties getProperties() {
	return properties;
    }

    /**
     * Getter method for mapper.
     * 
     * @return the mapper
     * @throws InvalidMapplerLoadingException
     *                 thrown if the mapper which is specified cannot be loaded,
     *                 or is not an mapper.
     */
    public Mapper getMapper() throws InvalidMapplerLoadingException {
	if ((mapper == null) && (propertyEncodingName != null)) {
	    mapper = MapperBunch.getInstance().getTwig(
		    properties.getProperty(propertyEncodingName
			    + Context.MAPPING_PARSER_SUFFIX));
	}
	return mapper;
    }

    /**
     * Setter method for mapper.
     * 
     * @param mapper
     *                the mapper to set
     */
    public void setMapper(Mapper mapper) {
	this.mapper = mapper;
    }
}
