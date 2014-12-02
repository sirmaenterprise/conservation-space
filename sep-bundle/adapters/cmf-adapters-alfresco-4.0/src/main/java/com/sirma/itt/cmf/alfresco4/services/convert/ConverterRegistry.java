package com.sirma.itt.cmf.alfresco4.services.convert;

import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Plugin;
import com.sirma.itt.emf.util.Documentation;

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
