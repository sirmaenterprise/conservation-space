package com.sirma.itt.seip.rest.handlers.writers;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.CREATED_BY;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.CREATED_ON;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_COMPACT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_DEFAULT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.THUMBNAIL_IMAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.BeanManager;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerationException;
import javax.json.stream.JsonGenerator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.relation.InstanceRelationsService;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.version.VersionProperties;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.fakes.TaskExecutorFake;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceContextServiceMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;
import com.sirma.itt.seip.time.ISO8601DateFormat;
import com.sirma.itt.seip.util.CDI;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Tests for custom message writer for {@link Instance} -> {@link JsonObject}.
 *
 * @author yasko
 */
public class InstanceToJsonSerializerTest {

	private static final JsonObject COMPACT_HEADER_JSON_OBJECT = Json
			.createObjectBuilder()
				.add(DefaultProperties.HEADER_COMPACT, "compactHeader")
				.build();

	private static final JsonObject SEMANTIC_HIERARCHY_ONLY_OBJECT = Json
			.createObjectBuilder()
				.add("semanticHierarchy", Json
						.createArrayBuilder()
							.add("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Activity")
							.add("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Case"))
				.build();

	@InjectMocks
	private InstanceToJsonSerializer writer;

	@Mock
	private DefinitionService definitionService;
	@Mock
	private TypeConverter typeConverter;
	@Mock
	private InstanceTypeResolver instanceTypeResolver;
	@Mock
	private InstanceLoadDecorator instanceLoadDecorator;
	@Mock
	private CodelistService codelistService;
	@Spy
	private InstanceContextServiceMock contextService;
	@Mock
	private LinkService linkService;
	@Spy
	private SecurityContextManager securityManager = new SecurityContextManagerFake();
	@Spy
	private TaskExecutor taskExecutor = new TaskExecutorFake();

	private DefinitionModel definition;
	private Instance instance;
	private EmfInstance owned;

	@Mock
	private SemanticDefinitionService semanticDefinitionService;
	@Mock
	private InstanceRelationsService instanceRelationsService;

