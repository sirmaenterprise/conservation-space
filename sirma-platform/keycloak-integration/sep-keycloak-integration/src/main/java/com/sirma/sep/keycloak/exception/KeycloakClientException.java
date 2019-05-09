package com.sirma.sep.keycloak.exception;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Represents general keycloak client exception, thrown when there is error in Keycloak after executing some operation.
 *
 * @author smustafov
 */
public class KeycloakClientException extends EmfRuntimeException {

	public KeycloakClientException(String message) {
		super(message);
	}

	public KeycloakClientException(Throwable cause) {
		super(cause);
	}

	public KeycloakClientException(String message, Throwable cause) {
		super(message, cause);
	}

}
