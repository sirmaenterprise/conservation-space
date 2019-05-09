package com.sirma.sep.model.management.exception.mappers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.ws.rs.core.Response;

import org.junit.Test;

import com.sirma.sep.model.management.exception.ChangeSetCollisionException;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Test for {@link ChangeSetCollisionExceptionMapper}
 *
 * @author Radoslav Dimitrov
 */
public class ChangeSetCollisionExceptionMapperTest {

	@Test
	public void should_Handle_Exception_With_Single_Message() {
		Response response = new ChangeSetCollisionExceptionMapper().toResponse(new ChangeSetCollisionException(
				"Detected value collision for node /class=test node [ expected value for key 'en' 'Drawing 2' got 'Drawing 1']"));
		assertNotNull(response);
		assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
		JsonAssert.assertJsonEquals(
				"{\"code\":0,\"message\":\"Detected value collision for node /class=test node [ expected value for key 'en' 'Drawing 2' got 'Drawing 1']\"}",
				response.getEntity());
	}

	@Test
	public void should_Handle_Exception_With_Message_And_Errors() {
		Response response = new ChangeSetCollisionExceptionMapper().toResponse(new ChangeSetCollisionException("test node",
				"[ expected value for key 'en' 'Drawing 2' got 'Drawing 1'];[ expected value for key 'en' 'Drawing 3' got 'Drawing 4']"));
		assertNotNull(response);
		assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
		JsonAssert.assertJsonEquals(
				"{\"code\":0,\"message\":\"Detected value collision for node test node\",\"errors\":{\"[ expected value for key 'en' 'Drawing 2' got 'Drawing 1']\":{\"type\":\"ChangeSetCollisionException\",\"message\":\"[ expected value for key 'en' 'Drawing 2' got 'Drawing 1']\",\"error\":\"Detected value collision \"},\"[ expected value for key 'en' 'Drawing 3' got 'Drawing 4']\":{\"type\":\"ChangeSetCollisionException\",\"message\":\"[ expected value for key 'en' 'Drawing 3' got 'Drawing 4']\",\"error\":\"Detected value collision \"}}}'.",
				response.getEntity());
	}
}

