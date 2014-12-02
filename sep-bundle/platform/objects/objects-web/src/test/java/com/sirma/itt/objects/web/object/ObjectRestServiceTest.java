package com.sirma.itt.objects.web.object;

import static org.testng.Assert.assertEquals;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;

import net.javacrumbs.jsonunit.JsonAssert;

import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.cmf.web.instance.AttachInstanceAction;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionImpl;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.BaseRegionDefinition;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.FieldDefinitionImpl;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionImpl;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.PropertiesService;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.objects.ObjectsTest;
import com.sirma.itt.objects.domain.model.ObjectInstance;
import com.sirma.itt.objects.services.ObjectService;

/**
 * The Class ObjectRestServiceTest.
 * 
 * @author svelikov
 */
@Test
public class ObjectRestServiceTest extends ObjectsTest {

	private static final String DOCUMENT_INSTANCE_ID = "documentInstanceId";
	private static final String OBJECT_INSTANCE_ID = "objectInstanceId";
	protected static final Object CASE_INSTANCE = "caseinstance";
	protected static final Object SECTION_INSTANCE = "sectioninstance";
	protected static final Object OBJECT_INSTANCE = "objectinstance";
	protected static final Object SECTION_INSTANCE_ID = "emf:482c15b7-845f-4597-a9fb-a6b451a72578";

	private final ObjectRestService controller;

	private InstanceService instanceService;

	private DictionaryService dictionaryService;

	private PropertiesService propertiesService;

	private ServiceRegister serviceRegister;

	private TypeConverter typeConverter;

	private ObjectService objectService;

	private ObjectInstance objectInstance;

	private DocumentInstance createDocumentInstance;

	private CaseInstance caseInstance;

	private SectionInstance sectionInstance;

	private boolean fetchNullInstance;

	private boolean fetchNullCase;

	private boolean fetchNullObject;

	private boolean fetchNullSection;

	private AttachInstanceAction attachInstanceAction;

	/**
	 * Instantiates a new object rest service test.
	 */
	public ObjectRestServiceTest() {
		controller = new ObjectRestService() {

			@Override
			@SuppressWarnings("unchecked")
			public <T extends Instance> T loadInstanceInternal(Class<T> type, Serializable id) {
				if (OBJECT_INSTANCE_ID.equals(id)) {
					return (T) objectInstance;
				} else if (DOCUMENT_INSTANCE_ID.equals(id)) {
					return (T) createDocumentInstance;
				} else if (SECTION_INSTANCE_ID.equals(id)) {
					return (T) sectionInstance;
				}

				return null;
			}

			@Override
			public Instance fetchInstance(String instanceId, String instanceType) {
				if (fetchNullInstance) {
					return null;
				}
				if (CASE_INSTANCE.equals(instanceType) && !fetchNullCase) {
					return caseInstance;
				} else if (SECTION_INSTANCE.equals(instanceType) && !fetchNullSection) {
					return sectionInstance;
				} else if (OBJECT_INSTANCE.equals(instanceType) && !fetchNullObject) {
					return objectInstance;
				}
				return null;
			}

			@Override
			public Resource getCurrentUser() {
				EmfUser user = new EmfUser("admin");
				user.setId("emf:" + user.getIdentifier());
				return user;
			}

		};

		objectInstance = createObjectInstance(Long.valueOf(1));
		objectInstance.setProperties(new HashMap<String, Serializable>());
		createDocumentInstance = createDocumentInstance(Long.valueOf(1));
		caseInstance = createCaseInstance(Long.valueOf(1));
		sectionInstance = createSectionInstance(Long.valueOf(1));

		instanceService = Mockito.mock(InstanceService.class);
		dictionaryService = Mockito.mock(DictionaryService.class);
		propertiesService = Mockito.mock(PropertiesService.class);
		typeConverter = Mockito.mock(TypeConverter.class);
		objectService = Mockito.mock(ObjectService.class);
		attachInstanceAction = Mockito.mock(AttachInstanceAction.class);
		serviceRegister = Mockito.mock(ServiceRegister.class);

		ReflectionUtils.setField(controller, "log", SLF4J_LOG);
		ReflectionUtils.setField(controller, "attachInstanceAction", attachInstanceAction);
		ReflectionUtils.setField(controller, "instanceService", instanceService);
		ReflectionUtils.setField(controller, "typeConverter", typeConverter);
		ReflectionUtils.setField(controller, "objectService", objectService);
		ReflectionUtils.setField(controller, "dictionaryService", dictionaryService);
		ReflectionUtils.setField(controller, "propertiesService", propertiesService);
		ReflectionUtils.setField(controller, "serviceRegister", serviceRegister);

	}

