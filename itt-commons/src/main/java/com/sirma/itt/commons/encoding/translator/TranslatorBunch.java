/*
 * Created on 12.02.2008 @ 14:05:21
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.translator;

import java.util.Set;

import com.sirma.itt.commons.bunch.Bunch;
import com.sirma.itt.commons.context.AbstractContext;
import com.sirma.itt.commons.encoding.context.Context;
import com.sirma.itt.commons.encoding.metainfo.MetaInformation;

/**
 * This class represent the bunch of the translator. The use of this class is to
 * retrieve a translator according the property passed as encoding name. If the
 * encoding translator already exists, TranslatorBunch does not create a new
 * one, but use the old one. No synchronization is required because the
 * translator core supports multithreading.
 * 
 * Translators should be searched by one of its names. For example to load the
 * translator for ISO-8859-1, the twig name by witch will be searched should be
 * ISO-8859-1.
 * 
 * @author Hristo Iliev
 * 
 */
public final class TranslatorBunch extends
	Bunch<MetaInformation, Translator, TranslatorLoadingException> {

    /**
     * Comment for serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** singleton implementation of the translator bunch. */
    private static TranslatorBunch instance;

    static {
	new TranslatorBunch();
    }

    /**
     * Singleton bunch of translator.
     */
    private TranslatorBunch() {
	AbstractContext context = Context.getContext();
	String autoload = context.getProperty(Context.AUTOLOAD);
	String[] encodigs = autoload.split("\\s+"); //$NON-NLS-1$
	instance = this;
	for (int i = 0; i < encodigs.length; i++) {
	    try {
		getTwig(encodigs[i]);
	    } catch (TranslatorLoadingException e) {
		e.printStackTrace();
	    }
	}
    }

    /**
     * Return the instance of Translator bunch.
     * 
     * @return TranslatorBunch, the translator bunch
     */
    public static TranslatorBunch getInstance() {
	return instance;
    }

    /**
     * Retrieve a meta-information according the translator name. The translator
     * name can be either the name used for loading the encoding information or
     * one of its aliases.
     * 
     * @param encoderName
     *                String, one of the names of the encoder
     * @return {@link MetaInformation}, the meta information of the translator
     */
    protected MetaInformation getTwigKey(String encoderName) {
	Set<MetaInformation> metaSet = keySet();
	for (MetaInformation metaInfo : metaSet) {
	    if (metaInfo.asKnownAs(encoderName)) {
		return metaInfo;
	    }
	}
	return null;
    }

    @Override
    protected MetaInformation createTwigKey(String twigName, Translator twig) {
	return twig.getMetaInfo();
    }

    @Override
    protected String getTwigClass(String twigName) {
	return Context.getContext().getProperty(
		twigName + Context.TRANSLATOR_CLASS);
    }

    @Override
    protected boolean isValidTwigClass(Class<?> twigClass) {
	return Translator.class.isAssignableFrom(twigClass);
    }

    @Override
    protected Translator manageException(String twigName, Exception cause)
	    throws TranslatorLoadingException {
	if (cause != null) {
	    throw new TranslatorLoadingException(twigName, cause);
	}
	throw new TranslatorLoadingException(twigName);
    }

}
