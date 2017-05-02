package com.sirma.itt.seip.export.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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

/**
 * Test for {@link ExportWordBodyReader}.
 *
 * @author Stella D
 */
public class ExportWordBodyReaderTest {

	@InjectMocks
	private ExportWordBodyReader reader;

	@Mock
	private RequestInfo request;

	/**
	 * Runs before each method and setup mockito.
	 */
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		setupPathParamId();
		setupRequestCookies();
	}

	/**
	 * Setup path param id.
	 */
	private void setupPathParamId() {
		MultivaluedMap<String, String> paramsMap = mock(MultivaluedMap.class);
		when(paramsMap.get("id")).thenReturn(Arrays.asList("instanceId"));
		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getPathParameters()).thenReturn(paramsMap);
		when(request.getUriInfo()).thenReturn(uriInfo);
	}

	/**
	 * Setup request cookies.
	 */
	private void setupRequestCookies() {
		Map<String, Cookie> cookies = new HashMap<>();
		Cookie cookie = new Cookie("cookie-name", "cookie-value");
		cookies.put("cookie-1", cookie);
		HttpHeaders httpHeaders = mock(HttpHeaders.class);
		when(httpHeaders.getCookies()).thenReturn(cookies);
		when(request.getHeaders()).thenReturn(httpHeaders);
	}

	/**
	 * Readable incorrect test.
	 */
	@Test
	public void isReadable_incorrectClass() {
		assertFalse(reader.isReadable(String.class, null, null, null));
	}

	/**
	 * Readable correct test.
	 */
	@Test
	public void isReadable_correctClass() {
		assertTrue(reader.isReadable(ExportWordRequest.class, null, null, null));
	}

	/**
	 * Incorrect object test.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test(expected = JsonException.class)
	public void readFrom_notAObject() throws IOException {
		try (InputStream stream = new ByteArrayInputStream("[]".getBytes())) {
			reader.readFrom(ExportWordRequest.class, null, null, null, null, stream);
		}
	}

	/**
	 * Empty object test.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test(expected = BadRequestException.class)
	public void readFrom_emptyJson() throws IOException {
		try (InputStream stream = new ByteArrayInputStream("{}".getBytes())) {
			reader.readFrom(ExportWordRequest.class, null, null, null, null, stream);
		}
	}

	/**
	 * Successful read test.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void readFrom_successful() throws IOException {
		try (InputStream stream = getClass().getClassLoader().getResourceAsStream("export-word-request-test.json")) {
			ExportWordRequest exportRequest = reader.readFrom(ExportWordRequest.class, null, null, null, null, stream);
			assertEquals("exportWord", exportRequest.getUserOperation());
			assertEquals("page-to-export-word-url", exportRequest.getUrl());
			assertNotNull(exportRequest.getCookies());
		}
	}

	/**
	 * Successful read test for tab id.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void readFrom_tabId_successful() throws IOException {
		try (InputStream stream = getClass().getClassLoader().getResourceAsStream("export-tab-word-request.json")) {
			ExportWordRequest exportRequest = reader.readFrom(ExportWordRequest.class, null, null, null, null, stream);
			assertEquals("exportWord", exportRequest.getUserOperation());
			assertEquals("page-to-export-word-url&tab=someTabId", exportRequest.getUrl());
			assertEquals("someTabId", exportRequest.getTabId());
			assertNotNull(exportRequest.getCookies());
		}
	}

}
