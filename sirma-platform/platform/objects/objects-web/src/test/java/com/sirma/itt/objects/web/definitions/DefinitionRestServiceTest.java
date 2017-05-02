/**
 * Copyright (c) 2013 22.11.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.objects.web.definitions;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.mockito.AdditionalAnswers;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.richfaces.json.JSONArray;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.seip.definition.DefinitionHelper;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.WritablePropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.HeadersService;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.dao.ServiceRegistry;
import com.sirma.itt.seip.instance.properties.PropertiesConverter;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sun.jersey.api.client.ClientResponse.Status;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Tests for {@link DefinitionRestService}.
 *
 * @author cdimitrov
 */
public class DefinitionRestServiceTest extends EmfTest {

	private static final String DEFINITION_ID = "DEFINITION_ID";
	private static final String INSTANCE_TYPE = "INSTANCE_TYPE";
	private static final String INSTANCE_ID = "INSTANCE_ID";
	private static final String PARENT_TYPE = "PARENT_TYPE";
	private static final String PARENT_ID = "PARENT_ID";
	private static final String IDENTIFIER = "identifier";
	private static final String FIELDS = "fields";
	private static final String PROPERTIES = "properties";
	private static final String FIELD_KEY = "FIELD_KEY";
	private static final String FIELD_VALUE = "FIELD_VALUE";
	private DefinitionRestService service;
	private DictionaryService dictionaryService;
	private PropertiesConverter propertyConverter;
	private CodelistService codelistService;
	private InstanceService instanceService;
	private NamespaceRegistryService namespaceRegistryService;
	private ServiceRegistry serviceRegistry;
	private SemanticDefinitionService semanticDefinitionService;
	private HeadersService headersService;
	private DefinitionHelper definitionHelper;

	private JSONObject jsonDefinition;
	private DefinitionModel definitionModel;
	private Instance instance;
	private TypeMappingProvider typeProvider;

	/**
	 * Test initialization.
	 */
	@BeforeMethod
	public void init() {
		// generate test data
		generateData();
		service = new DefinitionRestService() {
			@Override
			protected DefinitionModel loadDefinition(String definitionId, String instanceType) {
				return definitionModel;
			}

			@Override
			protected JSONObject convertDefinitionToJson(DefinitionModel definition) {
				return jsonDefinition;
			}

			@Override
			public Instance fetchInstance(String instanceId, String instanceType) {
				return instance;
			}

			@Override
			protected Map<String, String> getExternalModel(Instance instance, DefinitionModel definition) {
				Map<String, String> externalModel = new HashMap<>();
				externalModel.put(FIELD_KEY, FIELD_VALUE);
				return externalModel;
			}

		};

		dictionaryService = Mockito.mock(DictionaryService.class);
		ReflectionUtils.setField(service, "dictionaryService", dictionaryService);
		definitionHelper = Mockito.mock(DefinitionHelper.class);
		ReflectionUtils.setField(service, "definitionHelper", definitionHelper);
		headersService = Mockito.mock(HeadersService.class);
		ReflectionUtils.setField(service, "headersService", headersService);
		propertyConverter = Mockito.mock(PropertiesConverter.class);
		ReflectionUtils.setField(service, "propertyConverter", propertyConverter);
		instanceService = Mockito.mock(InstanceService.class);
		ReflectionUtils.setField(service, "instanceService", instanceService);
		codelistService = Mockito.mock(CodelistService.class);
		ReflectionUtils.setField(service, "codelistService", codelistService);
		namespaceRegistryService = Mockito.mock(NamespaceRegistryService.class);
		ReflectionUtils.setField(service, "namespaceRegistryService", namespaceRegistryService);
		serviceRegistry = Mockito.mock(ServiceRegistry.class);
		ReflectionUtils.setField(service, "serviceRegistry", serviceRegistry);
		semanticDefinitionService = Mockito.mock(SemanticDefinitionService.class);
		ReflectionUtils.setField(service, "semanticDefinitionService", semanticDefinitionService);
		ReflectionUtils.setField(service, "typeConverter", createTypeConverter());
		ReflectionUtils.setField(service, "userPreferences", userPreferences);
		typeProvider = mock(TypeMappingProvider.class);
		when(typeProvider.getDataTypeName(any())).thenReturn("instance");
		ReflectionUtils.setField(service, "typeProvider", typeProvider);
	}

