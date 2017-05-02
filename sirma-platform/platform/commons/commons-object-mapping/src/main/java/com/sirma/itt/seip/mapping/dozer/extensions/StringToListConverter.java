package com.sirma.itt.seip.mapping.dozer.extensions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dozer.CustomConverter;
import org.dozer.MappingException;

/**
 * Converter class used to convert between a list of string values and single string.
 *
 * @author BBonev
 */
public class StringToListConverter implements CustomConverter {

	@Override
	public Object convert(Object destination, Object source, Class<?> destClass, Class<?> sourceClass) {
		if (source == null) {
			return null;
		}
		if (source instanceof String) {
			return convertFromString(destination, source);
		} else if (source instanceof List) {
			return convertFromList(source);
		} else {
			throw new MappingException("Converter StringToBooleanConverter used incorrectly. Arguments passed in were:"
					+ destination + " and " + source);
		}
	}

	/**
	 * Convert from list.
	 *
	 * @param source
	 *            the source
	 * @return the object
	 */
	private static Object convertFromList(Object source) {
		StringBuilder builder = new StringBuilder();
		for (Object element : (List<?>) source) {
			builder.append(element).append(",");
		}
		if (builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		return builder.toString();
	}

	/**
	 * Convert from string.
	 *
	 * @param destination
	 *            the destination
	 * @param source
	 *            the source
	 * @return the object
	 */
	@SuppressWarnings("unchecked")
	private static Object convertFromString(Object destination, Object source) {
		List<String> dest;
		String[] split = ((String) source).split("\\s*,\\s*|\\s*;\\s*");
		if (destination == null) {
			dest = new ArrayList<>(split.length);
		} else {
			dest = (List<String>) destination;
		}
		dest.addAll(Arrays.asList(split));
		return destination;
	}

}
