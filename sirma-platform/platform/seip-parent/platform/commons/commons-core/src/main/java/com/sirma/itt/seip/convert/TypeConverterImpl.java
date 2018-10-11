package com.sirma.itt.seip.convert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.annotation.NoOperation;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.tasks.TransactionMode;

/*
 * The class is annotated @@NoOperation to remove the class from default selection but to scan it for producers
 */
/**
 * Support for generic conversion between types. Additional conversions may be added. Direct conversion and two stage
 * conversions via Number are supported. We do not support conversion by any route at the moment
 */
@ApplicationScoped
@NoOperation
public class TypeConverterImpl implements TypeConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(TypeConverterImpl.class);

	private static TypeConverter instance = new TypeConverterImpl();

	/** Map of conversion. */
	@SuppressWarnings("rawtypes")
	private Map<Class, Map<Class, Converter>> conversions = new HashMap<>(512);

	/**
	 * Trigger converter initialization
	 *
	 * @param converters
	 *            the converters
	 */
	@Startup(phase = StartupPhase.DEPLOYMENT, order = 0, transactionMode = TransactionMode.NOT_SUPPORTED)
	static void initializeConverter(TypeConverterProviders converters) {
		LOGGER.info("Initializing type converter");
		// register all available providers
		converters.register(instance);
	}

	/**
	 * Gets the converter instance for CDI access. The instance is used for injection in other beans
	 *
	 * @return the converter instance
	 */
	@Produces
	@SuppressWarnings("static-method")
	public TypeConverter getConverterInstance() {
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T convert(Class<T> c, Object value) {
		return convertInternal(c, value, true);
	}

	/**
	 * Convert internal.
	 *
	 * @param <T>
	 *            the generic type
	 * @param c
	 *            the c
	 * @param value
	 *            the value
	 * @param errorOnMissingConverter
	 *            the error on missing converter
	 * @return the t
	 */
	private <T> T convertInternal(Class<T> c, Object value, boolean errorOnMissingConverter) {
		if (value == null) {
			return null;
		}

		// Primative types
		if (c.isPrimitive()) {
			// We can not suport primitive type conversion
			throw new TypeConversionException("Can not convert direct to primitive type " + c.getName());
		}

		// Check if we already have the correct type
		if (c.isInstance(value)) {
			return c.cast(value);
		}

		return convertOrError(c, value, errorOnMissingConverter);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> T convertOrError(Class<T> c, Object value, boolean errorOnMissingConverter) {
		// Find the correct conversion - if available and do the conversion
		Converter converter = getConverter(value, c);
		if (converter == null) {
			if (errorOnMissingConverter) {
				throw new TypeConversionException("There is no conversion registered for the value: \n"
						+ "   value class: " + value.getClass().getName() + "\n" + "   to class: " + c.getName() + "\n"
						+ "   value: " + value);
			}
			LOGGER.trace("There is no conversion registered for the value class: {} to class: {}\n   value: {}",
					value.getClass().getName(), c.getName(), value);
			return null;
		}

		return (T) converter.convert(value);
	}

	@Override
	public <T> T tryConvert(Class<T> c, Object value) {
		return convertInternal(c, value, false);
	}

	@Override
	public <T> Collection<T> convert(Class<T> c, Collection values) {
		if (values == null) {
			return Collections.emptyList();
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
	public String stringValue(Object value) {
		return convert(String.class, value);
	}

	@Override
	public Date dateValue(Object value) {
		return convert(Date.class, value);
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
		LOGGER.trace("Registering converter {} from type {} to type {}", converter.getClass(), source, destination);
		Map<Class, Converter> map = conversions.computeIfAbsent(source, k -> new HashMap<>());
		Converter oldConverter = map.get(destination);
		if (oldConverter != null) {
			LOGGER.trace("Overriding converter {} for types from {} to {} with {}", oldConverter, source, destination,
					converter);
		}
		map.put(destination, converter);
	}

	@Override
	public <F, I, T> Converter<F, T> addDynamicTwoStageConverter(Class<F> source, Class<I> intermediate,
			Class<T> destination) {
		Converter<F, T> converter = new TypeConverterImpl.DynamicTwoStageConverter<>(source, intermediate, destination);
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
				// this probably is not good idea but in some cases we cannot define converter to
				// concrete class and we define using interface
				converter = tryToFindDestConverterIn(dest, map);
			}

			if (converter == null) {
				converter = getNumberConverter(dest, map);
			}
		} while (converter == null && (clazz = clazz.getSuperclass()) != null);

		return converter;
	}

	@SuppressWarnings("rawtypes")
	private <T> Converter<?, ?> getNumberConverter(Class<T> dest, Map<Class, Converter> map) {
		Map<Class, Converter> mapping = map;
		// attempt to establish converter from source to dest via Number
		Converter<?, ?> first = mapping.get(Number.class);
		Converter<?, ?> second = null;
		if (first != null) {
			mapping = conversions.get(Number.class);
			if (mapping != null) {
				second = mapping.get(dest);
			}
		}
		if (second != null) {
			return new TwoStageConverter<>(first, second);
		}
		return null;
	}

	/**
	 * Try to find converter by super classes or interfaces in the given destination file. The converter is searched in
	 * the given map of possible converters.
	 *
	 * @param source
	 *            the source class to check
	 * @param map
	 *            the map of converter to check in
	 * @return the found converter or null
	 */
	@SuppressWarnings("rawtypes")
	private static Converter tryToFindDestConverterIn(Class<?> source, Map<Class, Converter> map) {
		if (source == null || source.isInterface() || map == null) {
			return null;
		}
		Converter<?, ?> converter = null;
		Class<?> checkFor = source;
		do {
			// first we check the super class for possible converter
			converter = map.get(checkFor);
			if (converter != null) {
				return converter;
			}
			converter = searchInInterfaces(map, checkFor);
		} while (converter == null && (checkFor = checkFor.getSuperclass()) != null);

		return converter;
	}

	@SuppressWarnings("rawtypes")
	private static Converter<?, ?> searchInInterfaces(Map<Class, Converter> map, Class<?> checkFor) {
		Converter<?, ?> converter = null;
		Class<?>[] interfaces = checkFor.getInterfaces();
		if (interfaces.length == 0) {
			return null;
		}
		// then try all interfaces except for serializable
		for (int i = 0; i < interfaces.length; i++) {
			Class<?> class1 = interfaces[i];
			// we are not interested in generic interfaces like serializable
			// other interfaces that need to be removed?
			if (!class1.equals(Serializable.class)) {
				converter = map.get(class1);
			}
			if (converter != null) {
				break;
			}
		}
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
			if (iConverter == null || tConverter == null) {
				throw new TypeConversionException("Cannot convert from " + from.getName() + " to " + to.getName());
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
