package com.sirma.itt.seip.instance.actions.download;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Test for {@link DownloadActionBodyReader}.
 *
 * @author A. Kunchev
 */
public class DownloadActionBodyReaderTest {

	@InjectMocks
	private DownloadActionBodyReader reader;

	@Mock
	private MultivaluedMap<String, String> paramsMap;

	@Mock
	private UriInfo uriInfo;

	@Mock
	private RequestInfo request;

	@Before
	public void setup() {
		reader = new DownloadActionBodyReader();
		MockitoAnnotations.initMocks(this);
		when(paramsMap.get("id")).thenReturn(Arrays.asList("targetId"));
		when(uriInfo.getPathParameters()).thenReturn(paramsMap);
		when(uriInfo.getQueryParameters()).thenReturn(paramsMap);
		when(request.getUriInfo()).thenReturn(uriInfo);
	}

	@Test
	public void isReadable_wrongClass_false() {
		assertFalse(reader.isReadable(String.class, null, null, null));
	}

	@Test
	public void isReadable_true() {
		assertTrue(reader.isReadable(DownloadRequest.class, null, null, null));
	}

	@Test
	public void readFrom_noPurpose() throws IOException {
		DownloadRequest result = reader.readFrom(null, null, null, null, null, null);
		assertEquals("targetId", result.getTargetId());
		assertNull(result.getPurpose());
	}

	@Test
	public void readFrom_emptyPurpose() throws IOException {
		DownloadRequest result = reader.readFrom(null, null, null, null, null, null);
		assertEquals("targetId", result.getTargetId());
		assertNull(result.getPurpose());
	}

	@Test
	public void readFrom_withPurpose() throws IOException {
		when(paramsMap.get("purpose")).thenReturn(Arrays.asList("purpose"));
		DownloadRequest result = reader.readFrom(null, null, null, null, null, null);
		assertEquals("targetId", result.getTargetId());
		assertEquals("purpose", result.getPurpose());
		assertEquals("download", result.getOperation());
		assertEquals("download", result.getUserOperation());
	}

}
