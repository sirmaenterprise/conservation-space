package com.sirma.itt.emf.label.retrieve;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Instance;

import org.mockito.Matchers;
import org.mockito.Mockito;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.seip.collections.ContextualSet;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;

/**
 * Helper class for the {@link FieldValueRetrieverTest} tests. Mocks objects.
 *
 * @author nvelkov
 */
public class FieldValueRetrieverTestHelper {
	/**
	 * Mocks the Instance fieldValueRetriever by creating {@link UsernameFieldValueRetriever}, mocking it and putting it
	 * in the {@link InstanceProxyMock}. E.g. when called the {@link Instance#get()} method will return a mocked
	 * {@link UsernameFieldValueRetriever}.
	 * <p>
	 * Note: See {@link FieldValueRetrieverTest#mockUsernameRetriever(UsernameFieldValueRetriever)} for the actual
	 * mocking of the forementioned retriever.
	 * </p>
	 *
	 * @return the mocked instance, containing a {@link UsernameFieldValueRetriever}
	 * @param usernameFound
	 *            indicates wheter a label has been found by the retriever
	 */
	public Map<String, FieldValueRetriever> mockUsernameFieldValueRetrieverInstance(boolean usernameFound) {

		UsernameFieldValueRetriever usernameFieldValueRetriever = new UsernameFieldValueRetriever();
		mockUsernameRetriever(usernameFieldValueRetriever, usernameFound);
		Map<String, FieldValueRetriever> mockedExtensions = new HashMap<>();
		mockedExtensions.put(FieldId.USERNAME, usernameFieldValueRetriever);
		return mockedExtensions;
	}

	/**
	 * Mock username field values retriever instance. When no filter and pagination is applied it should return 20
	 * results.
	 *
	 * @return the mocked instance
	 */
	public Map<String, FieldValueRetriever> mockUsernameFieldValuesRetrieverInstance() {
		UsernameFieldValueRetriever usernameFieldValueRetriever = new UsernameFieldValueRetriever();
		ResourceService resourceService = Mockito.mock(ResourceService.class);
		List<Resource> resources = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			Resource anUser = Mockito.mock(Resource.class);
			Mockito.when(anUser.getName()).thenReturn("" + i);
			Mockito.when(anUser.getDisplayName()).thenReturn("User " + i);
			resources.add(anUser);

			Resource anotherUser = Mockito.mock(Resource.class);
			Mockito.when(anotherUser.getName()).thenReturn("" + i);
			Mockito.when(anotherUser.getDisplayName()).thenReturn("Name " + i);
			resources.add(anotherUser);
		}

