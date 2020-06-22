package com.sirma.itt.seip.convert;

import java.util.Collection;
import java.util.Date;

/**
 * Support for generic conversion between types. Additional conversions may be added. Direct conversion and two stage
 * conversions via Number are supported.
 * <p>
 * <b>NOTE:</b> When new converter is registered the source class could always be instantiatable class and not an
 * interface or class. The destination class could be any class that will be used for conversion. Example:
 * <ul>
 * <li>Invalid registration: <code>converter.addConverter(List.class, String.class, someConverterImplementation)</code>
 * <li>Valid registration
 * <code>converter.addConverter(ArrayList.class, String.class, someConverterImplementation)</code>
 * <li>Valid registration <code>converter.addConverter(String.class, List.class, someConverterImplementation)</code>
 * </ul>
 * <p>
 * For non CDI context or where injection is impossible can be used {@link TypeConverterUtil#getConverter()}
 * <p>
 * For injection in Serializable bean/context use:
 *
 * <pre>
 * <code>@Inject @SerializableConverter
 * private TypeConverter converter;</code>
 * </pre>
 *
 * @author BBonev
 * @see TypeConverterUtil
 * @see SerializableConverter
 */
public interface TypeConverter {

	/**
	 * General conversion method to Object types (note it cannot support conversion to primary types due the
	 * restrictions of reflection. Use the specific conversion methods to primitive types). The method will throw a
	 * {@link com.sirma.itt.seip.convert.TypeConversionException} if there is no converter for the given source and
	 * destination types.
	 *
	 * @param <T>
	 *            The target type for the result of the conversion
	 * @param c
	 *            a class for the target type
	 * @param value
	 *            the value to be converted
	 * @return the converted value as the correct type or <code>null</code> if source value is <code>null</code>
	 */
	<T> T convert(Class<T> c, Object value);

	/**
	 * General conversion method to Object types (note it cannot support conversion to primary types due the
	 * restrictions of reflection. Use the specific conversion methods to primitive types). The method will not throw an
	 * exception if there is no converter for the given source and destination type but will return <code>null</code>.
	 *
	 * @param <T>
	 *            The target type for the result of the conversion
	 * @param c
	 *            a class for the target type
	 * @param value
	 *            the value to be converted
	 * @return the converted value as the correct type or <code>null</code> if source value is <code>null</code> or the
	 *         types are not supported.
	 */
	<T> T tryConvert(Class<T> c, Object value);

	/**
	 * General conversion method to convert collection contents to the specified type.
	 *
	 * @param <T>
	 *            The target type for the result of the conversion
	 * @param c
	 *            a class for the target type
	 * @param values
	 *            the values
	 * @return the converted collection
	 */
	@SuppressWarnings("rawtypes")
	<T> Collection<T> convert(Class<T> c, Collection values);

	/**
	 * Get the boolean value for the value object May have conversion failure.
	 *
	 * @param value
	 *            the value
	 * @return true, if successful
	 */
	boolean booleanValue(Object value);

	/**
	 * Get the char value for the value object May have conversion failure.
	 *
	 * @param value
	 *            the value
	 * @return the char
	 */
	char charValue(Object value);

	/**
	 * Get the byte value for the value object May have conversion failure.
	 *
	 * @param value
	 *            the value
	 * @return the byte
	 */
	byte byteValue(Object value);

	/**
	 * Get the short value for the value object May have conversion failure.
	 *
	 * @param value
	 *            the value
	 * @return the short
	 */
	short shortValue(Object value);

	/**
	 * Get the int value for the value object May have conversion failure.
	 *
	 * @param value
	 *            the value
	 * @return the int
	 */
	int intValue(Object value);

	/**
	 * Get the long value for the value object May have conversion failure.
	 *
	 * @param value
	 *            the value
	 * @return the long
	 */
	long longValue(Object value);

	/**
	 * Get the boolean value for the value object May have conversion failure.
	 *
	 * @param value
	 *            the value
	 * @return the float
	 */
	float floatValue(Object value);

	/**
	 * Get the boolean value for the value object May have conversion failure.
	 *
	 * @param value
	 *            the value
	 * @return the double
	 */
	double doubleValue(Object value);

	/**
	 * Converts the given object as {@link String}. Same as calling {@link #convert(Class, Object)} with {@link String}
	 * as first argument.
	 *
	 * @param value
	 *            the value
	 * @return the string value or <code>null</code>.
	 */
	String stringValue(Object value);

	/**
	 * Converts the given object as {@link Date}. Same as calling {@link #convert(Class, Object)} with {@link Date} as
	 * first argument.
	 *
	 * @param value
	 *            the value
	 * @return the date value or <code>null</code>
	 */
	Date dateValue(Object value);

	/**
	 * Is the value multi valued.
	 *
	 * @param value
	 *            the value
	 * @return true - if the underlying is a collection of values and not a single value
	 */
	boolean isMultiValued(Object value);

	/**
	 * Get the number of values represented.
	 *
	 * @param value
	 *            the value
	 * @return 1 for normal values and the size of the collection for MVPs
	 */
	int size(Object value);

	/**
	 * Get a collection for the passed value converted to the specified type.
	 *
	 * @param <T>
	 *            the generic type
	 * @param c
	 *            the c
	 * @param value
	 *            the value
	 * @return the collection
	 */
	<T> Collection<T> getCollection(Class<T> c, Object value);

	/**
	 * Add a converter to the list of those available.
	 *
	 * @param <F>
	 *            the generic type
	 * @param <T>
	 *            the generic type
	 * @param source
	 *            the source
	 * @param destination
	 *            the destination
	 * @param converter
	 *            the converter
	 */
	<F, T> void addConverter(Class<F> source, Class<T> destination, Converter<F, T> converter);

	/**
	 * Add a dynamic two stage converter.
	 *
	 * @param <F>
	 *            from
	 * @param <I>
	 *            intermediate
	 * @param <T>
	 *            to
	 * @param source
	 *            the source
	 * @param intermediate
	 *            the intermediate
	 * @param destination
	 *            the destination
	 * @return the converter
	 */
	<F, I, T> Converter<F, T> addDynamicTwoStageConverter(Class<F> source, Class<I> intermediate, Class<T> destination);

	/**
	 * Find conversion for the specified object Note: Takes into account the class of the object and any interfaces it
	 * may also support.
	 *
	 * @param <T>
	 *            the generic type
	 * @param value
	 *            the value
	 * @param dest
	 *            the dest
	 * @return the converter
	 */
	@SuppressWarnings("rawtypes")
	<T> Converter getConverter(Object value, Class<T> dest);

	/**
	 * Find a conversion for a specific Class.
	 *
	 * @param <F>
	 *            the generic type
	 * @param <T>
	 *            the generic type
	 * @param source
	 *            the source
	 * @param dest
	 *            the dest
	 * @return the converter
	 */
	@SuppressWarnings("rawtypes")
	<F, T> Converter getConverter(Class<F> source, Class<T> dest);

}