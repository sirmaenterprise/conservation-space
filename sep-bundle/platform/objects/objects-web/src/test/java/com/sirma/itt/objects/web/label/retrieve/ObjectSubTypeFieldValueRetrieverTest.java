package com.sirma.itt.objects.web.label.retrieve;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.codelist.model.CodeValue;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.SemanticDefinitionService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.FieldDefinitionImpl;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.ClassInstance;
import com.sirma.itt.emf.instance.model.PropertyInstance;
import com.sirma.itt.emf.label.retrieve.FieldValueRetriever;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverServiceImpl;
import com.sirma.itt.emf.label.retrieve.RetrieveResponse;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.resources.ResourceProperties;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.objects.domain.definitions.impl.ObjectDefinitionImpl;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Testing the {@link ObjectSubTypeFieldValueRetriever}.
 * 
 * @author nvelkov
 */
public class ObjectSubTypeFieldValueRetrieverTest {

	/** The semantic definition service. */
	@Mock
	private SemanticDefinitionService semanticDefinitionService;

	/** The namespace registry service. */
	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	/** The codelist service. */
	@Mock
	private CodelistService codelistService;

	/** The dictionary service. */
	@Mock
	private DictionaryService dictionaryService;

	/** The authentication service. */
	@Mock
	private Instance<AuthenticationService> authenticationService;

	/** The object subtype field value retriever. */
	@InjectMocks
	private ObjectSubTypeFieldValueRetriever objectSubtypeFieldValueRetriever = new ObjectSubTypeFieldValueRetriever();

	private FieldValueRetrieverServiceImpl fieldValueRetrieverService;

	/**
	 * Before method. Reinit the objectSubtypeFieldValueRetriever and the mocks.
	 */
	@BeforeMethod
	public void beforeMethod() {
		objectSubtypeFieldValueRetriever = new ObjectSubTypeFieldValueRetriever();
		objectSubtypeFieldValueRetriever.init();
		MockitoAnnotations.initMocks(this);
		fieldValueRetrieverService = new FieldValueRetrieverServiceImpl();
	}

	/**
	 * Test object subtype semantic label retrieval. In this case the object subtype is succesfully
	 * found in the semantic.
	 */
	@Test
	public void testObjectSubtypeSemanticLabelRetrieval() {
		ClassInstance classInstance = new ClassInstance();
		classInstance.setLabel("en", "mockedLabel");
		mockSemanticDefinitionservice(classInstance, null);
		String label = objectSubtypeFieldValueRetriever.getLabel("definition", "parent");
		Assert.assertEquals(label, "mockedLabel");
	}

	/**
	 * Test cached object subtype semantic label retrieval. In this case the object subtype is
	 * succesfully found in the semantic at first, then cached and found in the cache the second
	 * time.
	 */
	@Test
	public void testCachedObjectSubtypeSemanticLabelRetrieval() {
		ClassInstance classInstance = new ClassInstance();
		classInstance.setLabel("en", "mockedLabel");
		mockSemanticDefinitionservice(classInstance, null);
		objectSubtypeFieldValueRetriever.getLabel("definition", "parent");
		String label = objectSubtypeFieldValueRetriever.getLabel("definition", "parent");
		Assert.assertEquals(label, "mockedLabel");
	}

	/**
	 * Test relation subtype semantic label retrieval. In this case the relation subtype is
	 * succesfully found in the semantic.
	 */
	@Test
	public void testRelationSubtypeSemanticLabelRetrieval() {
		PropertyInstance propertyInstance = new PropertyInstance();
		Map<String, Serializable> properties = new HashMap<>();
		properties.put("title", "mockedTitle");
		propertyInstance.setProperties(properties);
		mockSemanticDefinitionservice(null, propertyInstance);
		String label = objectSubtypeFieldValueRetriever.getLabel("relationDefinition", "parent");
		Assert.assertEquals(label, "mockedTitle");
	}

	/**
	 * Test document subtype definition label retrieval.
	 */
	@Test
	public void testDocumentSubtypeDefinitionLabelRetrieval() {
		mockSemanticDefinitionservice(null, null);
		mockAuthenticationService(this.objectSubtypeFieldValueRetriever);
		mockDictionaryservice(DocumentInstance.class, true, true, true);
		String label = objectSubtypeFieldValueRetriever.getLabel("definition", "parent");
		Assert.assertEquals(label, "labelFromCodeValue");
	}

