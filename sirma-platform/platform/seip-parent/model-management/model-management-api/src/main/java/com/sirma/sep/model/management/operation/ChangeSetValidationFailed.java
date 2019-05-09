package com.sirma.sep.model.management.operation;

/**
 * Exception thrown to indicate problem during change set validation phase. This means that the change set detected data
 * discrepancies and the user should review his/her changes before sending them back to the server.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 30/07/2018
 */
public class ChangeSetValidationFailed extends RuntimeException {

	public ChangeSetValidationFailed(String message) {
		super(message);
	}

	public ChangeSetValidationFailed(Throwable causedBy) {
		super(causedBy);
	}

	public ChangeSetValidationFailed(String message, Throwable causedBy) {
		super(message, causedBy);
	}
}
