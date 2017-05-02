package com.sirma.itt.emf.cls.web.validation;

import java.util.Map;

/**
 * Validator interface.
 *
 * @param <T>
 *            the type of the validated entity
 */
public interface Validator<T> {

	/**
	 * Validate an entity against set of rules. Returns a map with field names and error messages.
	 *
	 * @param code
	 *            the code to be validated
	 * @param update
	 *            true if entity is to be updated
	 * @return {@link Map} with field names and error messages if any of the validations fail. Empty {@link Map} if
	 *         validation is successful.
	 */
	Map<String, String> validate(T code, boolean update);
}
