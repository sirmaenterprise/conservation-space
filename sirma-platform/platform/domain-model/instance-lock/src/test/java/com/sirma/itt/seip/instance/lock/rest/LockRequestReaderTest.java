package com.sirma.itt.seip.instance.lock.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.json.JsonException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.lock.action.LockRequest;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Test for {@link LockRequestReader}.
 *
 * @author A. Kunchev
 */
public class LockRequestReaderTest {

	@InjectMocks
	private LockRequestReader reader;

	@Mock
	private RequestInfo requestInfo;

	@Mock
	private MultivaluedMap<String, String> pathParams;

	@Mock
	private UriInfo uriInfo;

	@Before
	public void setup() {
		reader = new LockRequestReader();
		MockitoAnnotations.initMocks(this);
		when(pathParams.get("id")).thenReturn(Arrays.asList("instanceId"));
		when(uriInfo.getPathParameters()).thenReturn(pathParams);
		when(requestInfo.getUriInfo()).thenReturn(uriInfo);
	}

	@Test
	public void isReadable_wrongType() {
		assertFalse(reader.isReadable(String.class, null, null, null));
	}

	@Test
	public void isReadable_correctType() {
		assertTrue(reader.isReadable(LockRequest.class, null, null, null));
	}

	@Test(expected = NullPointerException.class)
	public void readFrom_nullStream() throws IOException {
		reader.readFrom(LockRequest.class, null, null, null, null, null);
	}

	@Test(expected = JsonException.class)
	public void readFrom_IOExceptionWhileReading() throws IOException {
		try (InputStream stream = Mockito.mock(InputStream.class)) {
			Mockito.when(stream.read(any(byte[].class), anyInt(), anyInt())).thenThrow(new IOException());
			reader.readFrom(LockRequest.class, null, null, null, null, stream);
		}
	}

	@Test(expected = BadRequestException.class)
	public void readFrom_emptyJson() throws IOException {
		try (InputStream stream = new ByteArrayInputStream("{}".getBytes())) {
			reader.readFrom(LockRequest.class, null, null, null, null, stream);
		}
	}

	@Test
	public void readFrom_correctLockRequest() throws IOException {
		try (InputStream stream = new ByteArrayInputStream("{\"type\":\"for edit\"}".getBytes())) {
			LockRequest request = reader.readFrom(LockRequest.class, null, null, null, null, stream);
			assertEquals(LockRequest.LOCK, request.getOperation());
			assertEquals(LockRequest.LOCK, request.getUserOperation());
			assertEquals("for edit", request.getLockType());
			assertEquals("instanceId", request.getTargetId());
		}
	}

}
