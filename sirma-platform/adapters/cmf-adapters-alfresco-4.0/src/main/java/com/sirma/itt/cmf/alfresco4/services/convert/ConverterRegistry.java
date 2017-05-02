package com.sirma.itt.cmf.alfresco4.services.convert;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * The Interface ConverterRegistry.
 */
@Documentation("The Interface ConverterRegistry is extension for applicable convertor for instance.")
public interface ConverterRegistry extends Plugin {

	/** The target name. */
	String TARGET_NAME = "ModelConvertorForInstance";

	/**
	 * Gets the name for converter to obtain by the provided class.
	 *
	 * @param type
	 *            the type of instance to get converter for
	 * @return the name for supported type or null if current registry doesn't recognize the class
	 */
	String getNameForSupportedType(Class<? extends Instance> type);

}
