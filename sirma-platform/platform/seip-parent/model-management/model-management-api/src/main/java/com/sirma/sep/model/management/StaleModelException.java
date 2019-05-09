package com.sirma.sep.model.management;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Exception throw to indicate incompatible model changes pass
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 31/07/2018
 */
public class StaleModelException extends EmfRuntimeException {
	public StaleModelException(String message) {super(message);}
}
