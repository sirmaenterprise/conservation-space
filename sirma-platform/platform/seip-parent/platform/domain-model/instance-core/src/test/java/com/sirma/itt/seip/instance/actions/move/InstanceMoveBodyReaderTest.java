package com.sirma.itt.seip.instance.actions.move;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Test the instance move body reader.
 *
 * @author nvelkov
 */
public class InstanceMoveBodyReaderTest {
	@Mock
	private RequestInfo request;

	@Mock
	private MultivaluedMap<String, String> paramsMap;

	@Mock
	private UriInfo uriInfo;

	@InjectMocks
	private InstanceMoveBodyReader instanceMoveBodyReader = new InstanceMoveBodyReader();

	/**
	 * Setup the mocks.
	 */
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(paramsMap.get("id")).thenReturn(Arrays.asList("instanceId"));
		Mockito.when(uriInfo.getPathParameters()).thenReturn(paramsMap);
		Mockito.when(request.getUriInfo()).thenReturn(uriInfo);
	}

	/**
	 * Test if the MessageBodyReader can produce an instance of a string type. It shouldn't be.
	 */
	@Test
	public void testIsReadableFalse() {
		Assert.assertFalse(instanceMoveBodyReader.isReadable(String.class, null, null, null));
	}

	/**
	 * Test if the MessageBodyReader can produce an instance of a MoveActionRequest type. It should be.
	 */
	@Test
	public void testIsReadableTrue() {
		Assert.assertTrue(instanceMoveBodyReader.isReadable(MoveActionRequest.class, null, null, null));
	}

	/**
	 * Test the readFrom logic with an empty input stream.
	 *
	 * @throws WebApplicationException
	 *             the web application exception
	 * @throws IOException
	 *             the io exception
	 */
	@Test(expected = BadRequestException.class)
	public void testReadFromWithAnEmptyStream() throws WebApplicationException, IOException {
		BufferedInputStream stream = new BufferedInputStream(
				new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8)));
		instanceMoveBodyReader.readFrom(null, null, null, null, null, stream);
	}

	/**
	 * Test the readFrom method with valid data.
	 *
	 * @throws WebApplicationException
	 *             the web application exception
	 * @throws IOException
	 *             the io exception
	 */
	@Test
	public void testReadFrom() throws WebApplicationException, IOException {
		try (InputStream stream = InstanceMoveBodyReaderTest.class
				.getResourceAsStream("/move-action-reader-test.json")) {
			MoveActionRequest actionRequest = instanceMoveBodyReader.readFrom(null, null, null, null, null, stream);
			assertNotNull(actionRequest);
			assertEquals("move", actionRequest.getUserOperation());
			assertEquals("instanceId", actionRequest.getTargetId());
			assertEquals("destinationId", actionRequest.getDestinationId());
		}
	}
}
