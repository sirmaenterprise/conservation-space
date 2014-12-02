package com.sirma.itt.emf.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.exceptions.TypeConversionException;

/*
 * The class is annotated @EmptyConverter to remove the class from default selection but to scan it
 * for producers
 */
/**
 * Support for generic conversion between types. Additional conversions may be added. Direct
 * conversion and two stage conversions via Number are supported. We do not support conversion by
 * any route at the moment
 */
@ApplicationScoped
@EmptyConverter
public class TypeConverterImpl implements TypeConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(TypeConverterImpl.class);

	private static TypeConverter instance = new TypeConverterImpl();

	/** Map of conversion. */
	@SuppressWarnings("rawtypes")
	private Map<Class, Map<Class, Converter>> conversions = new HashMap<>(512);

	/**
	 * Gets the converter instance for CDI access. The instance is used for injection in other beans
	 *
	 * @return the converter instance
	 */
	@Produces
	public TypeConverter getConverterInstance() {
		return instance;
	}

	@Override
	public Object convert(DataTypeDefinition propertyType, Object value) {

		// Convert property type to java class
		Class<?> javaClass = propertyType.getJavaClass();

		return convert(javaClass, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T convert(Class<T> c, Object value) {
		if (value == null) {
			return null;
		}

		// Primative types
		if (c.isPrimitive()) {
			// We can not suport primitive type conversion
			throw new TypeConversionException("Can not convert direct to primitive type "
					+ c.getName());
		}

		// Check if we already have the correct type
		if (c.isInstance(value)) {
			return c.cast(value);
		}

		// Find the correct conversion - if available and do the converiosn
		Converter converter = getConverter(value, c);
		if (converter == null) {
			throw new TypeConversionException("There is no conversion registered for the value: \n"
					+ "   value class: " + value.getClass().getName() + "\n" + "   to class: "
					+ c.getName() + "\n" + "   value: " + value);
		}

		return (T) converter.convert(value);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection convert(DataTypeDefinition propertyType, Object[] values) {
		if (values == null) {
			return convert(propertyType, (Collection) null);
		}
		// Turn the array into a Collection, then convert as that
		List c = new ArrayList(values.length);
		for (Object v : values) {
			c.add(v);
		}
		// Convert
		return convert(propertyType, c);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection convert(DataTypeDefinition propertyType, Collection values) {

		// Convert property type to java class
		Class javaClass = propertyType.getJavaClass();

		return convert(javaClass, values);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public <T> Collection<T> convert(Class<T> c, Collection values) {
		if (values == null) {
			return null;
		}

		Collection<T> converted = new ArrayList<>(values.size());
		for (Object value : values) {
			converted.add(convert(c, value));
		}

		return converted;
	}

	@Override
	public boolean booleanValue(Object value) {
		return convert(Boolean.class, value).booleanValue();
	}

	@Override
	public char charValue(Object value) {
		return convert(Character.class, value).charValue();
	}

	@Override
	public byte byteValue(Object value) {
		if (value instanceof Number) {
			return ((Number) value).byteValue();
		}
		return convert(Byte.class, value).byteValue();
	}

	@Override
	public short shortValue(Object value) {
		if (value instanceof Number) {
			return ((Number) value).shortValue();
		}
		return convert(Short.class, value).shortValue();
	}

	@Override
	public int intValue(Object value) {
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		return convert(Integer.class, value).intValue();
	}

	@Override
	public long longValue(Object value) {
		if (value instanceof Number) {
			return ((Number) value).longValue();
		}
		return convert(Long.class, value).longValue();
	}

	@Override
	public float floatValue(Object value) {
		if (value instanceof Number) {
			return ((Number) value).floatValue();
		}
		return convert(Float.class, value).floatValue();
	}

	@Override
	public double doubleValue(Object value) {
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		return convert(Double.class, value).doubleValue();
	}

	@Override
	public boolean isMultiValued(Object value) {
		return value instanceof Collection;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public int size(Object value) {
		if (value instanceof Collection) {
			return ((Collection) value).size();
		}
		return 1;
	}

	/**
	 * Get a collection for the passed value.
	 *
	 * @param value
	 *            the value
	 * @return the collection
	 */
	private Collection<?> createCollection(Object value) {
		Collection<?> coll;
		if (isMultiValued(value)) {
			coll = (Collection<?>) value;
		} else {
			List<Object> list = new ArrayList<>(1);
			list.add(value);
			coll = list;
		}
		return coll;
	}

	@Override
	public <T> Collection<T> getCollection(Class<T> c, Object value) {
		Collection<?> coll = createCollection(value);
		return convert(c, coll);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public <F, T> void addConverter(Class<F> source, Class<T> destination, Converter<F, T> converter) {
		LOGGER.trace("Registering converter {} from type {} to type {}", converter.getClass(),
				source, destination);
		Map<Class, Converter> map = conversions.get(source);
		if (map == null) {
			map = new HashMap<>();
			conversions.put(source, map);
		}
		Converter oldConverter = map.get(destination);
		if (oldConverter != null) {
			LOGGER.trace("Overriding converter {} for types from {} to {} with {}", oldConverter,
					source, destination, converter);
		}
		map.put(destination, converter);
	}

	@Override
	public <F, I, T> Converter<F, T> addDynamicTwoStageConverter(Class<F> source,
			Class<I> intermediate, Class<T> destination) {
		Converter<F, T> converter = new TypeConverterImpl.DynamicTwoStageConverter<>(source,
				intermediate, destination);
		addConverter(source, destination, converter);
		return converter;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> Converter getConverter(Object value, Class<T> dest) {
		Converter converter = null;
		if (value == null) {
			return null;
		}

		// find via class of value
		Class valueClass = value.getClass();
		converter = getConverter(valueClass, dest);
		if (converter != null) {
			return converter;
		}

		// find via supported interfaces of value
		do {
			Class[] ifClasses = valueClass.getInterfaces();
			for (Class ifClass : ifClasses) {
				converter = getConverter(ifClass, dest);
				if (converter != null) {
					return converter;
				}
			}
			valueClass = valueClass.getSuperclass();
		} while (valueClass != null);

		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public <F, T> Converter getConverter(Class<F> source, Class<T> dest) {
		Converter<?, ?> converter = null;
		Class clazz = source;
		do {
			Map<Class, Converter> map = conversions.get(clazz);
			if (map == null) {
				continue;
			}
			converter = map.get(dest);

			if (converter == null) {
				// attempt to establish converter from source to dest via Number
				Converter<?, ?> first = map.get(Number.class);
				Converter<?, ?> second = null;
				if (first != null) {
					map = conversions.get(Number.class);
					if (map != null) {
						second = map.get(dest);
					}
				}
				if (second != null) {
					converter = new TwoStageConverter<F, T>(first, second);
				}
			}
		} while ((converter == null) && ((clazz = clazz.getSuperclass()) != null));

		return converter;
	}

	// Support for pluggable conversions

	/**
	 * Support for chaining conversions.
	 *
	 * @param <F>
	 *            From Type
	 * @param <I>
	 *            Intermediate type
	 * @param <T>
	 *            To Type
	 * @author David Caruana
	 */
	protected class DynamicTwoStageConverter<F, I, T> implements Converter<F, T> {

		/** The from. */
		Class<F> from;

		/** The intermediate. */
		Class<I> intermediate;

		/** The to. */
		Class<T> to;

		/**
		 * Instantiates a new dynamic two stage converter.
		 *
		 * @param from
		 *            the from
		 * @param intermediate
		 *            the intermediate
		 * @param to
		 *            the to
		 */
		DynamicTwoStageConverter(Class<F> from, Class<I> intermediate, Class<T> to) {
			this.from = from;
			this.intermediate = intermediate;
			this.to = to;
		}

		/**
		 * Gets the from.
		 *
		 * @return from class
		 */
		public Class<F> getFrom() {
			return from;
		}

		/**
		 * Gets the intermediate.
		 *
		 * @return intermediate class
		 */
		public Class<I> getIntermediate() {
			return intermediate;
		}

		/**
		 * Gets the to.
		 *
		 * @return to class
		 */
		public Class<T> getTo() {
			return to;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public T convert(F source) {
			Converter iConverter = TypeConverterImpl.this.getConverter(from, intermediate);
			Converter tConverter = TypeConverterImpl.this.getConverter(intermediate, to);
			if ((iConverter == null) || (tConverter == null)) {
				throw new TypeConversionException("Cannot convert from " + from.getName() + " to "
						+ to.getName());
			}

			Object iValue = iConverter.convert(source);
			// no need to continue conversion - nothing to convert
			if (iValue == null) {
				return null;
			}
			return (T) tConverter.convert(iValue);
		}
	}

}