		Mockito.when(resourceService.getAllResources(ResourceType.USER, null)).thenReturn(resources);
		ReflectionUtils.setField(usernameFieldValueRetriever, "resourceService", resourceService);
		Map<String, FieldValueRetriever> mockedExtensions = new HashMap<>();
		mockedExtensions.put(FieldId.USERNAME, usernameFieldValueRetriever);
		return mockedExtensions;
	}

	/**
	 * Mocks the Instance fieldValueRetriever by creating {@link ObjectTypeFieldValueRetriever}, mocking it and putting
	 * it in the {@link InstanceProxyMock}. E.g. when called the {@link Instance#get()} method will return a mocked
	 * {@link ObjectTypeFieldValueRetriever}.
	 * <p>
	 * Note: See {@link FieldValueRetrieverTestHelp#mockObjectTypeRetriever(ObjectTypeFieldValueRetriever)} for the
	 * actual mocking of the forementioned retriever.
	 * </p>
	 *
	 * @return the mocked instance, containing a {@link ObjectTypeFieldValueRetriever}
	 * @param labelFound
	 *            indicates wheter a label has been found by the retriever
	 */
	public Map<String, FieldValueRetriever> mockObjectTypeFieldValueRetrieverInstance(boolean labelFound) {
		ObjectTypeFieldValueRetriever objectTypeFieldValueRetriever = new ObjectTypeFieldValueRetriever();
		mockObjectTypeRetriever(objectTypeFieldValueRetriever, labelFound);
		Map<String, FieldValueRetriever> mockedExtensions = new HashMap<>();
		mockedExtensions.put(FieldId.OBJECT_TYPE, objectTypeFieldValueRetriever);
		return mockedExtensions;
	}

	/**
	 * Mocks the Instance fieldValueRetriever by creating {@link ActionFieldValueRetriever}, mocking it and putting it
	 * in the {@link InstanceProxyMock}. E.g. when called the {@link Instance#get()} method will return a mocked
	 * {@link ActionFieldValueRetriever}.
	 * <p>
	 * Note: See {@link FieldValueRetrieverTestHelp#mockActionRetriever(ActionFieldValueRetriever)} for the actual
	 * mocking of the forementioned retriever.
	 * </p>
	 *
	 * @return the mocked instance, containing a {@link ActionFieldValueRetriever}
	 * @param labelFound
	 *            indicates wheter a label has been found by the retriever
	 */
	public Map<String, FieldValueRetriever> mockActionFieldValueRetrieverInstance(boolean labelFound) {
		ActionFieldValueRetriever actionFieldValueRetriever = new ActionFieldValueRetriever();
		mockActionRetriever(actionFieldValueRetriever, labelFound);
		Map<String, FieldValueRetriever> mockedExtensions = new HashMap<>();
		mockedExtensions.put(FieldId.ACTION_ID, actionFieldValueRetriever);
		return mockedExtensions;
	}

	/**
	 * Mocks the Instance fieldValueRetriever by creating {@link ObjectStateFieldValueRetriever}, mocking it and putting
	 * it in the {@link InstanceProxyMock}. E.g. when called the {@link Instance#get()} method will return a mocked
	 * {@link ObjectStateFieldValueRetriever}.
	 * <p>
	 * Note: See
	 * {@link FieldValueRetrieverTestHelp#mockObjectStateFieldValueRetrieverInstance(String, boolean, boolean)} for the
	 * actual mocking of the forementioned retriever.
	 * </p>
	 *
	 * @param instance
	 *            the instance
	 * @param labelFound
	 *            indicates wheter a label has been found by the retriever
	 * @param stateFound
	 *            the state found
	 * @return the mocked instance, containing a {@link ActionFieldValueRetriever}
	 */
	public Map<String, FieldValueRetriever> mockObjectStateFieldValueRetrieverInstance(String instance,
			boolean labelFound, boolean stateFound) {
		ObjectStateFieldValueRetriever objectStateFieldRetriever = new ObjectStateFieldValueRetriever();
		mockObjectStateRetriever(objectStateFieldRetriever, instance, labelFound, stateFound);
		Map<String, FieldValueRetriever> mockedExtensions = new HashMap<>();
		mockedExtensions.put(FieldId.OBJECT_STATE, objectStateFieldRetriever);
		return mockedExtensions;
	}

	/**
	 * Mock object type field values retriever instance. When no filter and pagination is applied it should return 20
	 * results.
	 *
	 * @return the instance
	 */
	public Map<String, FieldValueRetriever> mockObjectTypeFieldValuesRetrieverInstance() {
		ObjectTypeFieldValueRetriever objectTypeFieldValueRetriever = new ObjectTypeFieldValueRetriever();
		SemanticDefinitionService semanticDefinitionService = Mockito.mock(SemanticDefinitionService.class);
		List<ClassInstance> classes = new ArrayList<>(20);
		for (int i = 0; i < 20; i++) {
			ClassInstance clazz = new ClassInstance();
			clazz.setId(i);
			Map<String, Serializable> properties = new HashMap<String, Serializable>();
			properties.put("searchable", true);
			properties.put("title", "Object type " + i);
			properties.put("instance", "instance:" + i);
			clazz.setProperties(properties);
			classes.add(clazz);
		}
		Mockito.when(semanticDefinitionService.getClasses()).thenReturn(classes);
		InstanceProxyMock<SemanticDefinitionService> semanticDefinitionServiceInstance = new InstanceProxyMock<SemanticDefinitionService>(
				semanticDefinitionService);
		ReflectionUtils.setField(objectTypeFieldValueRetriever, "semanticDefinitionService",
				semanticDefinitionServiceInstance);
		Map<String, FieldValueRetriever> mockedExtensions = new HashMap<>();
		mockedExtensions.put(FieldId.OBJECT_TYPE, objectTypeFieldValueRetriever);
		return mockedExtensions;
	}

	/**
	 * Mocks the {@link ResourceService#getResource(String, ResourceType)} in the retriever, to return a {@link User}
	 * with a pre-set display name.
	 *
	 * @param retriever
	 *            the retriever
	 * @param labelFound
	 *            indicates wheter a label has been found by the retriever
	 */
	public void mockUsernameRetriever(UsernameFieldValueRetriever retriever, boolean labelFound) {
		// Mocking the resource service
		ResourceService service = Mockito.mock(ResourceService.class);
		User userResource = Mockito.mock(User.class);
		Mockito.when(userResource.getDisplayName()).thenReturn("mockedUser");
		if (labelFound) {
			Mockito.when(service.findResource(Matchers.anyString())).thenReturn(userResource);
		}
		ReflectionUtils.setField(retriever, "resourceService", service);
	}

	/**
	 * Mocks the semantic and authentication service instances in the retriever. Mocks the
	 * {@link SemanticDefinitionService#getClassInstance(String)}.
	 *
	 * @param retriever
	 *            the retriever
	 * @param labelFound
	 *            indicates wheter a label has been found by the retriever
	 */
	public void mockObjectTypeRetriever(ObjectTypeFieldValueRetriever retriever, boolean labelFound) {
		// Mocking the semantic definition service
		SemanticDefinitionService semanticDefinitionService = Mockito.mock(SemanticDefinitionService.class);
		ClassInstance model = new ClassInstance();
		model.setLabel("en", "mockedLabel");
		if (labelFound) {
			Mockito.when(semanticDefinitionService.getClassInstance(Matchers.anyString())).thenReturn(model);
		}
		InstanceProxyMock<SemanticDefinitionService> semanticDefinitionServiceInstance = new InstanceProxyMock<SemanticDefinitionService>(
				semanticDefinitionService);

		// Mocking the authentication service
		UserPreferences userPreferences = mockAuthenticationService();

		// Putting it together
		ReflectionUtils.setField(retriever, "semanticDefinitionService", semanticDefinitionServiceInstance);
		ReflectionUtils.setField(retriever, "userPreferences", userPreferences);
	}

	/**
	 * Mock the action retriever. Mocks the {@link CodelistService} and the {@link UserPreferences} in the
	 * {@link ActionFieldValueRetriever}.
	 *
	 * @param retriever
	 *            the retriever
	 * @param labelFound
	 *            the label found
	 */
	public void mockActionRetriever(ActionFieldValueRetriever retriever, boolean labelFound) {
		UserPreferences userPreferences = mockAuthenticationService();
		mockCodelistService(retriever, labelFound);
		ReflectionUtils.setField(retriever, "userPreferences", userPreferences);

	}

	/**
	 * Mock object state retriever.
	 *
	 * @param retriever
	 *            the retriever
	 * @param instance
	 *            the instance
	 * @param labelFound
	 *            indicates whether a codelist has been found
	 * @param stateFound
	 *            indicates whether a codevalue has been found
	 */
	public void mockObjectStateRetriever(ObjectStateFieldValueRetriever retriever, String instance, boolean labelFound,
			boolean stateFound) {
		UserPreferences userPreferences = mockAuthenticationService();
		mockCodelistService(retriever, labelFound);
		mockDictionaryservice(retriever, instance);
		mockStateService(retriever, stateFound);
		ReflectionUtils.setField(retriever, "userPreferences", userPreferences);
	}

	/**
	 * Mock codelist service.
	 *
	 * @param retriever
	 *            the retriever
	 * @param labelFound
	 *            indicates whether the codelist has been found
	 */
	public void mockCodelistService(FieldValueRetriever retriever, boolean labelFound) {
		CodelistService codelistService = Mockito.mock(CodelistService.class);
		if (labelFound) {
			CodeValue codevalue = new CodeValue();
			codevalue.setValue("something");
			Map<String, Serializable> properties = new HashMap<>();
			properties.put("en", "mockedLabel");
			codevalue.setProperties(properties);
			Mockito.when(codelistService.getCodeValue(Matchers.anyInt(), Matchers.anyString())).thenReturn(codevalue);
			Map<String, CodeValue> codeValues = new HashMap<>();
			codeValues.put("key", codevalue);
			Mockito.when(codelistService.getCodeValues(Matchers.anyInt())).thenReturn(codeValues);
		}
		ReflectionUtils.setField(retriever, "codelistService", codelistService);
	}

	/**
	 * Mock state service.
	 *
	 * @param retriever
	 *            the retriever
	 * @param stateFound
	 *            indicates whether the state has been found
	 */
	public void mockStateService(FieldValueRetriever retriever, boolean stateFound) {
		StateService stateService = Mockito.mock(StateService.class);
		if (stateFound) {
			Mockito.when(stateService.getPrimaryStateCodelist(Matchers.any(Class.class))).thenReturn(1);
		}
		ReflectionUtils.setField(retriever, "stateService", stateService);
	}

	/**
	 * Mock the dictionary service.
	 *
	 * @param retriever
	 *            the retriever
	 * @param instance
	 *            the instance
	 */
	public void mockDictionaryservice(FieldValueRetriever retriever, String instance) {
		DictionaryService dictionaryService = Mockito.mock(DictionaryService.class);
		DataTypeDefinition dataType = Mockito.mock(DataTypeDefinition.class);
		Mockito.when(dataType.getJavaClassName()).thenReturn(instance);
		Mockito.when(dictionaryService.getDataTypeDefinition(Matchers.anyString())).thenReturn(dataType);
		ReflectionUtils.setField(retriever, "dictionaryService", dictionaryService);

	}

	/**
	 * Mock authentication service.
	 *
	 * @return the mocked instance
	 */
	public UserPreferences mockAuthenticationService() {
		UserPreferences userPreferences = mock(UserPreferences.class);
		when(userPreferences.getLanguage()).thenReturn("en");
		return userPreferences;
	}

	/**
	 * Mock action field values retriever instance. When no filter and pagination is applied it should return 20
	 * results.
	 *
	 * @return the instance
	 */
	public Map<String, FieldValueRetriever> mockActionFieldValuesRetrieverInstance() {
		ActionFieldValueRetriever actionFieldValueRestriever = new ActionFieldValueRetriever();

		UserPreferences authenticationService = mockAuthenticationService();
		ReflectionUtils.setField(actionFieldValueRestriever, "userPreferences", authenticationService);

		CodelistService codelistService = Mockito.mock(CodelistService.class);

		Map<String, CodeValue> codeValues = new LinkedHashMap<>();
		for (int i = 0; i < 20; i++) {
			CodeValue codeValue = new CodeValue();
			Map<String, Serializable> properties = new HashMap<>();
			properties.put("en", "Label " + i);
			codeValue.setProperties(properties);
			codeValue.setValue("cv" + i);
			codeValues.put("cv" + i, codeValue);
		}

		Mockito.when(codelistService.getCodeValues(Matchers.anyInt(), Matchers.anyBoolean())).thenReturn(codeValues);
		ReflectionUtils.setField(actionFieldValueRestriever, "codelistService", codelistService);
		Map<String, FieldValueRetriever> mockedExtensions = new HashMap<>();
		mockedExtensions.put(FieldId.ACTION_ID, actionFieldValueRestriever);
		return mockedExtensions;
	}

	/**
	 * Mock object state values retriever instance. When no filter and pagination is applied it should return 20
	 * results.
	 *
	 * @return the instance
	 */
	public Map<String, FieldValueRetriever> mockObjectStateFieldValuesRetrieverInstance() {
		ObjectStateFieldValueRetriever objectStateFieldValueRestriever = new ObjectStateFieldValueRetriever();

		UserPreferences authenticationService = mockAuthenticationService();
		ReflectionUtils.setField(objectStateFieldValueRestriever, "userPreferences", authenticationService);

		CodelistService codelistService = Mockito.mock(CodelistService.class);

		Set<Integer> stateCodeLists = ContextualSet.create();

		for (int i = 0; i < 3; i++) {
			stateCodeLists.add(i);

			Map<String, CodeValue> codeValues = new LinkedHashMap<>();
			for (int j = 0; j < 5; j++) {
				CodeValue codeValue = new CodeValue();
				Map<String, Serializable> properties = new HashMap<>();
				properties.put("en", "Label " + j + (j % 2 == 0 ? "" : i));
				codeValue.setProperties(properties);
				codeValue.setValue("cv" + j + (j % 2 == 0 ? "" : i));
				codeValues.put("cv" + j, codeValue);
			}

			Mockito.when(codelistService.getCodeValues(Matchers.eq(i))).thenReturn(codeValues);
		}
		ReflectionUtils.setField(objectStateFieldValueRestriever, "stateCodeLists", stateCodeLists);
		ReflectionUtils.setField(objectStateFieldValueRestriever, "codelistService", codelistService);
		Map<String, FieldValueRetriever> mockedExtensions = new HashMap<>();
		mockedExtensions.put(FieldId.OBJECT_STATE, objectStateFieldValueRestriever);
		return mockedExtensions;
	}

	/**
	 * Creates a multi value map and adds a single record.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the map
	 */
	public static SearchRequest createMultiValueMap(String key, String value) {
		Map<String, List<String>> multiValudMap = new HashMap<>();
		List<String> list = new ArrayList<>();
		list.add(value);
		multiValudMap.put(key, list);
		return new SearchRequest(multiValudMap);
	}

	/**
	 * Adds a record to multi value map.
	 *
	 * @param map
	 *            the map
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public static void addToMultiValueMap(Map<String, List<String>> map, String key, String value) {
		List<String> list = map.get(key);
		if (list == null) {
			list = new ArrayList<String>();
		}
		list.add(value);
		map.put(key, list);
	}
}
