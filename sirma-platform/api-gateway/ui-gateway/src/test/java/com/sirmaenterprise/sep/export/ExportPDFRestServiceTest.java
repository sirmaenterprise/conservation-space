package com.sirmaenterprise.sep.export;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

public class ExportPDFRestServiceTest {

	@Spy
	@InjectMocks
	private ExportPDFRestService rest;

	@Mock
	private ConfigurationProperty<String> exportServerURL;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testExportPDFNoConfig() {
		when(exportServerURL.isNotSet()).thenReturn(true);
		Response response = rest.exportPDF(null, "test content");
		assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatus());
	}

	@Test
	public void testExportPDF() throws HttpException, IOException {
		when(exportServerURL.isNotSet()).thenReturn(false);
		when(exportServerURL.get()).thenReturn("http://localhost:8123");

		HttpClient client = mock(HttpClient.class);
		when(client.executeMethod(Matchers.any(HttpMethod.class))).thenReturn(HttpStatus.SC_OK);
		when(rest.createHttpClient()).thenReturn(client);

		RequestInfo info = mock(RequestInfo.class);
		HttpHeaders httpHeaders = mock(HttpHeaders.class);
		when(httpHeaders.getRequestHeaders()).thenReturn(new MultivaluedHashMap<>(0));
		when(info.getHeaders()).thenReturn(httpHeaders);

		Response response = rest.exportPDF(info, "test content");
		assertEquals(HttpStatus.SC_OK, response.getStatus());
	}
}