	@SuppressWarnings("unchecked")
	@Before
	// TODO refactor ;/
	public void init() {
		writer = new InstanceToJsonSerializer();
		CDI.setCachedBeanManager(mock(BeanManager.class));
		MockitoAnnotations.initMocks(this);

		instance = InstanceReferenceMock.createGeneric("1").toInstance();
		instance.setIdentifier("id-1-definition");
		instance.add(DefaultProperties.HEADER_COMPACT, "compactHeader");
		instance.add(CREATED_BY, "emf:admin");
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.set(2016, 5, 4, 10, 44, 56);
		calendar.set(Calendar.MILLISECOND, 0);
		instance.add(CREATED_ON, ISO8601DateFormat.format(calendar));
		instance.add("intProp", Integer.valueOf(1));
		instance.add("longProp", Long.valueOf(1));
		instance.add("floatProp", Float.valueOf(1));
		instance.add("doubleProp", Double.valueOf(1));
		instance.add("BooleanProp", Boolean.valueOf(true));
		instance.add("uriProp", new ShortUri("emf:Case"));
		instance.add("has:assignee", (Serializable) Arrays.asList("emf:admin", "emf:test"));
		instance.add("uriPropCollection", new ShortUri("emf:Case"));
		instance.add("codelistProp", "clValue");
		instance.add("$tempProperty$", "tempProperty");
		instance.add("multivalueCodelistProp", (Serializable) Arrays.asList("clValue1", "clValue2"));

		owned = (EmfInstance) InstanceReferenceMock.createGeneric("2").toInstance();
		owned.setIdentifier("id-2-definition");
		contextService.bindContext(owned, "id-2-owner-id");
		definition = new DefinitionMock();

		PropertyDefinitionMock compactHeader = new PropertyDefinitionMock();
		compactHeader.setName(HEADER_COMPACT);
		compactHeader.setType(DataTypeDefinition.TEXT);
		compactHeader.setDisplayType(DisplayType.READ_ONLY);
		definition.getFields().add(compactHeader);

		PropertyDefinitionMock defaultHeader = new PropertyDefinitionMock();
		defaultHeader.setName(HEADER_DEFAULT);
		defaultHeader.setType(DataTypeDefinition.TEXT);
		defaultHeader.setDisplayType(DisplayType.READ_ONLY);
		definition.getFields().add(defaultHeader);

		PropertyDefinitionMock objectProp = new PropertyDefinitionMock();
		objectProp.setName(CREATED_BY);
		objectProp.setType(DataTypeDefinition.URI);
		objectProp.setDisplayType(DisplayType.READ_ONLY);
		definition.getFields().add(objectProp);

		PropertyDefinitionMock objectProp2 = new PropertyDefinitionMock();
		objectProp2.setName("has:assignee");
		objectProp2.setType(DataTypeDefinition.URI);
		objectProp2.setDisplayType(DisplayType.EDITABLE);
		definition.getFields().add(objectProp2);

		PropertyDefinitionMock dateProp = new PropertyDefinitionMock();
		dateProp.setName(CREATED_ON);
		dateProp.setType(DataTypeDefinition.DATETIME);
		dateProp.setDisplayType(DisplayType.READ_ONLY);
		definition.getFields().add(dateProp);

		PropertyDefinitionMock codelistProp = new PropertyDefinitionMock();
		codelistProp.setName("codelistProp");
		codelistProp.setType(DataTypeDefinition.TEXT);
		codelistProp.setCodelist(Integer.valueOf(1));
		codelistProp.setDisplayType(DisplayType.EDITABLE);
		definition.getFields().add(codelistProp);

		PropertyDefinitionMock multivalueCodelistProp = new PropertyDefinitionMock();
		multivalueCodelistProp.setName("multivalueCodelistProp");
		multivalueCodelistProp.setType(DataTypeDefinition.TEXT);
		multivalueCodelistProp.setCodelist(Integer.valueOf(1));
		multivalueCodelistProp.setDisplayType(DisplayType.EDITABLE);
		multivalueCodelistProp.setMultiValued(Boolean.TRUE);
		definition.getFields().add(multivalueCodelistProp);

		when(definitionService.getInstanceDefinition(any())).thenReturn(definition);

		when(typeConverter.convert(eq(String.class), any(String.class)))
				.thenAnswer(a -> a.getArgumentAt(1, Object.class));
		when(typeConverter.tryConvert(eq(ShortUri.class), any(String.class)))
				.thenAnswer(a -> new ShortUri(a.getArgumentAt(1, Object.class).toString()));
		when(typeConverter.convert(eq(String.class), any(Date.class)))
				.thenAnswer(a -> ISO8601DateFormat.format(a.getArgumentAt(1, Date.class)));

		when(instanceTypeResolver.resolveReferences(anyCollection())).then(a -> {
			List<InstanceReference> result = new ArrayList<>();
			for (Object id : a.getArgumentAt(0, Collection.class)) {
				result.add(InstanceReferenceMock.createGeneric(id.toString()));
			}
			return result;
		});
		when(instanceTypeResolver.resolveInstances(anyCollection())).then(a -> {
			List<Instance> result = new ArrayList<>();
			for (Object id : a.getArgumentAt(0, Collection.class)) {
				result.add(InstanceReferenceMock.createGeneric(id.toString()).toInstance());
			}
			return result;
		});
		doAnswer(a -> {
			Collection<Instance> instances = a.getArgumentAt(0, Collection.class);
			for (Instance inst : instances) {
				inst.add(HEADER_COMPACT, HEADER_COMPACT + " of " + inst.getId());
				inst.add(HEADER_DEFAULT, HEADER_DEFAULT + " of " + inst.getId());
				inst.add(THUMBNAIL_IMAGE, THUMBNAIL_IMAGE + " of " + inst.getId());
			}
			return null;
		}).when(instanceLoadDecorator).decorateResult(anyCollection());

		when(semanticDefinitionService.getHierarchy("emf:Case"))
				.thenReturn(Arrays.asList("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Activity",
						"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Case"));

		when(codelistService.getDescription(anyInt(), anyString())).then(
				a -> "Description of " + a.getArgumentAt(0, Integer.class) + "-" + a.getArgumentAt(1, String.class));

		TypeConverterUtil.setTypeConverter(typeConverter);

		final Function<Serializable, List<String>> relationsMapper = value -> {
			if (value instanceof Collection) {
				return (List<String>) value;
			}

			return Collections.singletonList(value.toString());
		};

		when(instanceRelationsService.evaluateRelations(any(), anyCollection())).thenAnswer(a -> {
			Collection<String> properties = a.getArgumentAt(1, Collection.class);
			Instance inst = a.getArgumentAt(0, Instance.class);
			return properties.stream().collect(
					Collectors.toMap(Function.identity(), prop -> relationsMapper.apply(inst.get(prop))));
		});
		when(instanceRelationsService.getDefaultLimitPerInstanceProperty()).thenReturn(5);
	}

