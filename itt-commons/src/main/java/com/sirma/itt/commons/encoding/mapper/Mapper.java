/*
 * Created on 08.02.2008 @ 21:00:48
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import com.sirma.itt.commons.encoding.translator.encoders.ArrayKey;

/**
 * Base class for mappers. All mappers which should read mapping files with
 * concrete syntax should inherit this class.
 * 
 * @author Hristo Iliev
 * 
 */
public abstract class Mapper {

    /** name of the mapper. */
    private final String mapperName;

    /**
     * Initialize the mapper with the name of the mapper. This name is used as
     * key in the Context to search the required values.
     * 
     * @param mapperName
     *                String, name of the mapper
     */
    public Mapper(String mapperName) {
	this.mapperName = mapperName;
	MapperBunch.getInstance().put(mapperName, this);
    }

    /**
     * Add encoding from the specified input stream. Every token must be in
     * separate row of the String input. The encoding is put in the encoding
     * table as the name specified by the encodingName parameter.
     * 
     * @param encodingMapping
     *                InputStream, input stream where will be read the mappings
     *                of characters.
     * @param encodings
     *                {@link HashMap}, map with the encodings from specific
     *                encoding to Unicode number
     * @param reverseEncoding
     *                {@link HashMap}, map with the encodings from Unicode
     *                number to the specific encoding
     * @throws IOException
     *                 thrown if some IO error with the input stream is
     *                 encountered
     * @throws UnsupportedMappingException
     *                 thrown if the syntax of the file differs with the syntax
     *                 required byte the mapper
     */
    public final void readEncoding(InputStream encodingMapping,
	    HashMap<ArrayKey, Integer> encodings,
	    HashMap<Integer, ArrayKey> reverseEncoding) throws IOException,
	    UnsupportedMappingException {
	BufferedReader reader = new BufferedReader(new InputStreamReader(
		encodingMapping));
	String line;
	while ((line = reader.readLine()) != null) {
	    addTransformation(encodings, reverseEncoding, line);
	}
    }

    /**
     * Add an transformation to maps. Parameter <code>line</code> is a single
     * mapping which should be parsed according concrete syntax and the mapped
     * character(s) should be inserted in both hash map. In
     * <code>encodingTable</code> should be added transformations from the
     * encoding to Unicode number and in <code>reverseEncodingTable</code>
     * should be added transformations from Unicode number to the encoding
     * 
     * @param encodingTable
     *                HashMap, map where should be added transformations from
     *                encoding to Unicode number
     * @param reverseEncodingTable
     *                HashMap, map where should be added transformations from
     *                Unicode number to the encoding
     * @param line
     *                String, line which contains the information for a concrete
     *                mapping
     * @throws UnsupportedMappingException
     *                 thrown if the line cannot be parsed according the syntax
     */
    protected abstract void addTransformation(
	    HashMap<ArrayKey, Integer> encodingTable,
	    HashMap<Integer, ArrayKey> reverseEncodingTable, String line)
	    throws UnsupportedMappingException;

    /**
     * Getter for mapper name.
     * 
     * @return name of the mapper
     */
    public String getMapperName() {
	return mapperName;
    }

}
