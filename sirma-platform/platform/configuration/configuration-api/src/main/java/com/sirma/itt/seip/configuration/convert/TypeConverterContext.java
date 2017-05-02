package com.sirma.itt.seip.configuration.convert;

import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;

/**
 * Base interface for configuration value conversion. It's base type for the one of the 2 sub types depending on the
 * converted configuration. A converter method could have injected one of the interfaces {@link ConverterContext} or
 * {@link GroupConverterContext}.
 *
 * @author BBonev
 * @see ConfigurationConverter
 */
public interface TypeConverterContext {

	/**
	 * Gets the configuration definition that is being converted/produced.
	 *
	 * @return the configuration instance.
	 */
	ConfigurationInstance getConfiguration();
}
