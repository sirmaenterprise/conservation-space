/**
 * Copyright (c) 2014 13.03.2014 , Sirma ITT. /* /**
 */
package com.sirma.itt.emf.rest;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.rest.EmfApplicationException;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Tests {@link EmfApplicationExceptionHandler}.
 *
 * @author Adrian Mitev
 */
@Test
public class EmfApplicationExceptionHandlerTest {

	private EmfApplicationExceptionHandler handler = new EmfApplicationExceptionHandler();

	/**
	 * Tests {@link EmfApplicationExceptionHandler#toResponse(EmfApplicationException)} with exception message only.
	 */
	public void testToResponseWithoutMessages() {
		EmfApplicationException exception = new EmfApplicationException("Error has occured!");

		String result = (String) handler.toResponse(exception).getEntity();
		JsonAssert.assertJsonEquals("{\"message\" : \"Error has occured!\"}", result.toString());
	}

	/**
	 * Tests {@link EmfApplicationExceptionHandler#toResponse(EmfApplicationException)} with additional exception
	 * messages map.
	 */
	public void testToResponseWithMessages() {
		Map<String, String> messages = new HashMap<>();
		messages.put("message1", "Message1");
		messages.put("message2", "Message2");

		EmfApplicationException exception = new EmfApplicationException("Error has occured!", messages);

		handler.toResponse(exception).getEntity();
	}

}
