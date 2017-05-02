package com.sirma.itt.seip.rest.handlers.writers;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_DEFAULT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.json.stream.JsonGenerator;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;

/**
 * Tests {@link InstanceBodyWriter}.
 *
 * @author Mihail Radkov
 */
public class InstanceBodyWriterTest {

	@InjectMocks
	private InstanceBodyWriter bodyWriter;

	@Mock
	private InstanceToJsonSerializer instanceSerializer;
	@Mock
	private InstanceLoadDecorator instanceLoadDecorator;
	@Mock
	private RequestInfo info;
	@Mock
	private UriInfo uriInfo;
	@Mock
	private MultivaluedMap<String, String> queryParameters;

	@Before
	public void init() {
		bodyWriter = new InstanceBodyWriter();
		MockitoAnnotations.initMocks(this);
		when(info.getUriInfo()).thenReturn(uriInfo);
		when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
	}

	/**
	 * Test if class is supported by the writer.
	 */
	@Test
	public void testCanWrite() {
		assertTrue(bodyWriter.isWriteable(Instance.class, null, null, null));
		assertFalse(bodyWriter.isWriteable(String.class, null, null, null));
	}

	/**
	 * Test if {@link InstanceToJsonSerializer} is called correctly.
	 *
	 * @throws IOException
	 *             - if instance could not be written
	 */
	@Test
	@SuppressWarnings("resource")
	public void testInstanceSerializerCall() throws IOException {
		doAnswer(a -> {
			// Avoiding JsonGenerationException: Generating incomplete JSON
			JsonGenerator generator = a.getArgumentAt(2, JsonGenerator.class);
			generator.writeStartObject();
			generator.writeEnd();
			return null;
		}).when(instanceSerializer).serialize(any(Instance.class), eq(InstanceToJsonSerializer.allProperties()),
				any(JsonGenerator.class));

		Instance instance = new EmfInstance();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bodyWriter.writeTo(instance, null, null, null, null, null, out);

		verify(instanceSerializer).serialize(eq(instance), any(), any(JsonGenerator.class));
		verify(instanceLoadDecorator).decorateInstance(eq(instance));
	}

	/**
	 * Test if {@link InstanceToJsonSerializer} is called with a properties filter
	 *
	 * @throws IOException
	 *             - if instance could not be written
	 */
	@Test
	@SuppressWarnings("resource")
	public void testInstancePropertiesFilter() throws IOException {
		when(queryParameters.get(RequestParams.KEY_PROPERTIES)).thenReturn(Arrays.asList(HEADER_DEFAULT, TITLE));
		doAnswer(a -> {
			// Avoiding JsonGenerationException: Generating incomplete JSON
			JsonGenerator generator = a.getArgumentAt(2, JsonGenerator.class);
			generator.writeStartObject();
			generator.writeEnd();
			return null;
		}).when(instanceSerializer).serialize(any(Instance.class), any(), any(JsonGenerator.class));

		Instance instance = new EmfInstance();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bodyWriter.writeTo(instance, null, null, null, null, null, out);

		verify(instanceSerializer).serialize(eq(instance), any(), any(JsonGenerator.class));
		verify(instanceLoadDecorator).decorateInstance(eq(instance));
	}
}