	/**
	 * Inits the test by reseting some fields.
	 */
	@BeforeMethod
	public void initTest() {
		fetchNullInstance = false;
		fetchNullCase = false;
		fetchNullSection = false;
		fetchNullObject = false;
	}

	/**
	 * Attach objects test.
	 */
	// FIXME: test
	// public void attachObjectsTest() {
	// // if data is not provided we should get error response
	// Response expectedResponse = Response.status(Response.Status.BAD_REQUEST).entity("").build();
	// Mockito.when(
	// attachInstanceAction.attachObjects(null, controller, null,
	// ObjectActionTypeConstants.ATTACH_OBJECT)).thenReturn(expectedResponse);
	// Response response = controller.attachObjects(null);
	// assertNotNull(response);
	// assertEquals(response.getStatus(), expectedResponse.getStatus());
	// }

	/**
	 * Move object same case test.
	 */
	@SuppressWarnings("boxing")
	public void moveObjectSameCaseTest() {
		// if all required arguments are missing
		Response response = controller.moveObjectSameCase(null, null, null);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
		// if source and destination id are missing
		response = controller.moveObjectSameCase("objectid", null, null);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
		// if destination id is missing
		response = controller.moveObjectSameCase("objectid", "sourceid", null);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
		// if source id is missing
		response = controller.moveObjectSameCase("objectid", null, "destid");
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		// if all required arguments are missing
		response = controller.moveObjectSameCase("", "", "");
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
		// if source and destination id are missing
		response = controller.moveObjectSameCase("objectid", "", "");
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
		// if destination id is missing
		response = controller.moveObjectSameCase("objectid", "sourceid", "");
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
		// if source id is missing
		response = controller.moveObjectSameCase("objectid", "", "destid");
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		// if object instance can't be loaded
		fetchNullObject = true;
		response = controller.moveObjectSameCase("objectid", "sourceid", "destid");
		assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		// if source section instance can't be loaded
		initTest();
		fetchNullSection = true;
		response = controller.moveObjectSameCase("objectid", "sourceid", "destid");
		assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		// if destination section instance can't be loaded
		// TODO: refactor to test this branch

		// if service respond that object can't be moved
		initTest();
		Mockito.when(
				objectService.move(Mockito.any(ObjectInstance.class),
						Mockito.any(SectionInstance.class), Mockito.any(SectionInstance.class)))
				.thenReturn(false);
		response = controller.moveObjectSameCase("objectid", "sourceid", "destid");
		assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

		// if everything is ok
		Mockito.when(
				objectService.move(Mockito.any(ObjectInstance.class),
						Mockito.any(SectionInstance.class), Mockito.any(SectionInstance.class)))
				.thenReturn(true);
		response = controller.moveObjectSameCase("objectid", "sourceid", "destid");
		assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
	}

