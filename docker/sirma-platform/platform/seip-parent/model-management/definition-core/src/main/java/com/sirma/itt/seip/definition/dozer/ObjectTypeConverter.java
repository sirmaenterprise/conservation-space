package com.sirma.itt.seip.definition.dozer;

import org.dozer.CustomConverter;
import org.dozer.MappingException;

import com.sirma.itt.seip.definition.jaxb.ObjectType;

/**
 * The Class {@link ObjectType} converter for dozer mappings.
 *
 * @author BBonev
 */
public class ObjectTypeConverter implements CustomConverter {

	@Override
	public Object convert(Object destination, Object source, Class<?> destClass, Class<?> sourceClass) {
		if (source == null) {
			return null;
		}
		if (source instanceof String) {
			String s = (String) source;
			return ObjectType.fromValue(s);
		} else if (source instanceof ObjectType) {
			return ((ObjectType) source).value();
		} else {
			throw new MappingException("Converter PermissionTypesConverter used incorrectly. Arguments passed in were:"
					+ destination + " and " + source);
		}
	}

}
