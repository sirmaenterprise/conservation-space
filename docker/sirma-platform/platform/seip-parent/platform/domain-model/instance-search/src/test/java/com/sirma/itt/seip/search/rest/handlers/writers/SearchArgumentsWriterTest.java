package com.sirma.itt.seip.search.rest.handlers.writers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.rest.handlers.writers.InstanceToJsonSerializer;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Test for {@link SearchArgumentsWriter}.
 */
public class SearchArgumentsWriterTest {

	@InjectMocks
	private SearchArgumentsWriter writer;

	@Mock
	private InstanceToJsonSerializer serializer;
	@Mock
	private RequestInfo info;
	@Mock
	private UriInfo uriInfo;
	@Mock
	private MultivaluedMap<String, String> queryParameters;

	@Before
	public void init() throws Exception {
		writer = new SearchArgumentsWriter();
		MockitoAnnotations.initMocks(this);

		Mockito.doAnswer(invocation -> {
			JsonGenerator generator = invocation.getArgumentAt(2, JsonGenerator.class);
			generator.writeStartObject().writeEnd();
			return null;
		}).when(serializer).serialize(anyCollectionOf(Instance.class), any(), any(JsonGenerator.class));

		when(info.getUriInfo()).thenReturn(uriInfo);
		when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
	}

	@Test
	public void testIsReadable() {
		ParameterizedType generic = mock(ParameterizedType.class);
		when(generic.getActualTypeArguments()).thenReturn(new Class[] { Instance.class });
		assertTrue(writer.isWriteable(SearchArguments.class, generic, null, null));

		when(generic.getActualTypeArguments()).thenReturn(new Class[] { String.class });
		assertFalse(writer.isWriteable(SearchArguments.class, generic, null, null));

		when(generic.getActualTypeArguments()).thenReturn(new Class[] {});
		assertFalse(writer.isWriteable(SearchArguments.class, generic, null, null));

		when(generic.getActualTypeArguments()).thenReturn(null);
		assertFalse(writer.isWriteable(SearchArguments.class, generic, null, null));

		when(generic.getActualTypeArguments()).thenReturn(new Class[] { Instance.class });
		assertFalse(writer.isWriteable(String.class, generic, null, null));
	}

	@Test
	public void testWriteZeroResults() throws IOException {
		SearchArguments<Instance> args = new SearchArguments<>();
		args.setTotalItems(0);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writer.writeTo(args, null, null, null, null, null, out);

		JsonStructure actual = Json.createReader(new ByteArrayInputStream(out.toByteArray())).read();
		JsonObjectBuilder expected = Json.createObjectBuilder();
		expected.add(JsonKeys.RESULT_SIZE, 0);
		expected.add(JsonKeys.PAGE, 1);
		expected.add(JsonKeys.MESSAGE, JsonValue.NULL);
		expected.add(JsonKeys.VALUES, Json.createArrayBuilder().build());
		JsonAssert.assertJsonEquals(expected.build().toString(), actual.toString());
	}

	@Test
	public void testWriteWithResults() throws Exception {
		SearchArguments<Instance> args = new SearchArguments<>();
		args.setTotalItems(1);
		args.setResult(Arrays.asList(mock(Instance.class)));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writer.writeTo(args, null, null, null, null, null, out);

		JsonStructure actual = Json.createReader(new ByteArrayInputStream(out.toByteArray())).read();
		JsonObjectBuilder expected = Json.createObjectBuilder();
		expected.add(JsonKeys.RESULT_SIZE, 1);
		expected.add(JsonKeys.PAGE, 1);
		expected.add(JsonKeys.MESSAGE, JsonValue.NULL);
		JsonArray build = Json.createArrayBuilder().add(Json.createObjectBuilder().build()).build();
		expected.add(JsonKeys.VALUES, build);
		JsonAssert.assertJsonEquals(expected.build().toString(), actual.toString());
	}