	@Test
	public void serialize_withoutProperties() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (JsonGenerator generator = Json.createGenerator(out)) {
			writer.serialize(instance, generator);
			generator.flush();
		}

		JsonObject object = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readObject();
		assertEquals(7, object.size());
		assertEquals("1", object.getString(JsonKeys.ID));
		assertEquals("id-1-definition", object.getString(JsonKeys.DEFINITION_ID));
		assertEquals(COMPACT_HEADER_JSON_OBJECT, object.getJsonObject(JsonKeys.INSTANCE_HEADERS));

		out = new ByteArrayOutputStream();
		try (JsonGenerator generator = Json.createGenerator(out)) {
			writer.serialize(owned, generator);
			generator.flush();
		}

		object = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readObject();
		assertEquals(8, object.size());
		assertEquals("2", object.getString(JsonKeys.ID));
		assertEquals("id-2-definition", object.getString(JsonKeys.DEFINITION_ID));
		assertEquals("id-2-owner-id", object.getString(JsonKeys.PARENT_ID));
		JsonAssert.assertJsonEquals(SEMANTIC_HIERARCHY_ONLY_OBJECT.getJsonArray("semanticHierarchy"),
				object.getJsonObject(JsonKeys.PROPERTIES).getJsonArray("semanticHierarchy"));

		contextService.bindContext(owned, null);

		out = new ByteArrayOutputStream();
		try (JsonGenerator generator = Json.createGenerator(out)) {
			writer.serialize(owned, generator);
			generator.flush();
		}

