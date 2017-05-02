package com.sirma.itt.seip.instance.dozer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.dozer.CustomConverter;

import com.sirma.itt.seip.domain.util.PropertiesUtil;

/**
 * Custom converter that uses the internal {@link com.sirma.itt.seip.convert.TypeConverter} facility to convert data
 * types in Dozer.
 *
 * @author BBonev
 */
public class SerializableCloneConverterForDozer implements CustomConverter {

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class<?> destinationClass,
			Class<?> sourceClass) {
		if (sourceFieldValue instanceof Map && destinationClass.isAssignableFrom(Map.class)) {
			return PropertiesUtil.cloneProperties((Map<String, Serializable>) sourceFieldValue);
		}
		return new HashMap<>();
	}

}
