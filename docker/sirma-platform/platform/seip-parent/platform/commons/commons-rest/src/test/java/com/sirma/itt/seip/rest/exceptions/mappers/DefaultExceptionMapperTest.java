package com.sirma.itt.seip.rest.exceptions.mappers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;

import com.sirma.itt.seip.exception.EmfRuntimeException;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Test for {@link DefaultExceptionMapper}
 *
 * @author BBonev
 */
public class DefaultExceptionMapperTest {

	@Test
	public void handle_noMessages() throws Exception {
		Response response = new DefaultExceptionMapper().toResponse(new EmfRuntimeException());

		assertNotNull(response);
		assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
		assertNotNull(response.getEntity());
		JsonAssert.assertJsonEquals(
				"{\"message\":\"Unexpected exception occur in the system. Please contact your system administrator.\"}",
				response.getEntity());
	}

	@Test
	public void handle_withMessage() throws Exception {
		Response response = new DefaultExceptionMapper().toResponse(new EmfRuntimeException("exception message"));

		assertNotNull(response);
		assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
		assertNotNull(response.getEntity());
		JsonAssert.assertJsonEquals(
				"{\"message\":\"Unexpected exception occur in the system. Please contact your system administrator.\",\"exception\":\"exception message\"}",
				response.getEntity());
	}

	@Test
	public void handle_withOtherMessage() throws Exception {
		Response response = new DefaultExceptionMapper().toResponse(
				new EmfRuntimeException("exception message", new EmfRuntimeException("other exception message")));

		assertNotNull(response);
		assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
		assertNotNull(response.getEntity());
		JsonAssert.assertJsonEquals(
				"{\"message\":\"Unexpected exception occur in the system. Please contact your system administrator.\",\"exception\":\"exception message\",\"other\":[\"other exception message\"]}",
				response.getEntity());
	}

	@Test
	public void handle_withOtherMessage_fromSuppressed() throws Exception {
		EmfRuntimeException causedBy = new EmfRuntimeException("other exception message");
		causedBy.addSuppressed(new EmfRuntimeException("suppressed exception message"));
		Response response = new DefaultExceptionMapper()
				.toResponse(new EmfRuntimeException("exception message", causedBy));

		assertNotNull(response);
		assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
		assertNotNull(response.getEntity());
		JsonAssert.assertJsonEquals(
				"{\"message\":\"Unexpected exception occur in the system. Please contact your system administrator.\",\"exception\":\"exception message\",\"other\":[\"other exception message\",\"suppressed exception message\"]}",
				response.getEntity());
	}
}