	/**
	 * Test task subtype definition label retrieval.
	 */
	@Test
	public void testTaskSubtypeDefinitionLabelRetrieval() {
		mockSemanticDefinitionservice(null, null);
		mockAuthenticationService(this.objectSubtypeFieldValueRetriever);
		mockDictionaryservice(TaskInstance.class, true, true, true);
		String label = objectSubtypeFieldValueRetriever.getLabel("definition", "parent");
		Assert.assertEquals(label, "labelFromCodeValue");
	}

	/**
	 * Test object subtype definition label retrieval.
	 */
	@Test
	public void testObjectSubtypeDefinitionLabelRetrieval() {
		mockSemanticDefinitionservice(null, null);
		mockAuthenticationService(this.objectSubtypeFieldValueRetriever);
		mockDictionaryservice(LinkInstance.class, true, true, true);
		String label = objectSubtypeFieldValueRetriever.getLabel("definition", "parent");
		Assert.assertEquals(label, "labelFromCodeValue");
	}

	/**
	 * Test subtype definition label retrieval when the type property from the model is not found.
	 */
	@Test
	public void testSubtypeDefinitionLabelRetrievalPropertyNotFound() {
		mockSemanticDefinitionservice(null, null);
		mockAuthenticationService(this.objectSubtypeFieldValueRetriever);
		mockDictionaryservice(LinkInstance.class, false, true, true);
		String label = objectSubtypeFieldValueRetriever.getLabel("definition", "parent");
		Assert.assertEquals(label, "definition");
	}

	/**
	 * Test subtype definition label retrieval when the codelist from the property is not found.
	 */
	@Test
	public void testSubtypeDefinitionLabelRetrievalCodelistNotFound() {
		mockSemanticDefinitionservice(null, null);
		mockAuthenticationService(this.objectSubtypeFieldValueRetriever);
		mockDictionaryservice(LinkInstance.class, true, false, true);
		String label = objectSubtypeFieldValueRetriever.getLabel("definition", "parent");
		Assert.assertEquals(label, "definition");
	}

	/**
	 * Test subtype definition label retrieval when the codevalue from the codelist is not found.
	 */
	@Test
	public void testSubtypeDefinitionLabelRetrievalCodevalueNotFound() {
		mockSemanticDefinitionservice(null, null);
		mockAuthenticationService(this.objectSubtypeFieldValueRetriever);
		mockDictionaryservice(LinkInstance.class, true, true, false);
		String label = objectSubtypeFieldValueRetriever.getLabel("definition", "parent");
		Assert.assertEquals(label, "definition");
	}

	/**
	 * Test subtype label not found. In this case the label is not found in the semantic or the
	 * definitions, so null is returned.
	 */
	@Test
	public void testSubtypeLabelNotFound() {
		String label = objectSubtypeFieldValueRetriever.getLabel(null, null);
		Assert.assertEquals(label, null);
	}

	/**
	 * Test object sub type values retriever when no filter is applied.
	 */
	@Test
	public void testObjectSubTypeValuesRetrieverNoFilter() {
		FieldValueRetriever fieldValueRetriever = mockObjectSubTypeFieldValuesRetrieverInstance();
		RetrieveResponse values = fieldValueRetriever.getValues(null, 0, 10);
		Assert.assertEquals(values.getTotal(), new Long(30));
		Assert.assertEquals(values.getResults().size(), 10);
		Assert.assertEquals(values.getResults().get(0).getFirst(), "CODEVALUE_1");
		Assert.assertEquals(values.getResults().get(0).getSecond(), "Definition Label 1");
	}

	/**
	 * Test object sub type values retriever with applied filter. Filter must work with starts with
	 * comparison and should be case insenstive.
	 */
	@Test
	public void testObjectSubTypeValuesRetrieverWithFilter() {
		FieldValueRetriever fieldValueRetriever = mockObjectSubTypeFieldValuesRetrieverInstance();
		RetrieveResponse values = fieldValueRetriever.getValues("Title: 1", 0, 10);
		Assert.assertEquals(values.getTotal(), new Long(11));
		Assert.assertEquals(values.getResults().size(), 10);
		for (Pair<String, String> value : values.getResults()) {
			Assert.assertTrue(value.getSecond().toLowerCase().startsWith("title: 1"));
		}
	}

	/**
	 * Test object sub type values retriever paging. Testing that offset and limit parameters are
	 * properly applied.
	 */
	@Test
	public void testObjectSubTypeValuesRetrieverPaging() {
		FieldValueRetriever fieldValueRetriever = mockObjectSubTypeFieldValuesRetrieverInstance();
		RetrieveResponse values = fieldValueRetriever.getValues(null, 10, 5);
		Assert.assertEquals(values.getTotal(), new Long(30));
		Assert.assertEquals(values.getResults().size(), 5);
		Assert.assertEquals(values.getResults().get(0).getFirst(), "instance:0");
		Assert.assertEquals(values.getResults().get(0).getSecond(), "Title: 0");
	}