	/**
	 * Generate test data for the test scenarios.
	 */
	private void generateData() {
		jsonDefinition = new JSONObject();
		JsonUtil.append(jsonDefinition, IDENTIFIER, IDENTIFIER);
		JsonUtil.append(jsonDefinition, FIELDS, new JSONArray());
		JsonUtil.append(jsonDefinition, PROPERTIES, PROPERTIES);
		instance = getTestInstance();
		definitionModel = createDocumentDefinition(INSTANCE_ID, INSTANCE_TYPE);
	}

	/**
	 * Test for extracting fields(from regions also) from definitions.
	 */
	@Test
	public void displayDefinitionFieldsTest() {

		List<PropertyDefinition> fields = new ArrayList<>();

		// all fields are not supported
		Response response = service.retrieveFields(null, null, null, null, null);
		Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// all fields are not supported(with expected null-able string)
		response = service.retrieveFields("null", null, null, null, null);
		Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// trying to extract the fields with persisted instance and empty type
		response = service.retrieveFields(DEFINITION_ID, null, INSTANCE_ID, null, null);
		Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// trying to extract the fields with persisted instance with missing instance identifier
		response = service.retrieveFields(DEFINITION_ID, INSTANCE_TYPE, null, null, null);
		Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// trying to extract the fields based on new instance with missing current instance type
		response = service.retrieveFields(DEFINITION_ID, null, INSTANCE_ID, PARENT_TYPE, PARENT_ID);
		Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// trying to extract the fields based on new instance with missing definition identifier
		response = service.retrieveFields(null, INSTANCE_TYPE, null, PARENT_TYPE, PARENT_ID);
		Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// trying to extract the fields based on new instance with missing parent identifier
		response = service.retrieveFields(DEFINITION_ID, INSTANCE_TYPE, null, PARENT_TYPE, null);
		Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());

		// trying to extract the fields based on new instance with missing parent type
		response = service.retrieveFields(DEFINITION_ID, INSTANCE_TYPE, null, null, PARENT_TYPE);
		Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
		Mockito.when(dictionaryService.find(instance.getIdentifier())).thenReturn(definitionModel);
		// trying to extract the fields by already persisted instance
		response = service.retrieveFields(null, INSTANCE_TYPE, INSTANCE_ID, null, null);
		JsonAssert.assertJsonEquals(
				"{\"identifier\":[\"identifier\"],\"fields\":[\"[]\"],\"properties\":{\"cldescription\":{},\"FIELD_KEY\":\"FIELD_VALUE\"}}",
				response.getEntity().toString());

