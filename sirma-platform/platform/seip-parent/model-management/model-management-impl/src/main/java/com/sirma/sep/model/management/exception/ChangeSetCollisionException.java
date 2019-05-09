package com.sirma.sep.model.management.exception;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.Response;

import com.sirma.itt.seip.rest.models.Error;
import com.sirma.itt.seip.rest.models.ErrorData;
import com.sirma.sep.model.management.operation.ChangeSetValidationFailed;

/**
 * Exception thrown to indicate collision in values during change set validation phase. This means that the change set detected value
 * collisions and the user should review changes before sending them back to the server.
 *
 * @author Radoslav Dimitrov
 */
public class ChangeSetCollisionException extends ChangeSetValidationFailed {

	private final ErrorData data;

	public ChangeSetCollisionException(String message) {
		super(message);
		data = new ErrorData(message);
	}

	public ChangeSetCollisionException(String nodeId, String detectedCollisions) {
		super("Detected value collision for node " + nodeId + detectedCollisions);
		data = new ErrorData("Detected value collision for node " + nodeId);
		Map<String, Error> errors = collisionsAsMap(detectedCollisions);
		data.setErrors(errors);
	}

	public ErrorData getData() {
		return data;
	}

	public Response.Status getStatus() {
		return Response.Status.CONFLICT;
	}

	private Map<String, Error> collisionsAsMap(String detectedCollisions) {
		return Stream.of(detectedCollisions.split(";"))
				.collect(Collectors.toMap(errorMessage -> errorMessage, errorMessage -> {
					Error error = new Error();
					error.setType("ChangeSetCollisionException");
					error.setError("Detected value collision ");
					error.setMessage(errorMessage);
					return error;
				}));
	}
}
