/*
 * Created on 12.02.2008 @ 14:23:27
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.translator;

import com.sirma.itt.commons.bunch.TwigLoadingException;

/**
 * This exception is thrown if the translator cannot be loaded.
 * 
 * @author Hristo Iliev
 * 
 */
public class TranslatorLoadingException extends TwigLoadingException {

    /** Name of the translator. */
    private String translator;

    /** serial version ID. */
    private static final long serialVersionUID = 6692064060942073066L;

    /**
     * @param translatorName
     *                String, name of the translator
     * 
     */
    public TranslatorLoadingException(String translatorName) {
	translator = translatorName;
    }

    /**
     * @param translatorName
     *                String, name of the translator
     * @param message
     *                message for exception
     */
    public TranslatorLoadingException(String translatorName, String message) {
	super(message);
	translator = translatorName;
    }

    /**
     * @param translatorName
     *                String, name of the translator
     * @param cause
     *                throwable which cause this exception
     */
    public TranslatorLoadingException(String translatorName, Throwable cause) {
	super(cause);
	translator = translatorName;
    }

    /**
     * @param translatorName
     *                String, name of the translator
     * @param message
     *                message for exception
     * @param cause
     *                throwable which cause this exception
     */
    public TranslatorLoadingException(String translatorName, String message,
	    Throwable cause) {
	super(message, cause);
	translator = translatorName;
    }

    /**
     * @return the translator
     */
    public final String getTranslator() {
	return translator;
    }

}