		Mockito.when(instanceService.createInstance(definitionModel, instance)).thenReturn(instance);
		Mockito.when(codelistService.getCodeValue(Matchers.anyInt(), Matchers.anyString())).thenReturn(null);
		// trying to extract the fields by newly created instance
		response = service.retrieveFields(DEFINITION_ID, INSTANCE_TYPE, null, PARENT_TYPE, PARENT_ID);
		JsonAssert.assertJsonEquals(
				"{\"identifier\":[\"identifier\"],\"fields\":[\"[]\"],\"properties\":{\"cldescription\":{},\"FIELD_KEY\":\"FIELD_VALUE\"}}",
				response.getEntity().toString());
	}

	@Test
	public void retrieveDefinitionFieldsTest() {
		final String projectURI = "emf:Project";

		CodeValue cv = new CodeValue();
		cv.setCodelist(300);
		cv.add(userPreferences.getLanguage(), "Case Objects Dev Definition Label");
		when(codelistService.getCodeValue(300, INSTANCE_TYPE)).thenReturn(cv);

		when(dictionaryService.find(INSTANCE_ID)).thenReturn(definitionModel);

		when(namespaceRegistryService.buildFullUri(Matchers.anyString()))
				.thenAnswer(AdditionalAnswers.returnsFirstArg());
		when(dictionaryService.getDataTypeDefinition(projectURI))
				.thenReturn(new DataTypeDefinitionMock(Instance.class, null));
		when(serviceRegistry.getInstanceService(Instance.class)).thenReturn(Mockito.mock(InstanceService.class));

		List<DefinitionModel> projectDefinitions = new ArrayList<>(2);
		DefinitionMock definition1 = new DefinitionMock();
		definition1.getFields().add(createField("field1", "value1"));
		definition1.getFields().add(createField("field2", "value2"));
		definition1.getFields().add(createField("rdf:type", projectURI));

		projectDefinitions.add(definition1);

		DefinitionMock definition2 = new DefinitionMock();
		definition2.getFields().add(createField("field1", "different value"));
		definition2.getFields().add(createField("field3", "value3"));
		definition2.getFields().add(createField("rdf:type", projectURI));
		projectDefinitions.add(definition2);

		when(dictionaryService.getAllDefinitions()).thenReturn(projectDefinitions.stream());
		when(dictionaryService.getAllDefinitions(Matchers.any(Class.class))).thenReturn(projectDefinitions);

		ClassInstance classInstance = new ClassInstance();
		classInstance.setIdentifier(projectURI);
		classInstance.setId(projectURI);
		classInstance.add("searchable", Boolean.TRUE);
		classInstance.setLabel(userPreferences.getLanguage(), "Project");
		classInstance.setLabel("en", "Project");
		when(semanticDefinitionService.getClassInstance(projectURI)).thenReturn(classInstance);
		when(definitionHelper.getDefinitionLabel(Matchers.any(DefinitionModel.class)))
				.thenReturn("Case Objects Dev Definition Label");

		String params = "{\"identifiers\":[\"" + projectURI + "\", \"" + INSTANCE_ID + "\"]}";
		Response response = service.retrieveDefinitionFields(params);

		Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());

		String expectedResult = "[{\"identifier\":\"emf:Project\",\"label\":\"Project\",\"fields\":[{\"defaultValue\":\"value1\",\"name\":\"field1\",\"isMandatory\":false,\"multivalue\":false},{\"defaultValue\":\"value2\",\"name\":\"field2\",\"isMandatory\":false,\"multivalue\":false},{\"defaultValue\":\"emf:Project\",\"name\":\"rdf:type\",\"isMandatory\":false,\"multivalue\":false},{\"defaultValue\":\"value3\",\"name\":\"field3\",\"isMandatory\":false,\"multivalue\":false}]},{\"identifier\":\"INSTANCE_ID\",\"label\":\"Case Objects Dev Definition Label\",\"fields\":[{\"displayType\":\"EDITABLE\",\"codelist\":200,\"name\":\"FIELD_KEY\",\"isMandatory\":true,\"multivalue\":false},{\"codelist\":300,\"defaultValue\":\"INSTANCE_TYPE\",\"name\":\"type\",\"isMandatory\":false,\"multivalue\":false}]}]";
		JsonAssert.assertJsonEquals(expectedResult, response.getEntity().toString());

		DefinitionRestService service2 = Mockito.mock(service.getClass(),
				Mockito.withSettings().spiedInstance(service).defaultAnswer(Mockito.CALLS_REAL_METHODS));
		Mockito
				.doReturn("[{name:'" + projectURI + "'}, {name:'" + INSTANCE_ID + "', subType: true}]")
					.when(service2)
					.getAllTypes(Matchers.anyBoolean(), Matchers.any(), Matchers.anyBoolean());
		Response response2 = service2.retrieveDefinitionFields("{}");
		String expectedResult2 = "[{\"identifier\":\"emf:Project\",\"label\":\"Project\",\"fields\":[{\"defaultValue\":\"value1\",\"name\":\"field1\",\"isMandatory\":false,\"multivalue\":false},{\"defaultValue\":\"value2\",\"name\":\"field2\",\"isMandatory\":false,\"multivalue\":false},{\"defaultValue\":\"emf:Project\",\"name\":\"rdf:type\",\"isMandatory\":false,\"multivalue\":false},{\"defaultValue\":\"value3\",\"name\":\"field3\",\"isMandatory\":false,\"multivalue\":false}]}]";
		JsonAssert.assertJsonEquals(expectedResult2, response2.getEntity().toString());
	}

	@Test
	public void retrieveDefinitionFieldsWrongParamsTest() {
		ClassInstance classInstance = new ClassInstance();
		classInstance.setLabel(userPreferences.getLanguage(), "Project");

		Response response = service.retrieveDefinitionFields("{\"identifiers\":[\"wrongDefinitionId\"]}");
		Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
		JsonAssert.assertJsonEquals("[]", response.getEntity().toString());
	}

	@Test
	public void addHeadersToModelsTest() {
		Mockito.when(headersService.generateInstanceHeader(instance, "default_header")).thenReturn(
				"<span data-property=\"default\">(Default)</span>");
		Mockito.when(headersService.generateInstanceHeader(instance, "compact_header")).thenReturn(
				"<span data-property=\"compact\">(Compact)</span>");
		Mockito.when(headersService.generateInstanceHeader(instance, "breadcrumb_header")).thenReturn(
				"<span data-property=\"breadcrumb\">(Breadcrumb)</span>");

		JSONObject models = new JSONObject();
		service.addHeadersToModels(instance, models);
		String expectedResult = "{\"headers\":{\"breadcrumb_header\":\"<span data-property=\\\"breadcrumb\\\">(Breadcrumb)<\\/span>\",\"compact_header\":\"<span data-property=\\\"compact\\\">(Compact)<\\/span>\",\"default_header\":\"<span data-property=\\\"default\\\">(Default)<\\/span>\"}}";
		Assert.assertEquals(expectedResult, models.toString());
	}

	private static WritablePropertyDefinition createField(String id, String defaultValue) {
		PropertyDefinitionProxy typeProperty = new PropertyDefinitionProxy();
		typeProperty.setIdentifier(id);
		typeProperty.setValue(defaultValue);
		return typeProperty.cloneProxy();
	}

	/**
	 * Create document model for the test scenarios.
	 *
	 * @param id
	 *            current identifier
	 * @param type
	 *            current type
	 * @return definition model
	 */
	@SuppressWarnings("unused")
	private DefinitionModel createDocumentDefinition(String id, String type) {
		DefinitionMock definition = new DefinitionMock();
		definition.setIdentifier(id);
		List<PropertyDefinition> fields = new ArrayList<>(1);
		PropertyDefinitionProxy field = new PropertyDefinitionProxy();
		field.setIdentifier(FIELD_KEY);
		field.setDisplayType(DisplayType.EDITABLE);
		field.setMandatory(Boolean.TRUE);
		field.setCodelist(200);
		fields.add(field.cloneProxy());

		PropertyDefinitionProxy typeField = new PropertyDefinitionProxy();
		typeField.setIdentifier(DefaultProperties.TYPE);
		typeField.setValue(type);
		typeField.setCodelist(300);
		fields.add(typeField.cloneProxy());
		definition.setFields(fields);
		return definition;
	}

	/**
	 * Create test instance with properties inside.
	 *
	 * @return test instance
	 */
	private Instance getTestInstance() {
		Instance instance = new EmfInstance();
		instance.setId(INSTANCE_ID);
		Map<String, Serializable> properties = new HashMap<>();
		properties.put(FIELD_KEY, FIELD_VALUE);
		instance.setProperties(properties);
		return instance;
	}

}
