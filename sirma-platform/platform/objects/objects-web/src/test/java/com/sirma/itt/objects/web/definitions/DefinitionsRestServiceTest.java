package com.sirma.itt.objects.web.definitions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.library.LibraryProvider;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Tests the functionality in {@link DefinitionsRestService}.
 *
 * @author Mihail Radkov
 */
public class DefinitionsRestServiceTest {

	private static final String LANG = "en";
	private static final String LABEL = "label";

	private static final String TYPE_ID = "id";
	private static final String SHORT_URI = "short-uri";
	private static final String FULL_URI = "full-uri";

	@Mock
	private SemanticDefinitionService semanticDefinitionService;

	@Mock
	private CodelistService codelistService;

	@Mock
	private DictionaryService dictionaryService;

	@Mock
	private TypeMappingProvider typeProvider;

	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@Mock
	private AuthorityService authorityService;

	@Mock
	private UserPreferences userPreferences;

	@InjectMocks
	private DefinitionsRestService service;

	/**
	 * Initializes mocks before every test.
	 */
	@Before
	public void before() {
		service = new DefinitionsRestService();
		MockitoAnnotations.initMocks(this);
		Mockito.when(userPreferences.getLanguage()).thenReturn(LANG);
	}

	/**
	 * Tests when there are no searchable types from which to create types and sub types.
	 */
	@Test
	public void testGetTypesForNonSearchableType() {
		ClassInstance classInstance = new ClassInstance();
		classInstance.add("searchable", false);
		mockSemanticDefinition(classInstance);
		mockAuthorityService(Boolean.TRUE);

		String types = service.getTypes(null, false);
		JSONArray emptyArray = new JSONArray();
		Assert.assertEquals(emptyArray.toString(), types);
	}

	/**
	 * Tests when there are searchable types but from a forbidden library.
	 */
	@Test
	public void testGetTypesForForbiddenLibrary() {
		ClassInstance classInstance = getClassInstance(TYPE_ID, true, SHORT_URI, LABEL);
		classInstance.setLibrary("some-lib");

		mockSemanticDefinition(classInstance);
		mockNamespaceRegistry(SHORT_URI, FULL_URI);
		mockSemanticDefinitionServiceGetLibrary(classInstance);
		mockAuthorityService(Boolean.FALSE);

		String types = service.getTypes(null, false);
		JSONArray emptyArray = new JSONArray();
		Assert.assertEquals(emptyArray.toString(), types);
	}

	/**
	 * Tests the types building when there a type without sub types.
	 */
	@Test
	public void testGetTypesWithoutSubtypes() {
		ClassInstance classInstance = getClassInstance(TYPE_ID, true, SHORT_URI, LABEL);

		mockSemanticDefinition(classInstance);
		mockNamespaceRegistry(SHORT_URI, FULL_URI);
		mockAuthorityService(Boolean.TRUE);

		String types = service.getTypes(null, false);
		JSONObject expected = getTypeJson(SHORT_URI, LABEL);
		JSONArray array = new JSONArray();
		array.put(expected);

		Assert.assertEquals(array.toString(), types);
	}

	/**
	 * Tests when there are class filter but no types correspond to it.
	 */
	@Test
	public void testGetTypesWithClassFilterAndMissingType() {
		String types = service.getTypes(Arrays.asList("some-missing-id"), false);
		JSONArray emptyArray = new JSONArray();
		Assert.assertEquals(emptyArray.toString(), types);
	}

	/**
	 * Tests when a type is builded with a provided class filter.
	 */
	@Test
	public void testGetTypesWithClassFilter() {
		ClassInstance classInstance = getClassInstance(TYPE_ID, true, SHORT_URI, LABEL);

		mockSemanticDefinition(TYPE_ID, classInstance);
		mockNamespaceRegistry(SHORT_URI, FULL_URI);
		mockAuthorityService(Boolean.TRUE);

		String types = service.getTypes(Arrays.asList(TYPE_ID), false);
		JSONObject expected = getTypeJson(SHORT_URI, LABEL);
		JSONArray array = new JSONArray();
		array.put(expected);

		Assert.assertEquals(array.toString(), types);
	}

	/**
	 * Tests when a class has some sub classes.
	 */
	@Test
	public void testGetTypesWithSubtypes() {
		ClassInstance classInstance = getClassInstance(TYPE_ID, true, SHORT_URI, LABEL);
		ClassInstance subClass1 = getClassInstance("some-subclass1", true, "some-short-uri1", "SubClass");
		Map<String, ClassInstance> subClasses = CollectionUtils.createHashMap(1);
		subClasses.put("some-id1", subClass1);
		// Testing if there are no duplications
		subClasses.put("some-id2", subClass1);
		classInstance.setSubClasses(subClasses);

		mockSemanticDefinition(classInstance);
		mockNamespaceRegistry(SHORT_URI, FULL_URI);
		mockAuthorityService(Boolean.TRUE);

		String types = service.getTypes(Arrays.asList(TYPE_ID), false);
		JSONObject expected = getTypeJson(SHORT_URI, LABEL);
		JSONObject subClassJson = new JSONObject();
		JsonUtil.addToJson(subClassJson, DefaultProperties.URI, "some-short-uri1");
		JsonUtil.addToJson(subClassJson, DefaultProperties.TITLE, "SubClass");
		JsonUtil.addToJson(subClassJson, "definitionId", "123456");

		JsonUtil.getJsonArray(expected, "subTypes").put(subClassJson);
		JSONArray array = new JSONArray();
		array.put(expected);

		Assert.assertEquals(array.toString(), types);
	}

