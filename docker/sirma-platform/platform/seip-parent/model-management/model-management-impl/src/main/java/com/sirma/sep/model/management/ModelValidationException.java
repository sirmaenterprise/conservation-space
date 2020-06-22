package com.sirma.sep.model.management;

import java.util.List;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Exception thrown to indicate invalid nodes during model update.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 07/08/2018
 */
public class ModelValidationException extends EmfRuntimeException {

	private final List<Path> invalidNodes;

	public ModelValidationException(String message, List<Path> invalidNodes) {
		super(message);
		this.invalidNodes = invalidNodes;
	}

	public List<Path> getInvalidNodes() {
		return invalidNodes;
	}
}