	@Test
	public void testWriteError() throws Exception {
		SearchArguments<Instance> args = new SearchArguments<>();
		args.setTotalItems(1);
		args.setSearchError(new Exception("error"));
		args.setResult(Arrays.asList(mock(Instance.class)));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writer.writeTo(args, null, null, null, null, null, out);

		JsonStructure actual = Json.createReader(new ByteArrayInputStream(out.toByteArray())).read();
		JsonObjectBuilder expected = Json.createObjectBuilder();
		expected.add(JsonKeys.RESULT_SIZE, 1);
		expected.add(JsonKeys.PAGE, 1);
		expected.add(JsonKeys.MESSAGE, "error");
		JsonArray build = Json.createArrayBuilder().add(Json.createObjectBuilder().build()).build();
		expected.add(JsonKeys.VALUES, build);
		JsonAssert.assertJsonEquals(expected.build().toString(), actual.toString());
	}

	@Test(expected = InternalServerErrorException.class)
	public void testInstanceWriteError() throws Exception {
		List<Instance> results = Arrays.asList(mock(Instance.class));
		doThrow(new RuntimeException()).when(serializer).serialize(eq(results), any(), any(JsonGenerator.class));

		SearchArguments<Instance> args = new SearchArguments<>();
		args.setTotalItems(1);
		args.setResult(results);

		writer.writeTo(args, null, null, null, null, null, new ByteArrayOutputStream());
	}

	@Test
	public void testAggregatedDataWrite() throws IOException {
		SearchArguments<Instance> args = new SearchArguments<>();
		args.setAggregated(CollectionUtils.createHashMap(1));
		args.setShouldGroupBy(true);

		Map<String, Serializable> propertyData = CollectionUtils.createHashMap(2);
		propertyData.put("key1", 10);
		propertyData.put("key2", 15);
		args.getAggregatedData().put("emf:property", propertyData);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writer.writeTo(args, null, null, null, null, null, out);

		JsonObject actual = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readObject();

		JsonObjectBuilder propertyDataAsJson = Json.createObjectBuilder();
		propertyDataAsJson.add("key1", 10);
		propertyDataAsJson.add("key2", 15);

		JsonObjectBuilder propertyMapAsJson = Json.createObjectBuilder();
		propertyMapAsJson.add("emf:property", propertyDataAsJson);

		JsonObject aggregated = actual.getJsonObject(JsonKeys.AGGREGATED);
		JsonAssert.assertJsonEquals(propertyMapAsJson.build(), aggregated);
	}

	@Test
	public void testAggregatedDataWriteWithEmptyData() throws IOException {
		SearchArguments<Instance> args = new SearchArguments<>();
		args.setAggregated(CollectionUtils.createHashMap(1));
		args.setShouldGroupBy(true);
		args.getAggregatedData().put("emf:property", CollectionUtils.emptyMap());

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writer.writeTo(args, null, null, null, null, null, out);

		JsonObject actual = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readObject();

		JsonObjectBuilder propertyMapAsJson = Json.createObjectBuilder();
		propertyMapAsJson.add("emf:property", Json.createObjectBuilder());

		JsonObject aggregated = actual.getJsonObject(JsonKeys.AGGREGATED);
		JsonAssert.assertJsonEquals(propertyMapAsJson.build(), aggregated);
	}

	@Test
	public void testWriteWithHighLigthedResults() throws Exception {
		SearchArguments<Instance> args = new SearchArguments<>();
		args.setTotalItems(2);
		args.setResult(Arrays.asList(new EmfInstance("emf:instance"), new EmfInstance("emf:instance-2")));
		args.setHighlight(Collections.singletonList("emf:instance"));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writer.writeTo(args, null, null, null, null, null, out);

		JsonStructure actual = Json.createReader(new ByteArrayInputStream(out.toByteArray())).read();
		JsonObjectBuilder expected = Json.createObjectBuilder();
		expected.add(JsonKeys.RESULT_SIZE, 2);
		expected.add(JsonKeys.PAGE, 1);
		expected.add(JsonKeys.MESSAGE, JsonValue.NULL);
		expected.add(JsonKeys.HIGHLIGHT, Json.createArrayBuilder().add("emf:instance"));
		JsonArray build = Json.createArrayBuilder().add(Json.createObjectBuilder().build()).build();
		expected.add(JsonKeys.VALUES, build);
		JsonAssert.assertJsonEquals(expected.build().toString(), actual.toString());
	}
}
