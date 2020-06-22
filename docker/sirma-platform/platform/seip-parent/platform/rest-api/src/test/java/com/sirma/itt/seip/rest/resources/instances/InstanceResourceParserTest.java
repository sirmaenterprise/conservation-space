package com.sirma.itt.seip.rest.resources.instances;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.CREATED_BY;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.CREATED_ON;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.MODIFIED_ON;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TEMP_CONTENT_VIEW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.stream.JsonParsingException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;
import com.sirma.itt.seip.time.ISO8601DateFormat;

/**
 * Test for {@link InstanceResourceParser}.
 *
 * @author velikov
 */
public class InstanceResourceParserTest {

	private static final String INSTANCE_WITH_RELATIONS_AND_QUERIES = "parse-instance-with-relations-test.json";

	private static final String INSTANCE_LIST_TEST_JSON = "instance-list-w-properties-and-purpose.json";

	private static final String CREATE_INSTANCE_TEST_JSON = "create-instance-w-properties.json";

	private static final String INTEGRATION_INSTANCE = "integration.json";

	private static final int DOCUMENT1_WITH_CONTENT = 0;

	private static final int DOCUMENT2 = 1;

	@InjectMocks
	private InstanceResourceParser resourceParser;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	private TypeConverter typeConverter;

	private DefinitionModel definition;

	@Mock
	private InstanceService instanceService;

	@Mock
	private InstanceVersionService instanceVersionService;

	@Mock
	private DefinitionService definitionService;

	private Instance document1;
	private Instance document2;

	@Before
	public void init() {
		resourceParser = new InstanceResourceParser();
		MockitoAnnotations.initMocks(this);

		document1 = new EmfInstance();
		document1.setId("emf:1");
		document2 = new EmfInstance();
		document2.setId("emf:2");

		definition = new DefinitionMock();
		PropertyDefinitionMock dateProperty = new PropertyDefinitionMock();
		dateProperty.setType(DataTypeDefinition.DATETIME);
		dateProperty.setName(CREATED_ON);
		definition.getFields().add(dateProperty);

		PropertyDefinitionMock nullDateProperty = new PropertyDefinitionMock();
		nullDateProperty.setType(DataTypeDefinition.DATETIME);
		nullDateProperty.setName(MODIFIED_ON);
		definition.getFields().add(nullDateProperty);

		PropertyDefinitionMock objectProperty1 = new PropertyDefinitionMock();
		objectProperty1.setType(DataTypeDefinition.URI);
		objectProperty1.setName(CREATED_BY);
		objectProperty1.setDisplayType(DisplayType.READ_ONLY);
		definition.getFields().add(objectProperty1);

		PropertyDefinitionMock objectProperty2 = new PropertyDefinitionMock();
		objectProperty2.setType(DataTypeDefinition.URI);
		objectProperty2.setName("has:assignee");
		objectProperty2.setDisplayType(DisplayType.EDITABLE);
		definition.getFields().add(objectProperty2);

		when(domainInstanceService.loadInstance("emf:1")).thenReturn(document1);
		when(domainInstanceService.loadInstance("emf:2")).thenReturn(document2);
		when(definitionService.getInstanceDefinition(any(Instance.class))).thenReturn(definition);

		when(typeConverter.convert(eq(Date.class), anyString()))
				.then(a -> ISO8601DateFormat.parse(a.getArgumentAt(1, String.class)));
	}

	@Test
	public void convert_multiple_instances_provided_as_stream_of_json_data() throws Exception {
		List<Instance> instanceList = (List<Instance>) resourceParser
				.toInstanceList(getClass().getClassLoader().getResourceAsStream(INSTANCE_LIST_TEST_JSON));
		assertNotNull(instanceList);
		assertEquals(instanceList.size(), 2);

		Instance convertedDocument1 = instanceList.get(DOCUMENT1_WITH_CONTENT);
		Map<String, Serializable> document1Properties = convertedDocument1.getProperties();
		assertEquals(3, document1Properties.size());
		assertEquals("document 1 content", document1Properties.get(DefaultProperties.TEMP_CONTENT_VIEW));
		assertEquals("iDoc", document1Properties.get(DefaultProperties.EMF_PURPOSE));

		Instance convertedDocument2 = instanceList.get(DOCUMENT2);
		Map<String, Serializable> document2Properties = convertedDocument2.getProperties();
		assertEquals(2, document2Properties.size());
		assertEquals("iDoc", document2Properties.get(DefaultProperties.EMF_PURPOSE));
	}

