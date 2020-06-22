package com.sirma.itt.seip.definition.dozer;

import org.dozer.CustomConverter;
import org.dozer.MappingException;

import com.sirma.itt.seip.definition.jaxb.CheckTypes;
import com.sirma.itt.seip.domain.definition.FilterMode;

/**
 * The Class FilterMode &lt; - &gt; CheckTypes converter for dozer mappings.
 *
 * @author BBonev
 */
public class FilterModeConverter implements CustomConverter {

	@Override
	public Object convert(Object destination, Object source, Class<?> destClass, Class<?> sourceClass) {
		if (source == null) {
			// generate correct output value that is XSD valid value
			if (destClass.equals(CheckTypes.class)) {
				return CheckTypes.IN;
			}
			return null;
		}
		if (source instanceof CheckTypes) {
			return FilterMode.valueOf(source.toString());
		} else if (source instanceof FilterMode) {
			return CheckTypes.valueOf(source.toString());
		} else {
			throw new MappingException("Converter FilterModeConverter used incorrectly. Arguments passed in were:"
					+ destination + " and " + source);
		}
	}

}