	/**
	 * Test object sub type values retriever with null values for parameters to ensure that method
	 * works with default values.
	 */
	@Test
	public void testObjectSubTypeValuesRetrieverNullParams() {
		FieldValueRetriever fieldValueRetriever = mockObjectSubTypeFieldValuesRetrieverInstance();
		RetrieveResponse values = fieldValueRetriever.getValues(null, null, null);
		Assert.assertEquals(values.getTotal(), new Long(30));
		Assert.assertEquals(values.getResults().size(), 30);
	}

	/**
	 * Mock the semantic definition service.
	 * 
	 * @param classInstance
	 *            the class instance
	 * @param propertyInstance
	 *            the property instance
	 */
	public void mockSemanticDefinitionservice(ClassInstance classInstance,
			PropertyInstance propertyInstance) {
		AuthenticationService authenticationService = Mockito.mock(AuthenticationService.class);
		Instance<AuthenticationService> authenticationServiceInstance = Mockito
				.mock(Instance.class);
		Mockito.when(authenticationServiceInstance.get()).thenReturn(authenticationService);
		ReflectionUtils.setField(objectSubtypeFieldValueRetriever, "authenticationService",
				authenticationServiceInstance);

		if (classInstance != null) {
			Mockito.when(semanticDefinitionService.getClassInstance(Mockito.anyString()))
					.thenReturn(classInstance);
		}
		if (propertyInstance != null) {
			Mockito.when(semanticDefinitionService.getRelation(Mockito.anyString())).thenReturn(
					propertyInstance);
		}
	}

	/**
	 * Mock the dictionary service.
	 * 
	 * @param clazz
	 *            the clazz
	 * @param propertyFound
	 *            indicates whether the type property of the definition model is found
	 * @param codelistFound
	 *            indicates whether a codelist for that property is found
	 * @param codevalueFound
	 *            indicates whether a codevalue from the codelist is found
	 */
	public void mockDictionaryservice(Class clazz, boolean propertyFound, boolean codelistFound,
			boolean codevalueFound) {
		DataTypeDefinition dataType = Mockito.mock(DataTypeDefinition.class);
		ObjectDefinitionImpl model = new ObjectDefinitionImpl();
		model.setIdentifier("mockedPath");
		if (propertyFound) {
			FieldDefinitionImpl definition = new FieldDefinitionImpl();
			definition.setIdentifier("type");
			if (codelistFound) {
				definition.setCodelist(1);
			}
			List<PropertyDefinition> definitions = new ArrayList<>();
			definitions.add(definition);
			model.setFields(definitions);
		}
		if (codevalueFound) {
			CodeValue codevalue = new CodeValue();
			Map<String, Serializable> properties = new HashMap<>();
			properties.put("en", "labelFromCodeValue");
			codevalue.setProperties(properties);
			Mockito.when(codelistService.getCodeValue(Mockito.anyInt(), Mockito.anyString()))
					.thenReturn(codevalue);
		}
		Mockito.when(dataType.getJavaClass()).thenReturn(clazz);
		Mockito.when(dictionaryService.getDataTypeDefinition(Mockito.anyString())).thenReturn(
				dataType);
		Mockito.when(dictionaryService.getDefinition(Mockito.any(Class.class), Mockito.anyString()))
				.thenReturn(model);

	}

	/**
	 * Mock authentication service.
	 *
	 * @param retriever the retriever where to inject the mock
	 */
	private void mockAuthenticationService(ObjectSubTypeFieldValueRetriever retriever) {
		AuthenticationService service = Mockito.mock(AuthenticationService.class);
		User user = new EmfUser();
		user.getProperties().put(ResourceProperties.LANGUAGE, "en");
		Mockito.when(service.getCurrentUser()).thenReturn(user);
		Mockito.when(authenticationService.get()).thenReturn(service);
		ReflectionUtils.setField(retriever, "authenticationService",
				authenticationService);
	}

