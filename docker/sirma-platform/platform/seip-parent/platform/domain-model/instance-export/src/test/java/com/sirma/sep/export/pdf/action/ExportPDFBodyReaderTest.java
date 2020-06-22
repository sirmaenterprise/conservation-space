package com.sirma.sep.export.pdf.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.sep.export.pdf.action.ExportPDFBodyReader;
import com.sirma.sep.export.pdf.action.ExportPDFRequest;

/**
 * Test for {@link ExportPDFBodyReader}.
 *
 * @author A. Kunchev
 */
public class ExportPDFBodyReaderTest {

	@InjectMocks
	private ExportPDFBodyReader reader;

	@Mock
	private RequestInfo request;

	@Before
	public void setup() {
		reader = new ExportPDFBodyReader();
		MockitoAnnotations.initMocks(this);

		setupPathParamId();
		setupRequestCookies();
	}

	private void setupPathParamId() {
		MultivaluedMap<String, String> paramsMap = mock(MultivaluedMap.class);
		when(paramsMap.get("id")).thenReturn(Arrays.asList("instanceId"));
		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getPathParameters()).thenReturn(paramsMap);
		when(request.getUriInfo()).thenReturn(uriInfo);
	}

	private void setupRequestCookies() {
		Map<String, Cookie> cookies = new HashMap<>();
		Cookie cookie = new Cookie("cookie-name", "cookie-value");
		cookies.put("cookie-1", cookie);
		HttpHeaders httpHeaders = mock(HttpHeaders.class);
		when(httpHeaders.getCookies()).thenReturn(cookies);
		when(request.getHeaders()).thenReturn(httpHeaders);
	}

	@Test
	public void isReadable_incorrectClass() {
		assertFalse(reader.isReadable(String.class, null, null, null));
	}

	@Test
	public void isReadable_correctClass() {
		assertTrue(reader.isReadable(ExportPDFRequest.class, null, null, null));
	}

	@Test(expected = JsonException.class)
	public void readFrom_notAObject() throws IOException {
		try (InputStream stream = new ByteArrayInputStream("[]".getBytes())) {
			reader.readFrom(ExportPDFRequest.class, null, null, null, null, stream);
		}
	}

	@Test(expected = BadRequestException.class)
	public void readFrom_emptyJson() throws IOException {
		try (InputStream stream = new ByteArrayInputStream("{}".getBytes())) {
			reader.readFrom(ExportPDFRequest.class, null, null, null, null, stream);
		}
	}

	@Test
	public void readFrom_successful() throws IOException {
		try (InputStream stream = ExportPDFBodyReaderTest.class.getResourceAsStream("/export-pdf-request-test.json")) {
			ExportPDFRequest exportRequest = reader.readFrom(ExportPDFRequest.class, null, null, null, null, stream);
			assertEquals("exportPDF", exportRequest.getUserOperation());
			assertEquals("page-to-export-url", exportRequest.getUrl());
		}
	}

}