		object = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readObject();
		assertEquals(7, object.size());
		JsonAssert.assertJsonEquals(SEMANTIC_HIERARCHY_ONLY_OBJECT.getJsonArray("semanticHierarchy"),
				object.getJsonObject(JsonKeys.PROPERTIES).getJsonArray("semanticHierarchy"));
	}

	@Test
	public void serializeWithPropertiesFilter_withoutProperties() {
		instance.add(DefaultProperties.SEMANTIC_TYPE, "semanticType");

		when(semanticDefinitionService.getHierarchy("semanticType")).thenReturn(Arrays.asList("type1", "type2"));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (JsonGenerator generator = Json.createGenerator(out)) {
			writer.serialize(instance, PropertiesFilterBuilder.MATCH_NONE, generator);
			generator.flush();
		}

		JsonObject object = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readObject();
		assertEquals(7, object.size());
		assertEquals("1", object.getString(JsonKeys.ID));
		assertEquals("id-1-definition", object.getString(JsonKeys.DEFINITION_ID));
		assertEquals("{}", object.getJsonObject(JsonKeys.INSTANCE_HEADERS).toString());
		JsonObject properties = object.getJsonObject(JsonKeys.PROPERTIES);
		assertEquals(1, properties.size());
		assertNotNull(properties.get(DefaultProperties.SEMANTIC_HIERARCHY));
	}

	@Test
	public void serialize_withProperties() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (JsonGenerator generator = Json.createGenerator(out)) {
			writer.serialize(instance, generator);
			generator.flush();
		}

		JsonObject object = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readObject();
		assertEquals(7, object.size());
		assertEquals("1", object.getString(JsonKeys.ID));
		assertEquals("id-1-definition", object.getString(JsonKeys.DEFINITION_ID));
		assertEquals(COMPACT_HEADER_JSON_OBJECT, object.getJsonObject(JsonKeys.INSTANCE_HEADERS));
		JsonAssert.assertJsonEquals(ResourceLoadUtil.loadResource(getClass(), "instance-properties.json"),
				object.toString());
		verify(instanceRelationsService).evaluateRelations(any(), anyCollection());
	}

	@Test
	public void serialize_versionInstance_withProperties() {
		instance.setId("instance-id-v1.7");
		instance.add(VersionProperties.IS_VERSION, Boolean.TRUE);
		instance.add(VersionProperties.HAS_VIEW_CONTENT, Boolean.TRUE);
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.set(2017, 2, 4, 10, 00, 00);
		calendar.set(Calendar.MILLISECOND, 0);
		instance.add(VersionProperties.VERSION_CREATION_DATE, ISO8601DateFormat.format(calendar));
		instance.add(VersionProperties.ORIGINAL_INSTANCE_ID, "instance-id");
		instance.add(VersionProperties.QUERIES_RESULTS, "{}");
		instance.add(VersionProperties.MANUALLY_SELECTED, "{}");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (JsonGenerator generator = Json.createGenerator(out)) {
			writer.serialize(instance, generator);
			generator.flush();
		}

		JsonObject object = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readObject();
		assertEquals(7, object.size());
		assertEquals("instance-id-v1.7", object.getString(JsonKeys.ID));
		assertEquals("id-1-definition", object.getString(JsonKeys.DEFINITION_ID));
		assertEquals(COMPACT_HEADER_JSON_OBJECT, object.getJsonObject(JsonKeys.INSTANCE_HEADERS));
		JsonAssert.assertJsonEquals(ResourceLoadUtil.loadResource(getClass(), "version-instance-properties.json"),
				object.toString());
	}

	@Test
	public void serialize_customJsonStart() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (JsonGenerator generator = Json.createGenerator(out)) {
			generator.writeStartObject();
			writer.serialize("instanceData", instance, generator);
			generator.writeEnd();
			generator.flush();
		}

		JsonObject object = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readObject();
		assertEquals(1, object.size());
		object = object.getJsonObject("instanceData");
		assertNotNull(object);
		assertEquals(7, object.size());
		assertEquals("1", object.getString(JsonKeys.ID));
		assertEquals("id-1-definition", object.getString(JsonKeys.DEFINITION_ID));
		assertEquals(COMPACT_HEADER_JSON_OBJECT, object.getJsonObject(JsonKeys.INSTANCE_HEADERS));
		JsonAssert.assertJsonEquals(ResourceLoadUtil.loadResource(getClass(), "instance-properties.json"),
				object.toString());
	}

	@Test(expected = EmfRuntimeException.class)
	public void serialize_withoutIdentifiers() {
		instance.setId(null);
		instance.setIdentifier(null);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (JsonGenerator generator = Json.createGenerator(out)) {
			writer.serialize(instance, generator);
			generator.flush();
		}
	}

	/**
	 * Test serialize w/ relations.
	 *
	 * @throws Exception
	 *             thrown from classes being tested.
	 */
	@Test
	public void serialize_withRelations() {
		LinkReference link = new LinkReference();
		link.setFrom(instance.toReference());
		link.setIdentifier("emf:hasParent");
		InstanceReferenceMock toReference = InstanceReferenceMock.createGeneric("emf:parent");
		Instance relationInstance = toReference.toInstance();
		relationInstance.add(HEADER_COMPACT, "compact header");
		link.setTo(toReference);

		when(linkService.getInstanceRelations(eq(instance), any())).thenReturn(Arrays.asList(link));
		when(linkService.convertToLinkInstance(anyList())).then(a -> {
			List<LinkInstance> links = new ArrayList<>();
			List<LinkReference> references = a.getArgumentAt(0, List.class);
			for (LinkReference linkReference : references) {
				LinkInstance linkInstance = linkReference.toLinkInstance();
				// copy property from the instance that is in the reference
				linkInstance.setTo(linkReference.getTo().toInstance());
				links.add(linkInstance);
			}
			return links;
		});

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (JsonGenerator generator = Json.createGenerator(out)) {
			writer.serialize(instance, generator);
			generator.flush();
		}

		JsonObject object = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readObject();
		assertEquals(7, object.size());
		assertEquals("1", object.getString(JsonKeys.ID));
		assertEquals("id-1-definition", object.getString(JsonKeys.DEFINITION_ID));
		assertEquals(COMPACT_HEADER_JSON_OBJECT, object.getJsonObject(JsonKeys.INSTANCE_HEADERS));
		JsonAssert.assertJsonEquals(ResourceLoadUtil.loadResource(getClass(), "instance-properties.json"),
				object.toString());
		verify(instanceRelationsService).evaluateRelations(any(), anyCollection());
	}

	@Test
	public void serializeCollection_withRelations() {
		LinkReference link = new LinkReference();
		link.setFrom(instance.toReference());
		link.setIdentifier("emf:hasParent");
		InstanceReferenceMock toReference = InstanceReferenceMock.createGeneric("emf:parent");
		Instance relationInstance = toReference.toInstance();
		relationInstance.add(HEADER_COMPACT, "compact header");
		link.setTo(toReference);

		when(linkService.getInstanceRelations(eq(instance), any())).thenReturn(Arrays.asList(link));
		when(linkService.convertToLinkInstance(anyList())).then(a -> {
			List<LinkInstance> links = new ArrayList<>();
			List<LinkReference> references = a.getArgumentAt(0, List.class);
			for (LinkReference linkReference : references) {
				LinkInstance linkInstance = linkReference.toLinkInstance();
				// copy property from the instance that is in the reference
				linkInstance.setTo(linkReference.getTo().toInstance());
				links.add(linkInstance);
			}
			return links;
		});

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (JsonGenerator generator = Json.createGenerator(out)) {
			writer.serialize(Arrays.asList(instance), generator);
			generator.flush();
		}

		JsonObject object = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readObject();
		assertEquals(7, object.size());
		assertEquals("1", object.getString(JsonKeys.ID));
		assertEquals("id-1-definition", object.getString(JsonKeys.DEFINITION_ID));
		assertEquals(COMPACT_HEADER_JSON_OBJECT, object.getJsonObject(JsonKeys.INSTANCE_HEADERS));
		JsonAssert.assertJsonEquals(ResourceLoadUtil.loadResource(getClass(), "instance-properties.json"),
				object.toString());
		verify(instanceRelationsService).evaluateRelations(any(), anyCollection());
	}

	@Test
	public void serialize_withoutThumbnail() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (JsonGenerator generator = Json.createGenerator(out)) {
			writer.serialize(instance, generator);
			generator.flush();
		}
		JsonObject object = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readObject();
		assertNull(object.get(DefaultProperties.THUMBNAIL_IMAGE));
	}

	@Test
	public void serialize_withThumbnail() {
		instance.add(DefaultProperties.THUMBNAIL_IMAGE, "thumbnail-data");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (JsonGenerator generator = Json.createGenerator(out)) {
			writer.serialize(instance, generator);
			generator.flush();
		}
		JsonObject object = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readObject();
		assertEquals("thumbnail-data", object.getString(DefaultProperties.THUMBNAIL_IMAGE));
	}

	@Test
	public void serialize_deletedInstance() {
		instance.markAsDeleted();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (JsonGenerator generator = Json.createGenerator(out)) {
			writer.serialize(instance, generator);
			generator.flush();
		}
		JsonObject object = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readObject();
		assertTrue(object.getBoolean("deleted"));
	}

	@Test
	public void serialize_semanticHierarchy() {
		instance.add(DefaultProperties.SEMANTIC_TYPE, "rdfType");

		List<String> semanticHierarchy = new ArrayList<>(2);
		semanticHierarchy.add("rdfType_parent1");
		semanticHierarchy.add("rdfType_parent2");
		when(semanticDefinitionService.getHierarchy("rdfType")).thenReturn(semanticHierarchy);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (JsonGenerator generator = Json.createGenerator(out)) {
			writer.serialize(instance, generator);
			generator.flush();
		}

		JsonObject object = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readObject();
		JsonObject jsonProperties = object.getJsonObject(JsonKeys.PROPERTIES);
		JsonArray actual = jsonProperties.getJsonArray(DefaultProperties.SEMANTIC_HIERARCHY);
		JsonArray expected = Json.createArrayBuilder().add("rdfType_parent1").add("rdfType_parent2").build();

		assertEquals(expected, actual);
	}

	@Test(expected = JsonGenerationException.class)
	public void serialize_generationException() {
		try (JsonGenerator generator = Mockito.mock(JsonGenerator.class)) {
			when(generator.write(anyString(), anyString())).thenThrow(new JsonGenerationException("incomplete"));
			writer.serialize(instance, generator);
		}
	}
}
