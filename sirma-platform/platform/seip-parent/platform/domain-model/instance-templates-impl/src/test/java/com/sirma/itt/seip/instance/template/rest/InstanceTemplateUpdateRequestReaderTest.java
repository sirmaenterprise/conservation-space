package com.sirma.itt.seip.instance.template.rest;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;

public class InstanceTemplateUpdateRequestReaderTest {

	private InstanceTemplateUpdateRequestReader reader = new InstanceTemplateUpdateRequestReader();

	@Test
	public void should_ParseRequest() throws WebApplicationException, IOException {
		InstanceTemplateUpdateRequest request = reader.readFrom(null, null, null, null, null, getRequestData());

		assertEquals("template1", request.getTemplateInstance());
	}

	private InputStream getRequestData() {
		return new ByteArrayInputStream("{ \"templateInstance\" : \"template1\" }".getBytes());
	}

}
