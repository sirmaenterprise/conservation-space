/*
 * Created on 12.02.2008 @ 12:17:36
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.translator.engine;

import com.sirma.itt.commons.encoding.metainfo.MetaInformation;
import com.sirma.itt.commons.encoding.translator.Translator;

/**
 * This class is base class of engines. Engine is a translator from one encoding
 * to another which does not require a mapping file but it transform the
 * character via some algorithm. Such an engine is UTF-8 which generate
 * characters via transformation algorithm.
 * 
 * @author Hristo Iliev
 * 
 */
public abstract class Engine extends Translator {

    /**
     * Engine specific initialization.
     * 
     * @param encodingName
     *                {@link String}, name of the encoding
     */
    public Engine(String encodingName) {
	super(encodingName);
    }

    /**
     * Engine specific initialization.
     * 
     * @param metaInfo
     *                {@link MetaInformation}, information
     */
    public Engine(MetaInformation metaInfo) {
	super(metaInfo);
    }
}
