package com.sirma.itt.seip.instance.actions.transition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.json.JsonObject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.rest.resources.instances.InstanceResourceParser;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link InstanceTransitionBodyReader}.
 *
 * @author A. Kunchev
 */
public class InstanceTransitionBodyReaderTest {

	@InjectMocks
	private InstanceTransitionBodyReader reader;

	@Mock
	private MultivaluedMap<String, String> paramsMap;

	@Mock
	private UriInfo uriInfo;

	@Mock
	private RequestInfo request;

	@Mock
	private InstanceResourceParser instanceResourceParser;

	@Before
	public void setup() {
		reader = new InstanceTransitionBodyReader();
		MockitoAnnotations.initMocks(this);
		Mockito.when(paramsMap.get("id")).thenReturn(Arrays.asList("instanceId"));
		Mockito.when(uriInfo.getPathParameters()).thenReturn(paramsMap);
		Mockito.when(request.getUriInfo()).thenReturn(uriInfo);
	}

	@Test
	public void isReadable_stringClass_false() {
		assertFalse(reader.isReadable(String.class, null, null, null));
	}

	@Test
	public void isReadable() {
		assertTrue(reader.isReadable(TransitionActionRequest.class, null, null, null));
	}

	@Test(expected = BadRequestException.class)
	public void readFrom_emptyStream() throws WebApplicationException, IOException {
		BufferedInputStream stream = new BufferedInputStream(
				new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8)));
		reader.readFrom(null, null, null, null, null, stream);
	}

	@Test(expected = ResourceException.class)
	public void readFrom_nullInstance() throws WebApplicationException, IOException {
		try (InputStream stream = InstanceTransitionBodyReaderTest.class
				.getResourceAsStream("/immediate-action-reader-test.json")) {
			when(instanceResourceParser.toInstance(any(JsonObject.class), anyString())).thenReturn(null);
			reader.readFrom(null, null, null, null, null, stream);
		}
	}

	@Test
	public void readFrom() throws WebApplicationException, IOException {
		try (InputStream stream = InstanceTransitionBodyReaderTest.class
				.getResourceAsStream("/immediate-action-reader-test.json")) {
			InstanceReference reference = InstanceReferenceMock.createGeneric("emd:id");

			when(instanceResourceParser.toInstance(any(JsonObject.class), anyString()))
					.thenReturn(reference.toInstance());
			TransitionActionRequest actionRequest = reader.readFrom(null, null, null, null, null, stream);
			assertNotNull(actionRequest);
			assertEquals("approve", actionRequest.getUserOperation());
			assertEquals("transition", actionRequest.getOperation());
			assertEquals("instanceId", actionRequest.getTargetId());
			assertEquals(reference.toInstance(), actionRequest.getTargetInstance());
			assertEquals(2, actionRequest.getContextPath().size());
			assertEquals("path", actionRequest.getContextPath().get(0));
			assertEquals("path1", actionRequest.getContextPath().get(1));
		}
	}

}
