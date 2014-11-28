/*
 * Created on 08.02.2008 @ 21:20:38
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.mapper;

import java.util.HashMap;

import com.sirma.itt.commons.encoding.translator.encoders.ArrayKey;

/**
 * This class parse the mapping file with the following syntax:
 * 
 * <pre>
 * =XX	U+YYYY	Description
 * </pre>
 * 
 * where:
 * <ul>
 * <li> =XX is the character in specified ISO encoding
 * <li> U+YYYY is the UTF-8 representation of the character
 * <li> Description is the the name of the character
 * </ul>
 * Sections are separated with tab. <br/><br/>
 * 
 * @author Hristo Iliev
 */
public class DefaultMapper extends Mapper {

    /**
     * Initialize the default mapper.
     * 
     * @param mapperName
     *                {@link String}, name of the mapper
     */
    public DefaultMapper(String mapperName) {
	super(mapperName);
    }

    /**
     * {@inheritDoc}
     */
    protected void addTransformation(HashMap<ArrayKey, Integer> encodingTable,
	    HashMap<Integer, ArrayKey> reverseEncodingTable, String line)
	    throws UnsupportedMappingException {
	String trimmedLine = line.trim();
	if (trimmedLine.length() == 0) {
	    return;
	}
	String[] parsed = trimmedLine.split("\\s+"); //$NON-NLS-1$
	if (parsed.length < 3) {
	    throw new UnsupportedMappingException();
	}
	if (parsed[0].length() % 2 == 0) {
	    parsed[0] = "0" + parsed[0].substring(1); //$NON-NLS-1$
	} else {
	    parsed[0] = parsed[0].substring(1);
	}

	byte[] nativeInBytes = new byte[parsed[0].length() / 2];
	short nativeInShort;
	for (int index = 0; index < nativeInBytes.length; index++) {
	    int position = index << 1;
	    nativeInShort = Short.parseShort(parsed[0].substring(position,
		    position + 2), 16);
	    if (nativeInShort > 127) {
		nativeInBytes[index] = ((byte) (nativeInShort | 0x80));
	    } else {
		nativeInBytes[index] = ((byte) nativeInShort);
	    }
	}
	Integer unicodeNumber = new Integer(Integer.parseInt(parsed[1]
		.substring(2), 16));
	ArrayKey key = new ArrayKey(nativeInBytes);
	encodingTable.put(key, unicodeNumber);
	reverseEncodingTable.put(unicodeNumber, key);
    }
}
