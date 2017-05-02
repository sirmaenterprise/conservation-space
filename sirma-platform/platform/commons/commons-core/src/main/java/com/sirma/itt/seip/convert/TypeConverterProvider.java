package com.sirma.itt.seip.convert;

/**
 * Provider interface for custom conversions. The defined method is called to register the implemented class as
 * converter for the given instance. <br>
 * REVIEW: probably good idea to convert it to plugin.
 *
 * @author BBonev
 */
public interface TypeConverterProvider {

	/**
	 * Register the converter to the given {@link TypeConverter}
	 *
	 * @param converter
	 *            the target converter
	 */
	void register(TypeConverter converter);

}
