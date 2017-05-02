package com.sirma.itt.seip.template.exceptions.mappers;

import java.lang.invoke.MethodHandles;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.exceptions.DuplicateIdentifierException;

/**
 * Exception mapper for {@link DuplicateIdentifierException}. Thrown when trying to create a template with name that
 * already exists.
 *
 * @author smustafov
 */
@Provider
public class DuplicateIdentifierExceptionHandler implements ExceptionMapper<DuplicateIdentifierException> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public Response toResponse(DuplicateIdentifierException exception) {
		LOGGER.error(exception.getMessage(), exception);

		return Response.status(Status.CONFLICT).build();
	}

}
