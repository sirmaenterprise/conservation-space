package com.sirma.itt.cmf.dozer;

import org.dozer.CustomConverter;
import org.dozer.MappingException;

import com.sirma.itt.cmf.beans.model.TaskState;

/**
 * The Class TaskState converter for dozer mappings.
 *
 * @author BBonev
 */
public class TaskStateConverter implements CustomConverter {

	@Override
	public Object convert(Object destination, Object source, Class<?> destClass, Class<?> sourceClass) {
		if (source == null) {
			if (destClass.equals(TaskState.class)) {
				return null;
			}
			return null;
		}
		if (source instanceof String) {
			String s = (String) source;
			return TaskState.valueOf(s);
		} else if (source instanceof TaskState) {
			return source.toString().toLowerCase();
		} else {
			throw new MappingException("Converter TaskStateConverter used incorrectly. Arguments passed in were:"
					+ destination + " and " + source);
		}
	}

}
