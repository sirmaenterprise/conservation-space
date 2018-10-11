package com.sirmaenterprise.sep.roles.jaxb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dozer.CustomConverter;

/**
 * {@link FilterTypeToString} could convert the complex type {@link FilterType} to {@link String}
 */
public class FilterTypeToString implements CustomConverter {

	@SuppressWarnings("unchecked")
	@Override
	public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class<?> destinationClass,
			Class<?> sourceClass) {
		if (sourceFieldValue instanceof Collection && Collection.class.isAssignableFrom(destinationClass)) {
			List<String> result = new ArrayList<>(((Collection<?>) sourceFieldValue).size());
			for (FilterType filter : ((Collection<FilterType>) sourceFieldValue)) {
				result.add(filter.getValue());
			}
			return result;
		} else if (sourceClass == FilterType.class && destinationClass == String.class) {
			return ((FilterType) sourceFieldValue).getValue();
		}
		return null;
	}

}
