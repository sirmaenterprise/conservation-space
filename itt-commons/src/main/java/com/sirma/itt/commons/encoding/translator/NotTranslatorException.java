/*
 * Created on 12.02.2008 @ 14:20:14
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.translator;

/**
 * Thrown if the specified class is not subclass of Translator.
 * 
 * @author Hristo Iliev
 * 
 */
public class NotTranslatorException extends Exception {

    /** serial version ID. */
    private static final long serialVersionUID = 1053819149598361985L;
    /** class which supposed to be translator. */
    private Class<?> translatorClass;

    /**
     * 
     */
    public NotTranslatorException() {
	super();
    }

    /**
     * @param message
     *                message for exception
     * @param cause
     *                throwable which cause this exception
     */
    public NotTranslatorException(String message, Throwable cause) {
	super(message, cause);
    }

    /**
     * @param message
     *                message for exception
     */
    public NotTranslatorException(String message) {
	super(message);
    }

    /**
     * @param cause
     *                throwable which cause this exception
     */
    public NotTranslatorException(Throwable cause) {
	super(cause);
    }

    /**
     * Construct exception with information for the class which were supposed to
     * be {@link Translator} but it is not.
     * 
     * @param translatorClass
     *                Class<?> class which supposed to be translator
     */
    public NotTranslatorException(Class<?> translatorClass) {
	this.translatorClass = translatorClass;
    }

    /**
     * Getter method for translatorClass.
     * 
     * @return the translatorClass
     */
    public Class<?> getTranslatorClass() {
	return translatorClass;
    }

}
