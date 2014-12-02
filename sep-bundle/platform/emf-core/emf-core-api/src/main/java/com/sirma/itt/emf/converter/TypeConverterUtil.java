package com.sirma.itt.emf.converter;

import com.sirma.itt.emf.exceptions.EmfConfigurationException;

/**
 * Utility class that provides a singleton/static access to {@link TypeConverter} functionality.
 * NOTE: the class is initialized via {@link ConverterInitializer}
 * 
 * @author BBonev
 */
public class TypeConverterUtil {

	/**
	 * Instantiates a new type converter util.
	 */
	private TypeConverterUtil() {
		// disable instance creation
	}

	/** The type converter. */
	private static TypeConverter typeConverter;

	/**
	 * Gets the converter. If no converter implementation is found then the method will throw a
	 * {@link EmfConfigurationException}
	 * 
	 * @return the converter
	 */
	public static TypeConverter getConverter() {
		if (typeConverter == null) {
			throw new EmfConfigurationException("TypeConverter implementation not found!");
		}
		return typeConverter;
	}

	/**
	 * Setter method for typeConverter.
	 * 
	 * @param typeConverter
	 *            the typeConverter to set
	 */
	static void setTypeConverter(TypeConverter typeConverter) {
		TypeConverterUtil.typeConverter = typeConverter;
	}

}
