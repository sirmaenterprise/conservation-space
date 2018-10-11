package com.sirma.itt.seip.instance.actions.compare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.ws.rs.WebApplicationException;
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
 * Test for {@link VersionCompareRequestReader}.
 *
 * @author A. Kunchev
 */
public class VersionCompareRequestReaderTest {

	@InjectMocks
	private VersionCompareRequestReader reader;

	@Mock
	private RequestInfo request;

	@Mock
	private MultivaluedMap<String, String> paramsMap;

	@Mock
	private UriInfo uriInfo;

	@Before
	public void setup() {
		reader = new VersionCompareRequestReader();
		MockitoAnnotations.initMocks(this);

		when(paramsMap.get("id")).thenReturn(Arrays.asList("instance-id"));
		when(uriInfo.getPathParameters()).thenReturn(paramsMap);
		when(request.getUriInfo()).thenReturn(uriInfo);
	}

	@Test
	public void isReadable_wrongType_false() {
		assertFalse(reader.isReadable(String.class, null, null, null));
	}

	@Test
	public void isReadable_true() {
		assertTrue(reader.isReadable(VersionCompareRequest.class, null, null, null));
	}

	@Test(expected = BadRequestException.class)
	public void testReadFromWithAnEmptyStream() throws WebApplicationException, IOException {
		try (BufferedInputStream stream = new BufferedInputStream(new ByteArrayInputStream("{}".getBytes()))) {
			reader.readFrom(null, null, null, null, null, stream);
		}
	}

	@Test(expected = BadRequestException.class)
	public void readFrom_withoutAuthenticationHeaders() throws IOException {
		try (InputStream stream = VersionCompareRequestReaderTest.class
				.getClassLoader()
					.getResourceAsStream("compare-versions-request-payload-test.json")) {
			HttpHeaders headers = mock(HttpHeaders.class);
			when(headers.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn(null);
			when(headers.getHeaderString(HttpHeaders.COOKIE)).thenReturn("");
			when(request.getHeaders()).thenReturn(headers);

			reader.readFrom(null, null, null, null, null, stream);
		}
	}

	@Test
	public void readFrom_correctRequestBuild() throws IOException {
		try (InputStream stream = VersionCompareRequestReaderTest.class
				.getClassLoader()
					.getResourceAsStream("compare-versions-request-payload-test.json")) {
			HttpHeaders headers = mock(HttpHeaders.class);
			when(headers.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("authorization-header");
			when(headers.getHeaderString(HttpHeaders.COOKIE)).thenReturn("cookie-header");
			when(request.getHeaders()).thenReturn(headers);

			VersionCompareRequest actionRequest = reader.readFrom(null, null, null, null, null, stream);
			assertNotNull(actionRequest);
			assertEquals("compareVersions", actionRequest.getUserOperation());
			assertEquals("instance-id", actionRequest.getTargetId());
			assertEquals("instance-id-v1.6", actionRequest.getFirstSourceId());
			assertEquals("instance-id-v1.8", actionRequest.getSecondSourceId());
		}
	}

}
