/**
 * Copyright (c) 2009 25.04.2009 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.utils.stream;

import java.io.InputStream;

import com.sirma.itt.commons.utils.encoding.UTF8Utils;

/**
 * Implementation of {@link BOMSkipInputStream} for UTF-8 streams.
 * 
 * @author Hristo Iliev
 */
public final class UTF8BOMSkipInputStream extends BOMSkipInputStream {

    /**
     * Initialize the wrapped stream.
     * 
     * @param in
     *            {@link InputStream}, the wrapped stream
     */
    public UTF8BOMSkipInputStream(InputStream in) {
	super(in);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int bomSizeInBytes() {
	return 3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int isBom(byte[] bom) {
	if (UTF8Utils.startWithBOM(bom)) {
	    return 3;
	}
	return 0;
    }

}
