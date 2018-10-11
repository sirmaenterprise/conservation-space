package com.sirma.itt.emf.cls.validator.exception.mappers;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;

import com.sirma.itt.emf.cls.validator.exception.CodeValidatorException;

/**
 * Test class for the {@link CodeValidatorExceptionMapper}
 * 
 * @author svetlozar.iliev
 */
public class CodeValidatorExceptionMapperTest {

	private final CodeValidatorExceptionMapper validatorMapper = new CodeValidatorExceptionMapper();

	/**
	 * Tests that a correct response is constructed
	 */
	@Test
	public void should_construct_valid_response() {
		CodeValidatorException exception = new CodeValidatorException("Error", Arrays.asList("1", "2"));
		Response response = validatorMapper.toResponse(exception);
		assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		assertEquals("{\"message\":[\"1\",\"2\"]}", response.getEntity().toString());
	}
}
