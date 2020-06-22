package com.sirma.itt.seip.rest.handlers.writers;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_DEFAULT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.json.Json;
import javax.json.JsonArray;
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
import com.sirma.itt.seip.rest.resources.instances.InstancesLoadResponse;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;

/**
 * Tests {@link InstanceCollectionBodyWriter}.
 *
 * @author Mihail Radkov
 */
public class InstanceCollectionBodyWriterTest {

	@InjectMocks
	private InstanceCollectionBodyWriter bodyWriter;

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
		bodyWriter = new InstanceCollectionBodyWriter();
		MockitoAnnotations.initMocks(this);
		when(info.getUriInfo()).thenReturn(uriInfo);
		when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
	}

	@Test
	public void canWrite_withIncorrectType() {
		assertFalse(bodyWriter.isWriteable(String.class, null, null, null));
	}

	@Test
	public void canWrite_withCorrectType() {
		assertTrue(bodyWriter.isWriteable(InstancesLoadResponse.class, null, null, null));
	}

	/**
	 * Test if {@link InstanceToJsonSerializer} is called correctly and if the writer organizes the result in an array.
	 */
	@Test
	public void testInstanceSerializerCall() throws IOException {
		doAnswer(invokation -> {
			return null;
		}).when(instanceSerializer).serialize(anyCollectionOf(Instance.class),
				eq(InstanceToJsonSerializer.allProperties()), any(JsonGenerator.class));

		Collection<Instance> instances = Arrays.asList(new EmfInstance());
		InstancesLoadResponse response = new InstancesLoadResponse().setInstances(instances);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bodyWriter.writeTo(response, null, null, null, null, null, out);

		verify(instanceSerializer).serialize(eq(instances), any(), any(JsonGenerator.class));

		JsonArray array = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readArray();
		JsonArray empty = Json.createArrayBuilder().build();
		assertEquals(empty, array);
	}

	/**
	 * Test if {@link InstanceToJsonSerializer} is called correctly and if the writer organizes the result in an array.
	 */
	@Test
	public void testInstancePropertiesFilter() throws IOException {
		when(queryParameters.get(RequestParams.KEY_PROPERTIES)).thenReturn(Arrays.asList(HEADER_DEFAULT, TITLE));
		doAnswer(invokation -> {
			return null;
		}).when(instanceSerializer).serialize(anyCollectionOf(Instance.class), any(), any(JsonGenerator.class));

		Collection<Instance> instances = Arrays.asList(new EmfInstance());
		InstancesLoadResponse response = new InstancesLoadResponse().setInstances(instances);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bodyWriter.writeTo(response, null, null, null, null, null, out);

		verify(instanceSerializer).serialize(eq(instances), any(), any(JsonGenerator.class));

		JsonArray array = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readArray();
		JsonArray empty = Json.createArrayBuilder().build();
		assertEquals(empty, array);
	}
}
