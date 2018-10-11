package com.sirma.itt.seip.resources.dozer;

import org.dozer.CustomConverter;
import org.dozer.MappingException;

import com.sirma.itt.seip.resources.ResourceType;

/**
 * Dozer converter to support the {@link ResourceType} conversions
 *
 * @author BBonev
 */
public class ResourceTypeConverter implements CustomConverter {

	@Override
	public Object convert(Object destination, Object source, Class<?> destClass, Class<?> sourceClass) {
		if (source == null) {
			return null;
		}
		if (source instanceof Integer) {
			return ResourceType.getById((Integer) source);
		} else if (source instanceof String) {
			return ResourceType.getByType((String) source);
		} else if (source instanceof ResourceType) {
			if (destClass.equals(Integer.class)) {
				return ((ResourceType) source).getType();
			}
			return ((ResourceType) source).getName();
		} else {
			throw new MappingException("Converter ResourceTypeConverter used incorrectly. Arguments passed in were:"
					+ destination + " and " + source);
		}
	}

}
