/*
 * Created on 12.02.2008 @ 17:25:41
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.invalidhandler;

import com.sirma.itt.commons.bunch.Bunch;
import com.sirma.itt.commons.encoding.context.Context;

/**
 * Bunch of handlers of invalid characters. This bunch is used as base point to
 * retrieve concrete handler implementation. Handlers should be searched by its
 * keys in {@link Context}.
 * 
 * @author Hristo Iliev
 * 
 */
public final class InvalidCharacterHandlerBunch
	extends
	Bunch<String, InvalidCharacterHandler, InvalidCharacterHandlerLoadingException> {

    /**
     * Comment for serialVersionUID.
     */
    private static final long serialVersionUID = -1025745570311765655L;

    /** singleton instance. */
    private static InvalidCharacterHandlerBunch instance = new InvalidCharacterHandlerBunch();

    /**
     * Singleton constructor used to initialize the parameters.
     */
    private InvalidCharacterHandlerBunch() {
	super();
    }

    /**
     * Retrieve instance of {@link InvalidCharacterHandlerBunch} bunch.
     * 
     * @return {@link InvalidCharacterHandlerBunch}, retrieved bunch
     */
    public static InvalidCharacterHandlerBunch getInstance() {
	return instance;
    }

    @Override
    protected String createTwigKey(String twigName, InvalidCharacterHandler twig) {
	return twigName;
    }

    @Override
    protected String getTwigKey(String twigName) {
	return twigName;
    }

    @Override
    protected String getTwigClass(String twigName) {
	return Context.getContext().getProperty(twigName);
    }

    @Override
    protected boolean isValidTwigClass(Class<?> twigClass) {
	return InvalidCharacterHandler.class.isAssignableFrom(twigClass);
    }

    @Override
    protected InvalidCharacterHandler manageException(String twigName,
	    Exception cause) throws InvalidCharacterHandlerLoadingException {
	if (cause != null) {
	    throw new InvalidCharacterHandlerLoadingException(cause);
	}
	throw new InvalidCharacterHandlerLoadingException();
    }
}
