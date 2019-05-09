package com.sirma.itt.seip.rest.filters;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContentLengthReponseFilterTest {

	@Mock
	private ContainerResponseContext responseContext;

	@Mock
	private MultivaluedMap<String, Object> headers;

	@Mock
	private File file;

	@Before
	public void init() {
		Mockito.when(responseContext.getHeaders()).thenReturn(headers);
		Mockito.when(file.length()).thenReturn(256L);
	}

	@Test
	public void testSetContentLength() throws IOException {
		Mockito.when(responseContext.getEntity()).thenReturn(file);

		new ContentLengthReponseFilter().filter(null, responseContext);
		Mockito.verify(headers).addFirst(HttpHeaders.CONTENT_LENGTH, 256L);
	}

	@Test
	public void testShouldNotSetContentLength() throws IOException {
		Mockito.when(responseContext.getEntity()).thenReturn("string body");

		new ContentLengthReponseFilter().filter(null, responseContext);
		Mockito.verify(headers, Mockito.never()).addFirst(Mockito.anyString(), Mockito.any());
	}
}
