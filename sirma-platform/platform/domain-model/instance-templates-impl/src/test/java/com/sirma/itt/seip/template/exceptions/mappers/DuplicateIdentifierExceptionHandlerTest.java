package com.sirma.itt.seip.template.exceptions.mappers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.domain.exceptions.DuplicateIdentifierException;

/**
 * Tests for {@link DuplicateIdentifierExceptionHandler}.
 *
 * @author smustafov
 */
public class DuplicateIdentifierExceptionHandlerTest {

	private DuplicateIdentifierExceptionHandler handler;

	@Before
	public void beforeMethod() {
		handler = new DuplicateIdentifierExceptionHandler();
	}

	@Test
	public void testToResponse() {
		Response response = handler.toResponse(new DuplicateIdentifierException("duplicate id"));

		assertNotNull(response);
		assertEquals(Status.CONFLICT.getStatusCode(), response.getStatus());
	}

}