	@Test(expected = NullPointerException.class)
	public void toInstanceList_nullStream() throws IOException {
		InputStream stream = null;
		resourceParser.toInstanceList(stream);
	}

	@Test(expected = JsonParsingException.class)
	public void toInstanceList_closedStream() throws IOException {
		try (InputStream stream = new ByteArrayInputStream("stream".getBytes(StandardCharsets.UTF_8))) {
			stream.close();
			resourceParser.toInstanceList(stream);
		}
	}

	@Test(expected = JsonParsingException.class)
	public void toInstanceList_errorWhileStreamReading() throws IOException {
		try (InputStream stream = mock(InputStream.class)) {
			when(stream.read(any(byte[].class), anyInt(), anyInt())).thenThrow(new IOException());
			resourceParser.toInstanceList(stream);
		}
	}

	@Test(expected = JsonException.class)
	public void toInstanceList_jsonNotArray() throws IOException {
		try (InputStream stream = new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8))) {
			resourceParser.toInstanceList(stream);
		}
	}

	@Test
	public void toInstanceList_emptyJson() throws IOException {
		try (InputStream stream = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8))) {
			Collection<Instance> instances = resourceParser.toInstanceList(stream);
			assertEquals(Collections.emptyList(), instances);
		}
	}

	@Test(expected = JsonException.class)
	public void toInstanceList_emptyStream() throws IOException {
		try (InputStream stream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8))) {
			resourceParser.toInstanceList(stream);
		}
	}

	@Test
	public void toInstanceList_createInstanceCalled() throws IOException {
		try (InputStream stream = getClass().getClassLoader().getResourceAsStream(CREATE_INSTANCE_TEST_JSON)) {
			DefinitionMock instanceDefinition = new DefinitionMock();
			EmfInstance testInstance = new EmfInstance();
			when(domainInstanceService.createInstance("def-1", null)).thenReturn(testInstance);
			when(definitionService.getInstanceDefinition(testInstance)).thenReturn(instanceDefinition);
			Collection<Instance> instances = resourceParser.toInstanceList(stream);
			assertEquals(1, instances.size());
		}
	}

	@Test
	public void toInstanceListJsonArray_emptyArray_emptyCollection() {
		Collection<Instance> instances = resourceParser.toInstanceList(Json.createArrayBuilder().build());
		assertEquals(Collections.emptyList(), instances);
	}

	@Test
	public void toSingleInstance_withRelations() {
		JsonObject object = Json
				.createReader(getClass().getClassLoader().getResourceAsStream(INSTANCE_WITH_RELATIONS_AND_QUERIES))
					.readObject();

		EmfInstance instance = new EmfInstance();
		instance.add("has:assignee", (Serializable) Arrays.asList("emf:objPropToRemove"));
		when(domainInstanceService.loadInstance("instance-id")).thenReturn(instance);

		Function<JsonObject, Instance> singleInstance = resourceParser.toSingleInstance("instance-id");
		Instance resultInstance = singleInstance.apply(object);

		assertEquals(resultInstance.getString("title"), "source instance");
		assertNotNull(resultInstance.get(TEMP_CONTENT_VIEW));
		assertNotNull(resultInstance.get(CREATED_BY));
		assertNotNull(resultInstance.get(CREATED_ON));
		assertEquals(2, resultInstance.get("has:assignee", Collection.class).size());
		// null values should be transfered in the instance as is
		assertNull(resultInstance.get(MODIFIED_ON));
	}

	@Test
	public void testIntegrationJson() {
		JsonObject object = Json
				.createReader(getClass().getClassLoader().getResourceAsStream(INTEGRATION_INSTANCE))
					.readObject();
		InstanceReference reference = mock(InstanceReference.class);
		TypeConverterUtil.setTypeConverter(typeConverter);
		when(reference.toInstance()).thenReturn(document1);
		when(typeConverter.convert(eq(InstanceReference.class), anyString())).thenReturn(reference);

		Instance instance = resourceParser.toInstance(object, "emf:id");

		assertNotNull(instance);
	}
}