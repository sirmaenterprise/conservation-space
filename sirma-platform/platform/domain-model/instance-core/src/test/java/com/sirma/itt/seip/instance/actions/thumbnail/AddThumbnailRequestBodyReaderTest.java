package com.sirma.itt.seip.instance.actions.thumbnail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.json.JsonException;
import javax.json.stream.JsonParsingException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.actions.transition.InstanceTransitionBodyReaderTest;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Tests for {@link AddThumbnailRequestBodyReader}.
 *
 * @author A. Kunchev
 */
public class AddThumbnailRequestBodyReaderTest {

	@InjectMocks
	private AddThumbnailRequestBodyReader reader;

	@Mock
	private MultivaluedMap<String, String> paramsMap;

	@Mock
	private UriInfo uriInfo;

	@Mock
	private RequestInfo request;

	@Before
	public void setup() {
		reader = new AddThumbnailRequestBodyReader();
		MockitoAnnotations.initMocks(this);
		Mockito.when(paramsMap.get("id")).thenReturn(Arrays.asList("targetId"));
		Mockito.when(uriInfo.getPathParameters()).thenReturn(paramsMap);
		Mockito.when(request.getUriInfo()).thenReturn(uriInfo);
	}

	@Test
	public void isReadable_notCorrectRequest() {
		boolean result = reader.isReadable(String.class, null, null, null);
		assertFalse(result);
	}

	@Test
	public void isReadable_correctRequest() {
		boolean result = reader.isReadable(AddThumbnailRequest.class, null, null, null);
		assertTrue(result);
	}

	@Test(expected = NullPointerException.class)
	public void readFrom_nullStream() throws IOException {
		reader.readFrom(null, null, null, null, null, null);
	}

	@Test(expected = JsonParsingException.class)
	public void readFrom_closedStream() throws IOException {
		try (InputStream stream = mock(InputStream.class)) {
			when(stream.read(any(byte[].class), anyInt(), anyInt())).thenThrow(new IOException());
			reader.readFrom(null, null, null, null, null, stream);
		}
	}

	@Test(expected = JsonException.class)
	public void readFrom_notJsonObject() throws IOException {
		try (InputStream stream = new ByteArrayInputStream("[]".getBytes())) {
			reader.readFrom(null, null, null, null, null, stream);
		}
	}

	@Test(expected = BadRequestException.class)
	public void readFrom_emptyJson() throws IOException {
		try (InputStream stream = new ByteArrayInputStream("{}".getBytes())) {
			reader.readFrom(null, null, null, null, null, stream);
		}
	}

	@Test
	public void readFrom_returnsCorrectRequest() throws IOException {
		try (InputStream stream = InstanceTransitionBodyReaderTest.class
				.getResourceAsStream("/add-thumbnail-action-reader-test.json")) {
			AddThumbnailRequest thumbnailRequest = reader.readFrom(null, null, null, null, null, stream);
			assertEquals("targetId", thumbnailRequest.getTargetId());
			assertEquals("thumbnailId", thumbnailRequest.getThumbnailObjectId());
			assertEquals(AddThumbnailRequest.OPERATION_NAME, thumbnailRequest.getOperation());
			assertEquals(AddThumbnailRequest.OPERATION_NAME, thumbnailRequest.getUserOperation());
		}
	}

}
