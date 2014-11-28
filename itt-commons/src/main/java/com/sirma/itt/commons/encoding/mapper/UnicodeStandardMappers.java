/*
 * Created on 21.02.2008 @ 18:00:04
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.mapper;

import java.util.HashMap;

import com.sirma.itt.commons.encoding.translator.encoders.ArrayKey;

/**
 * Format of this Mapper is:
 * 
 * <pre>
 * #Comment
 * 0xYY	0xZZZZ	#Optional description
 * </pre>
 * 
 * where:
 * <ul>
 * <li>0xYY is number for the encoding. For example: 0xB3
 * <li>0xZZZZ is number for Unicode number. For example: 0x0403
 * <li>Description is optional for the mapping line
 * <li>File can contains comments. Comment lines start with #.
 * </ul>
 * Note:
 * 
 * @author Hristo Iliev
 * 
 */
public class UnicodeStandardMappers extends Mapper {

    /**
     * Initialize the Unicode standard mapper.
     * 
     * @param mapperName
     *                {@link String}, name of the mapper
     */
    public UnicodeStandardMappers(String mapperName) {
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
	if (trimmedLine.startsWith("#")) { //$NON-NLS-1$
	    return;
	}
	String[] parsed = line.split("\\s+"); //$NON-NLS-1$
	if (parsed.length < 2) {
	    throw new UnsupportedMappingException();
	}
	if (parsed[1].trim().length() == 0) {
	    return;
	}

	parsed[0] = parsed[0].substring(2);

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
