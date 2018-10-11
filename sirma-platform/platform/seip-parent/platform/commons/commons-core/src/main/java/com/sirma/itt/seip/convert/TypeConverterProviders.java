/**
 *
 */
package com.sirma.itt.seip.convert;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Wraps all injectable converters and provides method to register a type converter to them
 *
 * @author BBonev
 */
public class TypeConverterProviders {
	@Inject
	@Any
	private Instance<TypeConverterProvider> converters;

	/**
	 * Register converters in the specified {@link TypeConverter}
	 *
	 * @param converter
	 *            the converter
	 */
	public void register(TypeConverter converter) {
		converters.iterator().forEachRemaining((e) -> e.register(converter));
	}

}
