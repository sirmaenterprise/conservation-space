/**
 * Copyright (c) 2008 03.09.2008 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.encoding.mapper;

import com.sirma.itt.commons.bunch.Bunch;
import com.sirma.itt.commons.encoding.context.Context;

/**
 * Bunch of mappers. This bunch is used as base point to retrieve concrete
 * mapper implementation. Mappers should be searched by its keys in
 * {@link Context}.
 * 
 * @author Hristo Iliev
 */
public final class MapperBunch extends
	Bunch<String, Mapper, InvalidMapplerLoadingException> {

    /**
     * Comment for serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** Singleton implementation of the mapper bunch. */
    private static MapperBunch mapperBunch = new MapperBunch();

    /**
     * Singleton implementation of the mapper.
     */
    private MapperBunch() {
	// Singleton implementation
    }

    /**
     * Retrieve the instance of the {@link MapperBunch}.
     * 
     * @return {@link MapperBunch}, instance of {@link MapperBunch}
     */
    public static MapperBunch getInstance() {
	return mapperBunch;
    }

    @Override
    protected String createTwigKey(String twigName, Mapper twig) {
	return twigName;
    }

    @Override
    protected String getTwigClass(String twigName) {
	return Context.getContext().getProperty(twigName);
    }

    @Override
    protected String getTwigKey(String twigName) {
	return twigName;
    }

    @Override
    protected boolean isValidTwigClass(Class<?> twigClass) {
	return Mapper.class.isAssignableFrom(twigClass);
    }

    @Override
    protected Mapper manageException(String twigName, Exception cause)
	    throws InvalidMapplerLoadingException {
	if (cause != null) {
	    throw new InvalidMapplerLoadingException(cause);
	}
	throw new InvalidMapplerLoadingException();
    }

}
