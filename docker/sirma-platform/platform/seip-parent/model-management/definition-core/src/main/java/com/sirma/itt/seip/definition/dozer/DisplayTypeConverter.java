package com.sirma.itt.seip.definition.dozer;

import org.dozer.CustomConverter;
import org.dozer.MappingException;

import com.sirma.itt.seip.domain.definition.DisplayType;

/**
 * The Class DisplayType converter for dozer mappings.
 *
 * @author BBonev
 */
public class DisplayTypeConverter implements CustomConverter {

	@Override
	public Object convert(Object destination, Object source, Class<?> destClass, Class<?> sourceClass) {
		if (source == null) {
			if (destClass.equals(DisplayType.class)) {
				return DisplayType.parse(null);
			}
			return null;
		}
		if (source instanceof String) {
			String s = (String) source;
			return DisplayType.parse(s);
		} else if (source instanceof DisplayType) {
			return source.toString().toLowerCase();
		} else {
			throw new MappingException("Converter DisplayTypeConverter used incorrectly. Arguments passed in were:"
					+ destination + " and " + source);
		}
	}

}