	/**
	 * Tests the code list sub types building when the provided class has a definition with type field.
	 */
	@Test
	public void testCodelistSubtypes() {
		ClassInstance classInstance = getClassInstance(TYPE_ID, true, SHORT_URI, LABEL);

		mockSemanticDefinition(classInstance);
		mockNamespaceRegistry(SHORT_URI, FULL_URI);
		mockAuthorityService(Boolean.TRUE);

		DataTypeDefinition typeDefinition = new DataTypeDefinitionMock(EmfInstance.class, FULL_URI);
		Mockito.when(dictionaryService.getDataTypeDefinition(Matchers.anyString())).thenReturn(typeDefinition);

		DefinitionMock model = new DefinitionMock();
		FieldDefinitionImpl property = new FieldDefinitionImpl();
		property.setIdentifier(DefaultProperties.TYPE);
		property.setCodelist(123);
		property.setParentPath("parent-path");
		model.setFields(Arrays.asList(property));
		model.setIdentifier("model");

		// TODO: Correctly mock the code value to assert uri & title
		CodeValue codeValue = new CodeValue();
		Mockito.when(codelistService.getCodeValue(Matchers.eq(123), Matchers.any())).thenReturn(codeValue);

		Mockito.when(dictionaryService.getAllDefinitions(Matchers.any(Class.class))).thenReturn(Arrays.asList(model));
		Mockito.when(typeProvider.getDataTypeName(Matchers.anyString())).thenReturn(EmfInstance.class.getSimpleName());

		String types = service.getTypes(Arrays.asList(TYPE_ID), false);
		JSONObject expected = getTypeJson(SHORT_URI, LABEL);
		JSONObject clSubTypeJson = new JSONObject();
		JsonUtil.getJsonArray(expected, "clSubTypes").put(clSubTypeJson);
		JSONArray array = new JSONArray();
		array.put(expected);

		Assert.assertEquals(array.toString(), types);
	}

	/**
	 * Generates JSON object based on the provided parameters.
	 *
	 * @param id
	 *            - the type ID
	 * @param uri
	 *            - the type URI
	 * @return - the generated JSON object
	 */
	private static JSONObject getTypeJson(String id, String label) {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, DefaultProperties.URI, id);
		JsonUtil.addToJson(object, DefaultProperties.TITLE, label);
		JsonUtil.addToJson(object, "subTypes", new JSONArray());
		JsonUtil.addToJson(object, "clSubTypes", new JSONArray());
		JsonUtil.addToJson(object, "definitionId", "123456");
		return object;
	}

	private static ClassInstance getClassInstance(String id, Boolean searchable, String uri, String label) {
		ClassInstance classInstance = new ClassInstance();
		classInstance.setId(id);
		classInstance.add("searchable", searchable);
		classInstance.add("instance", uri);
		classInstance.setLabel(LANG, label);
		classInstance.add("partOfObjectLibrary", Boolean.TRUE);
		classInstance.add("definitions", new HashSet<Serializable>());
		CollectionUtils.addNonNullValue(classInstance.get("definitions", Set.class), "123456");
		return classInstance;
	}

	private void mockSemanticDefinition(ClassInstance instance) {
		Mockito.when(semanticDefinitionService.getClassInstance(Matchers.anyString())).thenReturn(instance);
		Mockito.when(semanticDefinitionService.getRootClass()).thenReturn(instance);
	}

	private void mockSemanticDefinition(String uri, ClassInstance instance) {
		Mockito.when(semanticDefinitionService.getClassInstance(Matchers.eq(uri))).thenReturn(instance);
	}

	private void mockSemanticDefinitionServiceGetLibrary(ClassInstance instance) {
		List<ClassInstance> instances = new ArrayList<>(1);
		instances.add(instance);
		Mockito.when(semanticDefinitionService.getLibrary(LibraryProvider.OBJECT_LIBRARY)).thenReturn(instances);
	}

	private void mockNamespaceRegistry(String shortUri, String fullUri) {
		Mockito.when(namespaceRegistryService.buildFullUri(Matchers.eq(shortUri))).thenReturn(fullUri);
	}

	private void mockAuthorityService(Boolean isActionAllowed) {
		Mockito
				.when(authorityService.isActionAllowed(Matchers.any(), Matchers.eq(ActionTypeConstants.VIEW_DETAILS),
						Matchers.any()))
					.thenReturn(isActionAllowed);
	}

}
