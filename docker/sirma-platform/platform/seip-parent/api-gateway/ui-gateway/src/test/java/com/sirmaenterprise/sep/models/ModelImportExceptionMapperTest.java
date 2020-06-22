package com.sirmaenterprise.sep.models;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import javax.ws.rs.core.Response;

import org.junit.Test;

public class ModelImportExceptionMapperTest {

	private ModelImportExceptionMapper mapper = new ModelImportExceptionMapper();

	@Test
	public void should_MapExceptionToJson() {
		Response response = mapper.toResponse(new ModelImportException(Arrays.asList("1", "2")));

		assertEquals(400, response.getStatus());
		assertEquals("{\"messages\":[\"1\",\"2\"]}", response.getEntity().toString());
	}

}