	/**
	 * Prepare DataType for future use
	 * 
	 * @return DataTypeDefinition
	 */
	public DataTypeDefinition getDataTypeDefinition() {
		return new DataTypeDefinition() {

			@Override
			public Long getId() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void setId(Long id) {
				// TODO Auto-generated method stub

			}

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getTitle() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getDescription() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getJavaClassName() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Class<?> getJavaClass() {
				// TODO Auto-generated method stub
				return DocumentInstance.class;
			}

			@Override
			public String getFirstUri() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Set<String> getUries() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	/**
	 * Load literals with empty values.
	 */
	@SuppressWarnings("boxing")
	public void loadEmptyLiterals() {

		Instance instance = controller.loadInstanceInternal(ObjectInstance.class,
				"objectInstanceId");
		Map<String, Serializable> properties = new HashMap<String, Serializable>();
		properties.put("rdf:type",
				"http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#Book");

		instance.setProperties(properties);
		instance.setIdentifier("OT210027");

		Mockito.when(typeConverter.convert(String.class, instance.getProperties().get("rdf:type")))
				.thenReturn("http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#Book");

		Map forRest = new HashMap<String, Serializable>();
		forRest.put("description", "");
		Mockito.when(propertiesService.convertToExternalModel(instance, null)).thenReturn(forRest);

		List<PropertyDefinition> fields = new LinkedList<>();
		PropertyDefinitionProxy propertyDef = new PropertyDefinitionProxy();
		propertyDef.setName("description");
		propertyDef.setLabelId("description");
		propertyDef.setMultiValued(false);

		FieldDefinitionImpl property = new FieldDefinitionImpl();
		property.setId((long) 11);
		property.setLabelId("text");
		property.setName("text");
		property.setType("text");

		propertyDef.setDataType(getDataTypeDefinition());

		fields.add(propertyDef);

		DocumentDefinitionImpl documentDefinition = new DocumentDefinitionImpl();
		documentDefinition.setFields(fields);

		Mockito.when(dictionaryService.getInstanceDefinition(instance)).thenReturn(
				documentDefinition);

		String result = controller.loadLiteralsForType("objectinstance",
				"emf:eb7d4242-ce10-4d73-b880-96ff6796fda0", "EO007005", true);
		JsonAssert
				.assertJsonEquals(
						"[{\"linkId\":\"description\",\"cls\":\"\",\"isMultiValued\":false,\"name\":\"description\",\"value\":\"\",\"oldValue\":\"\",\"editable\":true}]",
						result);
	}

	/**
	 * Load literals without empty values.
	 */
	@SuppressWarnings("boxing")
	public void loadNonEmptyLiterals() {
		// if all required arguments are missing
		Instance instance = controller.loadInstanceInternal(ObjectInstance.class,
				"objectInstanceId");
		Map<String, Serializable> properties = new HashMap<String, Serializable>();
		properties.put("rdf:type",
				"http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#Book");

		instance.setProperties(properties);
		instance.setIdentifier("OT210027");

		Mockito.when(typeConverter.convert(String.class, instance.getProperties().get("rdf:type")))
				.thenReturn("http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#Book");

		Map forRest = new HashMap<String, Serializable>();
		forRest.put("description", "");
		Mockito.when(propertiesService.convertToExternalModel(instance, null)).thenReturn(forRest);

		List<PropertyDefinition> fields = new LinkedList<>();
		PropertyDefinitionProxy propertyDef = new PropertyDefinitionProxy();
		propertyDef.setName("description");
		propertyDef.setLabelId("description");
		propertyDef.setMultiValued(false);

		FieldDefinitionImpl property = new FieldDefinitionImpl();
		property.setId((long) 11);
		property.setLabelId("text");
		property.setName("text");
		property.setType("text");

		propertyDef.setDataType(getDataTypeDefinition());

		fields.add(propertyDef);

		DocumentDefinitionImpl documentDefinition = new DocumentDefinitionImpl();
		documentDefinition.setFields(fields);

		BaseRegionDefinition re = new BaseRegionDefinition();

		Mockito.when(dictionaryService.getInstanceDefinition(instance)).thenReturn(
				documentDefinition);

		String result = controller.loadLiteralsForType("objectinstance",
				"emf:eb7d4242-ce10-4d73-b880-96ff6796fda0", "EO007005", false);
		assertEquals(result, "[]");
	}

	/**
	 * Load literals which value is not null.
	 */
	@SuppressWarnings("boxing")
	public void loadNotNullLiterals() {
		// if all required arguments are missing
		Instance instance = controller.loadInstanceInternal(ObjectInstance.class,
				"objectInstanceId");
		Map<String, Serializable> properties = new HashMap<String, Serializable>();
		properties.put("rdf:type",
				"http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#Book");

		instance.setProperties(properties);
		instance.setIdentifier("OT210027");

		Mockito.when(typeConverter.convert(String.class, instance.getProperties().get("rdf:type")))
				.thenReturn("http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#Book");

		Map forRest = new HashMap<String, Serializable>();
		forRest.put("description", "");
		Mockito.when(propertiesService.convertToExternalModel(instance, null)).thenReturn(forRest);

		List<PropertyDefinition> fields = new LinkedList<>();
		PropertyDefinitionProxy propertyDef = new PropertyDefinitionProxy();
		propertyDef.setName("description");
		propertyDef.setLabelId("description");
		propertyDef.setMultiValued(false);

		FieldDefinitionImpl property = new FieldDefinitionImpl();
		property.setId((long) 11);
		property.setLabelId("text");
		property.setName("text");
		property.setType("text");

		propertyDef.setDataType(getDataTypeDefinition());

		fields.add(propertyDef);

		DocumentDefinitionImpl documentDefinition = new DocumentDefinitionImpl();
		documentDefinition.setFields(fields);

		BaseRegionDefinition re = new BaseRegionDefinition();

		Mockito.when(dictionaryService.getInstanceDefinition(instance)).thenReturn(
				documentDefinition);

		String result = controller.loadLiteralsForType("objectinstance",
				"emf:eb7d4242-ce10-4d73-b880-96ff6796fda0", "EO007005", false);
		assertEquals(result, "[]");
	}

	/**
	 * Load literals (test for branch region definition).
	 */
	@SuppressWarnings("boxing")
	public void loadLiteralsRegionDefinition() {
		// if all required arguments are missing
		Instance instance = controller.loadInstanceInternal(ObjectInstance.class,
				"objectInstanceId");
		Map<String, Serializable> properties = new HashMap<String, Serializable>();
		properties.put("rdf:type",
				"http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#Book");

		instance.setProperties(properties);
		instance.setIdentifier("OT210027");

		Mockito.when(typeConverter.convert(String.class, instance.getProperties().get("rdf:type")))
				.thenReturn("http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#Book");

		Map forRest = new HashMap<String, Serializable>();
		forRest.put("description", "");
		Mockito.when(propertiesService.convertToExternalModel(instance, null)).thenReturn(forRest);

		List<PropertyDefinition> fields = new LinkedList<>();
		PropertyDefinitionProxy propertyDef = new PropertyDefinitionProxy();
		propertyDef.setName("description");
		propertyDef.setLabelId("description");
		propertyDef.setMultiValued(false);

		FieldDefinitionImpl property = new FieldDefinitionImpl();
		property.setId((long) 11);
		property.setLabelId("text");
		property.setName("text");
		property.setType("text");

		propertyDef.setDataType(getDataTypeDefinition());

		fields.add(propertyDef);

		BaseRegionDefinition regionDefinition = new BaseRegionDefinition();
		regionDefinition.setFields(fields);
		RegionDefinitionImpl regionDefinitionImpl = new RegionDefinitionImpl();
		List<RegionDefinition> regionDefList = new LinkedList();
		regionDefList.add(regionDefinitionImpl);

		PropertyDefinitionProxy propertyField = new PropertyDefinitionProxy();
		List<PropertyDefinition> propertyDefinitionList = new LinkedList();
		propertyDefinitionList.add(propertyField);

		regionDefinitionImpl.setFields(propertyDefinitionList);
		regionDefinition.setRegions(regionDefList);

		Mockito.when(dictionaryService.getInstanceDefinition(instance))
				.thenReturn(regionDefinition);

		String result = controller.loadLiteralsForType("objectinstance",
				"emf:eb7d4242-ce10-4d73-b880-96ff6796fda0", "EO007005", true);
		JsonAssert
				.assertJsonEquals(
						"[{\"linkId\":\"description\",\"cls\":\"\",\"isMultiValued\":false,\"name\":\"description\",\"value\":\"\",\"oldValue\":\"\",\"editable\":true}]",
						result);

	}

	/**
	 * Load literals (test if domain class value is empty).
	 */
	@SuppressWarnings("boxing")
	public void loadLiteralsDomainClassEmpty() {
		// if all required arguments are missing
		Instance instance = controller.loadInstanceInternal(ObjectInstance.class,
				"objectInstanceId");
		Map<String, Serializable> properties = new HashMap<String, Serializable>();
		properties.put("rdf:type",
				"http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#Book");

		instance.setProperties(properties);
		instance.setIdentifier("OT210027");

		Mockito.when(typeConverter.convert(String.class, instance.getProperties().get("rdf:type")))
				.thenReturn("");

		Map forRest = new HashMap<String, Serializable>();
		forRest.put("description", "");
		Mockito.when(propertiesService.convertToExternalModel(instance, null)).thenReturn(forRest);

		List<PropertyDefinition> fields = new LinkedList<>();
		PropertyDefinitionProxy propertyDef = new PropertyDefinitionProxy();
		propertyDef.setName("description");
		propertyDef.setLabelId("description");
		propertyDef.setMultiValued(false);

		FieldDefinitionImpl property = new FieldDefinitionImpl();
		property.setId((long) 11);
		property.setLabelId("text");
		property.setName("text");
		property.setType("text");

		propertyDef.setDataType(getDataTypeDefinition());

		fields.add(propertyDef);

		Mockito.when(dictionaryService.getDataTypeDefinition("objectinstance")).thenReturn(
				propertyDef.getDataType());

		DocumentDefinitionImpl documentDefinition = new DocumentDefinitionImpl();
		documentDefinition.setFields(fields);

		Mockito.when(dictionaryService.getInstanceDefinition(instance)).thenReturn(
				documentDefinition);

		String result = controller.loadLiteralsForType("objectinstance",
				"emf:eb7d4242-ce10-4d73-b880-96ff6796fda0", "EO007005", true);
		JsonAssert
				.assertJsonEquals(
						"[{\"linkId\":\"description\",\"cls\":\"\",\"isMultiValued\":false,\"name\":\"description\",\"value\":\"\",\"oldValue\":\"\",\"editable\":true}]",
						result);
	}

	/**
	 * Load literals.
	 */
	@SuppressWarnings("boxing")
	public void loadLiteralsNullInstance() {
		fetchNullInstance = true;

		String result = controller.loadLiteralsForType(null,
				"emf:eb7d4242-ce10-4d73-b880-96ff6796fda0", "EO007005", true);
		assertEquals(result, null);
	}

	/**
	 * test case for new instance creation.
	 */
	public void testNewDocumentInstance() {

		fetchNullInstance = true;
		Instance instance = controller.loadInstanceInternal(ObjectInstance.class,
				"objectInstanceId");

		List<PropertyDefinition> fields = new LinkedList<>();
		PropertyDefinitionProxy propertyDef = new PropertyDefinitionProxy();
		propertyDef.setName("description");
		propertyDef.setLabelId("description");
		propertyDef.setMultiValued(false);

		FieldDefinitionImpl property = new FieldDefinitionImpl();
		property.setId((long) 11);
		property.setLabelId("text");
		property.setName("text");
		property.setType("text");

		propertyDef.setDataType(getDataTypeDefinition());

		fields.add(propertyDef);

		Mockito.when(dictionaryService.getDataTypeDefinition("objectinstance")).thenReturn(
				propertyDef.getDataType());

		DocumentDefinitionImpl documentDefinition = new DocumentDefinitionImpl();
		documentDefinition.setFields(fields);

		Mockito.when(dictionaryService.getInstanceDefinition(instance)).thenReturn(
				documentDefinition);

		Mockito.when(serviceRegister.getInstanceService(DocumentInstance.class)).thenReturn(
				instanceService);

		DocumentInstance documentInstance = new DocumentInstance();
		documentInstance.setIdentifier("EO007005");
		documentInstance.setStandalone(true);
		documentInstance.setRevision(0L);

		Mockito.when(dictionaryService.getInstanceDefinition(documentInstance)).thenReturn(
				documentDefinition);

		Mockito.when(instanceService.createInstance(documentDefinition, null, new Operation("")))
				.thenReturn(instance);

		String result = controller.loadLiteralsForType("objectinstance",
				"emf:eb7d4242-ce10-4d73-b880-96ff6796fda0", "EO007005", true);
		JsonAssert
				.assertJsonEquals(
						"[{\"linkId\":\"description\",\"cls\":\"\",\"isMultiValued\":false,\"name\":\"description\",\"value\":\"\",\"oldValue\":\"\",\"editable\":true}]",
						result);
	}
}