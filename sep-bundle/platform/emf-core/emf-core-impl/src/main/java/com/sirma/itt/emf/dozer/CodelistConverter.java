package com.sirma.itt.emf.dozer;

import org.dozer.CustomConverter;
import org.dozer.MappingException;

/**
 * Converter class used to convert between String and codelist int values
 *
 * @author BBonev
 */
public class CodelistConverter implements CustomConverter {

	private static final String AND = " and ";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object convert(Object destination, Object source, Class<?> destClass,
			Class<?> sourceClass) {
		if (source == null) {
			return null;
		}
		if (source instanceof String) {
			String s = (String) source;
			if (s.toUpperCase().startsWith("CL")) {
				s = s.substring(2);
			}
			Integer result = null;
			try {
				result = Integer.parseInt(s);
			} catch (NumberFormatException e) {
				throw new MappingException(
						"Invalid codelist value. Arguments passed in were:"
								+ destination + AND + source);
			}
			return result;
		} else if (source instanceof Integer) {
			return "CL" + source;
		} else {
			throw new MappingException(
					"Converter CodelistConverter used incorrectly. Arguments passed in were:"
							+ destination + AND + source);
		}
	}

}
