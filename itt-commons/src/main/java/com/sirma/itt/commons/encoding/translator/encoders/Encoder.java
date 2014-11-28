/*
 * Created on 12.02.2008 @ 12:10:17
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.translator.encoders;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import com.sirma.itt.commons.encoding.mapper.InvalidMapplerLoadingException;
import com.sirma.itt.commons.encoding.mapper.Mapper;
import com.sirma.itt.commons.encoding.mapper.UnsupportedMappingException;
import com.sirma.itt.commons.encoding.metainfo.MetaInformation;
import com.sirma.itt.commons.encoding.translator.Translator;

/**
 * This class is base class of all encoders. Encoder use mapping file to create
 * the transformation mappings. For every specific group of encoding which share
 * same rules must be implemented an encoder.
 * 
 * @author Hristo Iliev
 * 
 */
public abstract class Encoder extends Translator {

    /** map from native to Unicode number. */
    private HashMap<ArrayKey, Integer> straight = new HashMap<ArrayKey, Integer>(
	    347);

    /** map from Unicode number to native. */
    private HashMap<Integer, ArrayKey> backward = new HashMap<Integer, ArrayKey>(
	    347);

    /**
     * Create encoder type of translator.
     * 
     * @param encodingName
     *                String, encoding name used in property file as property
     *                header
     */
    public Encoder(String encodingName) {
	super(encodingName);
	init();
    }

    /**
     * Create encoder type of translator.
     * 
     * @param metaInfo
     *                {@link MetaInformation}, information for the encoder
     */
    public Encoder(MetaInformation metaInfo) {
	super(metaInfo);
	init();
    }

    /**
     * Initialize the encoder specific parameters of the translator.
     */
    private void init() {
	Mapper mapper;
	try {
	    mapper = getMetaInfo().getMapper();
	} catch (InvalidMapplerLoadingException e) {
	    throw new ExceptionInInitializerError(e);
	}
	InputStream input = null;
	try {
	    try {
		/* tries to load the input from out file first */
		input = new FileInputStream(getMetaInfo().getMappingFile());
	    } catch (IOException e) {
		/* if fails then try to load from files in the jar */
		input = getClass().getClassLoader().getResourceAsStream(
			getMetaInfo().getMappingFile());
	    }
	    mapper.readEncoding(input, straight, backward);
	} catch (FileNotFoundException e) {
	    throw new ExceptionInInitializerError(e);
	} catch (IOException e) {
	    throw new ExceptionInInitializerError(e);
	} catch (UnsupportedMappingException e) {
	    throw new ExceptionInInitializerError(e);
	} finally {
	    if (input != null) {
		try {
		    input.close();
		} catch (IOException e) {
		    throw new ExceptionInInitializerError(e);
		}
	    }
	}
    }

    /**
     * Get map from the encoding to Unicode number.
     * 
     * @return the straight HashMap, the map
     */
    protected final HashMap<ArrayKey, Integer> getStraight() {
	return straight;
    }

    /**
     * Get map from Unicode number to the encoding.
     * 
     * @return the backward HashMap, the map
     */
    protected final HashMap<Integer, ArrayKey> getBackward() {
	return backward;
    }
}
