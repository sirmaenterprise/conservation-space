package com.sirma.itt.seip.configuration.convert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Chaining converter that can wrap several converters that produce the same type. When calling the convert method the
 * implementation will call the converters in the order they were added. If the result from a converter is valid (non
 * null and is of the expected type) then the value is returned and the chain stopped. If the value is not valid or the
 * converter throws an exception next converter will be tried to convert the value. If all converters fail then
 * <code>null</code> will be returned
 *
 * @author BBonev
 * @param <T>
 *            the generic type
 */
public class ChainingConverter<T> implements ConfigurationValueConverter<T> {

	private final Class<T> type;
	private final List<ConfigurationValueConverter<T>> unNamedConverters = new ArrayList<>();
	private final Map<String, ConfigurationValueConverter<T>> namedConverters = new HashMap<>(8);

	/**
	 * Instantiates a new chaining converter.
	 *
	 * @param type
	 *            the type that this converter will be responsible and all converters should result to this type
	 * @param initial
	 *            the initial
	 */
	public ChainingConverter(Class<T> type, ConfigurationValueConverter<T> initial) {
		this.type = type;
		addConverter(initial);
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public T convert(TypeConverterContext converterContext) {
		// first check for any specific converter name
		T value = callConverterByName(converterContext, converterContext.getConfiguration().getConverter());
		if (value != null) {
			return value;
		}

		// check for converter with the same name as the configuration
		value = callConverterByName(converterContext, converterContext.getConfiguration().getName());
		if (value != null) {
			return value;
		}

		for (int i = 0; i < unNamedConverters.size(); i++) {
			ConfigurationValueConverter<T> converter = unNamedConverters.get(i);
			value = callConverter(converter, converterContext);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	private T callConverterByName(TypeConverterContext converterContext, String converterName) {
		if (StringUtils.isBlank(converterName)) {
			return null;
		}
		ConfigurationValueConverter<T> namedConverter = namedConverters.get(converterName);
		if (namedConverter != null) {
			return callConverter(namedConverter, converterContext);
		}
		return null;
	}

	private T callConverter(ConfigurationValueConverter<T> converter, TypeConverterContext converterContext) {
		try {
			T result = converter.convert(converterContext);
			if (getType().isInstance(result)) {
				return result;
			}
		} catch (ConverterException e) {
			throw e;
		} catch (Exception e) {
			throw new ConverterException("Failed to execute converter " + converter, e);
		}
		return null;
	}

	/**
	 * Adds the converter to the chain
	 *
	 * @param converter
	 *            the converter
	 */
	public void addConverter(ConfigurationValueConverter<T> converter) {
		if (converter != null) {
			validateType(converter);
			String name = converter.getName();
			if (name == null) {
				unNamedConverters.add(converter);
			} else {
				namedConverters.put(name, converter);
			}
		}
	}

	private void validateType(ConfigurationValueConverter<T> converter) {
		if (!getType().equals(converter.getType())) {
			throw new IllegalArgumentException(
					"Cannot chain converters that produce different types! Current converter type is "
							+ getType().getName() + " added converter type is " + converter.getType().getName());
		}
	}

}