	/**
	 * Mock object sub type field values retriever instance. When no filter and pagination is
	 * applied it should return 20 results.
	 * 
	 * @return the instance
	 */
	private FieldValueRetriever mockObjectSubTypeFieldValuesRetrieverInstance() {
		ObjectSubTypeFieldValueRetriever objectSubTypeFieldValueRetriever = new ObjectSubTypeFieldValueRetriever();

		SemanticDefinitionService semanticDefinitionService = Mockito
				.mock(SemanticDefinitionService.class);
		List<ClassInstance> classes = new ArrayList<>();
		ClassInstance clazz = new ClassInstance();
		Map<String, Serializable> properties1 = new HashMap<String, Serializable>();
		properties1.put("instance", "emf:1");
		properties1.put("searchable", true);
		clazz.setProperties(properties1);

		Map<String, ClassInstance> subClasses = new LinkedHashMap<>();
		for (int i = 0; i < 20; i++) {
			ClassInstance subClazz = new ClassInstance();
			Map<String, Serializable> properties = new HashMap<String, Serializable>();
			properties.put("instance", "instance:" + i);
			properties.put("title", "Title: " + i);
			properties.put("searchable", true);
			subClazz.setProperties(properties);
			subClasses.put("instance:" + i, subClazz);
		}
		clazz.setSubClasses(subClasses);

		classes.add(clazz);

		NamespaceRegistryService namespaceRegistryService = Mockito
				.mock(NamespaceRegistryService.class);
		Mockito.when(namespaceRegistryService.buildFullUri(Mockito.anyString()))
				.thenReturn("emf:1");
		ReflectionUtils.setField(objectSubTypeFieldValueRetriever, "namespaceRegistryService",
				namespaceRegistryService);

		DictionaryService dictionaryService = Mockito.mock(DictionaryService.class);
		ReflectionUtils.setField(objectSubTypeFieldValueRetriever, "dictionaryService",
				dictionaryService);

		DataTypeDefinition typeDefinition = Mockito.mock(DataTypeDefinition.class);
		Mockito.when(typeDefinition.getJavaClass()).thenAnswer(
				new Answer<Class<? extends CaseInstance>>() {
					@Override
					public Class<? extends CaseInstance> answer(InvocationOnMock invocation)
							throws Throwable {
						return CaseInstance.class;
					}
				});

		Mockito.when(typeDefinition.getFirstUri()).thenReturn("emf:1");

		ServiceRegister serviceRegister = Mockito.mock(ServiceRegister.class);

		InstanceService<com.sirma.itt.emf.instance.model.Instance, DefinitionModel> instanceService = Mockito
				.mock(InstanceService.class);
		Mockito.when(serviceRegister.getInstanceService(typeDefinition.getJavaClass())).thenReturn(
				instanceService);

		ReflectionUtils.setField(objectSubTypeFieldValueRetriever, "serviceRegister",
				serviceRegister);

		Mockito.when(dictionaryService.getDataTypeDefinition(Mockito.anyString())).thenReturn(
				typeDefinition);

		mockAuthenticationService(objectSubTypeFieldValueRetriever);

		CodelistService codelistService = Mockito.mock(CodelistService.class);
		ReflectionUtils.setField(objectSubTypeFieldValueRetriever, "codelistService",
				codelistService);

		List<DefinitionModel> models = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {
			ObjectDefinitionImpl model = Mockito.mock(ObjectDefinitionImpl.class);
			Mockito.when(model.getIdentifier()).thenReturn("mockedPath" + i);
			Mockito.when(model.getPath()).thenReturn("mockedPath" + i);

			List<PropertyDefinition> fields = new ArrayList<>();

			PropertyDefinition property = Mockito.mock(PropertyDefinition.class);
			Mockito.when(property.getCodelist()).thenReturn(i);
			Mockito.when(property.getDefaultValue()).thenReturn("Default " + i);

			CodeValue codeValue = Mockito.mock(CodeValue.class);
			Mockito.when(codeValue.getValue()).thenReturn("CODEVALUE_" + i);
			Map<String, Serializable> properties = new HashMap<>();

			properties.put("en", "Definition Label " + i);

			Mockito.when(codeValue.getProperties()).thenReturn(properties);
			Mockito.when(
					codelistService.getCodeValue(property.getCodelist(), property.getDefaultValue()))
					.thenReturn(codeValue);
			fields.add(property);

			Mockito.when(property.getIdentifier()).thenReturn("type");
			Mockito.when(model.getFields()).thenReturn(fields);

			models.add(model);
		}

		Mockito.when(
				dictionaryService.getAllDefinitions(instanceService.getInstanceDefinitionClass()))
				.thenReturn(models);
		Mockito.when(semanticDefinitionService.getClasses()).thenReturn(classes);
		Mockito.when(semanticDefinitionService.getRelations()).thenReturn(
				new ArrayList<PropertyInstance>());
		ReflectionUtils.setField(objectSubTypeFieldValueRetriever, "semanticDefinitionService",
				semanticDefinitionService);

		return objectSubTypeFieldValueRetriever;
	}

}
