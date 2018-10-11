package com.sirma.itt.emf.cls.validator.exception.mappers;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;

import com.sirma.itt.emf.cls.validator.exception.SheetValidatorException;

/**
 * Test class for the {@link SheetValidatorExceptionMapper}
 * 
 * @author svetlozar.iliev
 */
public class SheetValidatorExceptionMapperTest {

	private final SheetValidatorExceptionMapper validatorMapper = new SheetValidatorExceptionMapper();

	/**
	 * Tests that a correct response is constructed
	 */
	@Test
	public void should_construct_valid_response() {
		SheetValidatorException exception = new SheetValidatorException("Error");
		Response response = validatorMapper.toResponse(exception);
		assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		assertEquals("{\"message\":\"Error\"}", response.getEntity().toString());
	}
}
