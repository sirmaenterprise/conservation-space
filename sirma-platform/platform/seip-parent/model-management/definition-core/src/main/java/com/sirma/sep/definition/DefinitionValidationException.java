package com.sirma.sep.definition;

import java.util.Collection;

/**
 * Thrown when defnition validation fails. Contains list of validation errors.
 *
 * @author Adrian Mitev
 */
public class DefinitionValidationException extends RuntimeException {

	private final transient Collection<String> errors;

	public DefinitionValidationException(Collection<String> errors) {
		this.errors = errors;
	}

	public Collection<String> getErrors() {
		return errors;
	}

}
