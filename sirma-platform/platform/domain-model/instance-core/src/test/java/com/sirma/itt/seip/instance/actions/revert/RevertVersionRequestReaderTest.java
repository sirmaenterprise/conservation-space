package com.sirma.itt.seip.instance.actions.revert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.json.Json;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Test for {@link RevertVersionRequestReader}.
 *
 * @author A. Kunchev
 */
public class RevertVersionRequestReaderTest {

	@InjectMocks
	private RevertVersionRequestReader reader;

	@Mock
	private MultivaluedMap<String, String> paramsMap;

	@Mock
	private UriInfo uriInfo;

	@Mock
	private RequestInfo request;

	@Before
	public void setup() {
		reader = new RevertVersionRequestReader();
		MockitoAnnotations.initMocks(this);

		when(paramsMap.get("id")).thenReturn(Arrays.asList("instance-id-v1.6"));
		when(uriInfo.getPathParameters()).thenReturn(paramsMap);
		when(request.getUriInfo()).thenReturn(uriInfo);
	}

	@Test
	public void isReadable_incorrect() {
		boolean result = reader.isReadable(String.class, null, null, null);
		assertFalse(result);
	}

	@Test
	public void isReadable_correct() {
		boolean result = reader.isReadable(RevertVersionRequest.class, null, null, null);
		assertTrue(result);
	}

	@Test
	public void readFrom_withUserOperation() throws IOException {
		String jsonAsString = Json.createObjectBuilder().add("userOperation", "userDefinedRevert").build().toString();
		RevertVersionRequest revertRequest = reader.readFrom(null, null, null, null, null,
				new ByteArrayInputStream(jsonAsString.getBytes()));

		assertNotNull(revertRequest);
		assertEquals("instance-id-v1.6", revertRequest.getTargetId());
		assertEquals("userDefinedRevert", revertRequest.getUserOperation());
		assertEquals("revertVersion", revertRequest.getOperation());
	}

	@Test
	public void readFrom_withoutUserOperation() throws IOException {
		RevertVersionRequest revertRequest = reader.readFrom(null, null, null, null, null,
				new ByteArrayInputStream("{}".getBytes()));

		assertNotNull(revertRequest);
		assertEquals("instance-id-v1.6", revertRequest.getTargetId());
		assertEquals("revertVersion", revertRequest.getUserOperation());
		assertEquals("revertVersion", revertRequest.getOperation());
	}

}
