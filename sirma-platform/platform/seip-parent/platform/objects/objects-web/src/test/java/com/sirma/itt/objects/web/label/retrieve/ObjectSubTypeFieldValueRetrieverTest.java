package com.sirma.itt.objects.web.label.retrieve;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.label.retrieve.FieldValueRetriever;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverParameters;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverTestHelper;
import com.sirma.itt.emf.label.retrieve.RetrieveResponse;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.testutil.fakes.InstanceTypeFake;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Testing the {@link ObjectSubTypeFieldValueRetriever}.
 *
 * @author nvelkov
 */
@SuppressWarnings("boxing")
public class ObjectSubTypeFieldValueRetrieverTest {

	@Mock
	private SemanticDefinitionService semanticDefinitionService;

	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@Mock
	private CodelistService codelistService;

	@Mock
	private DefinitionService definitionService;

	@Mock
	UserPreferences userPreferences;
	@Spy
	private ContextualMap<String, String> objectSubTypeCache = ContextualMap.create();

	@InjectMocks
	private ObjectSubTypeFieldValueRetriever objectSubtypeFieldValueRetriever = new ObjectSubTypeFieldValueRetriever();

	@Mock
	private InstanceTypes instanceTypes;

	/**
	 * Before method. Reinit the objectSubtypeFieldValueRetriever and the mocks.
	 */
	@BeforeMethod
	public void beforeMethod() {
		objectSubTypeCache.clear();
		objectSubtypeFieldValueRetriever = new ObjectSubTypeFieldValueRetriever();
		MockitoAnnotations.initMocks(this);
		when(namespaceRegistryService.buildFullUri(anyString())).then((e) -> e.getArgumentAt(0, String.class));
		mockAuthenticationService(objectSubtypeFieldValueRetriever);
		when(instanceTypes.from(any(Serializable.class))).then(
				a -> Optional.of(InstanceTypeFake.buildForClass(a.getArgumentAt(0, Serializable.class).toString())));
	}

	/**
	 * Test object subtype semantic label retrieval. In this case the object subtype is succesfully found in the
	 * semantic.
	 */
	@Test
	public void testObjectSubtypeSemanticLabelRetrieval() {
		ClassInstance classInstance = new ClassInstance();
		classInstance.setLabel("en", "mockedLabel");
		mockSemanticDefinitionservice(classInstance, null);
		String label = objectSubtypeFieldValueRetriever.getLabel("definition",
				FieldValueRetrieverTestHelper.createMultiValueMap(FieldValueRetrieverParameters.OBJECTTYPE, "parent"));
		Assert.assertEquals(label, "mockedLabel");
	}

	/**
	 * Test cached object subtype semantic label retrieval. In this case the object subtype is succesfully found in the
	 * semantic at first, then cached and found in the cache the second time.
	 */
	@Test
	public void testCachedObjectSubtypeSemanticLabelRetrieval() {
		ClassInstance classInstance = new ClassInstance();
		classInstance.setLabel("en", "mockedLabel");
		mockSemanticDefinitionservice(classInstance, null);
		objectSubtypeFieldValueRetriever.getLabel("definition",
				FieldValueRetrieverTestHelper.createMultiValueMap(FieldValueRetrieverParameters.OBJECTTYPE, "parent"));
		String label = objectSubtypeFieldValueRetriever.getLabel("definition",
				FieldValueRetrieverTestHelper.createMultiValueMap(FieldValueRetrieverParameters.OBJECTTYPE, "parent"));
		Assert.assertEquals(label, "mockedLabel");
	}

	/**
	 * Test relation subtype semantic label retrieval. In this case the relation subtype is succesfully found in the
	 * semantic.
	 */
	@Test
	public void testRelationSubtypeSemanticLabelRetrieval() {
		PropertyInstance propertyInstance = new PropertyInstance();
		Map<String, Serializable> properties = new HashMap<>();
		properties.put("title", "mockedTitle");
		propertyInstance.setProperties(properties);
		mockSemanticDefinitionservice(null, propertyInstance);
		String label = objectSubtypeFieldValueRetriever.getLabel("relationDefinition",
				FieldValueRetrieverTestHelper.createMultiValueMap(FieldValueRetrieverParameters.OBJECTTYPE, "parent"));
		Assert.assertEquals(label, "mockedTitle");
	}

	/**
	 * Test document subtype definition label retrieval.
	 */
	@Test
	public void testDocumentSubtypeDefinitionLabelRetrieval() {
		mockSemanticDefinitionservice(null, null);
		mockDefinitionService(EmfInstance.class, true, true, true);
		String label = objectSubtypeFieldValueRetriever.getLabel("definition",
				FieldValueRetrieverTestHelper.createMultiValueMap(FieldValueRetrieverParameters.OBJECTTYPE, "parent"));
		Assert.assertEquals(label, "labelFromCodeValue");
	}

	/**
	 * Test object subtype definition label retrieval.
	 */
	@Test
	public void testObjectSubtypeDefinitionLabelRetrieval() {
		mockSemanticDefinitionservice(null, null);
		mockDefinitionService(LinkInstance.class, true, true, true);
		String label = objectSubtypeFieldValueRetriever.getLabel("definition",
				FieldValueRetrieverTestHelper.createMultiValueMap(FieldValueRetrieverParameters.OBJECTTYPE, "parent"));
		Assert.assertEquals(label, "labelFromCodeValue");
	}

	/**
	 * Test subtype definition label retrieval when the type property from the model is not found.
	 */
	@Test
	public void testSubtypeDefinitionLabelRetrievalPropertyNotFound() {
		mockSemanticDefinitionservice(null, null);
		mockDefinitionService(LinkInstance.class, false, true, true);
		String label = objectSubtypeFieldValueRetriever.getLabel("definition",
				FieldValueRetrieverTestHelper.createMultiValueMap(FieldValueRetrieverParameters.OBJECTTYPE, "parent"));
		Assert.assertEquals(label, "definition");
	}

	/**
	 * Test subtype definition label retrieval when the codelist from the property is not found.
	 */
	@Test
	public void testSubtypeDefinitionLabelRetrievalCodelistNotFound() {
		mockSemanticDefinitionservice(null, null);
		mockDefinitionService(LinkInstance.class, true, false, true);
		String label = objectSubtypeFieldValueRetriever.getLabel("definition",
				FieldValueRetrieverTestHelper.createMultiValueMap(FieldValueRetrieverParameters.OBJECTTYPE, "parent"));
		Assert.assertEquals(label, "definition");
	}

	/**
	 * Test subtype definition label retrieval when the codevalue from the codelist is not found.
	 */
	@Test
	public void testSubtypeDefinitionLabelRetrievalCodevalueNotFound() {
		mockSemanticDefinitionservice(null, null);
		mockDefinitionService(LinkInstance.class, true, true, false);
		String label = objectSubtypeFieldValueRetriever.getLabel("definition",
				FieldValueRetrieverTestHelper.createMultiValueMap(FieldValueRetrieverParameters.OBJECTTYPE, "parent"));
		Assert.assertEquals(label, "definition");
	}

	/**
	 * Test subtype label not found. In this case the label is not found in the semantic or the definitions, so null is
	 * returned.
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
	 * Test object sub type values retriever with applied filter. Filter must work with starts with comparison and
	 * should be case insenstive.
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
	 * Test object sub type values retriever paging. Testing that offset and limit parameters are properly applied.
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
	 * Test object sub type values retriever with null values for parameters to ensure that method works with default
	 * values.
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
	public void mockSemanticDefinitionservice(ClassInstance classInstance, PropertyInstance propertyInstance) {

		if (classInstance != null) {
			Mockito.when(semanticDefinitionService.getClassInstance(Matchers.anyString())).thenReturn(classInstance);
		}
		if (propertyInstance != null) {
			Mockito.when(semanticDefinitionService.getRelation(Matchers.anyString())).thenReturn(propertyInstance);
		}
	}

	/**
	 * Mock the definition service.
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
	public void mockDefinitionService(Class clazz, boolean propertyFound, boolean codelistFound,
			boolean codevalueFound) {
		DataTypeDefinition dataType = Mockito.mock(DataTypeDefinition.class);
		GenericDefinition model = new GenericDefinitionImpl();
		model.setIdentifier("mockedPath");
		if (propertyFound) {
			FieldDefinitionImpl definition = new FieldDefinitionImpl();
			definition.setIdentifier("type");
			if (codelistFound) {
				definition.setCodelist(1);
			}
			List<PropertyDefinition> definitions = new ArrayList<>();
			definitions.add(definition);
			model.getFields().addAll(definitions);
		}
		if (codevalueFound) {
			CodeValue codevalue = new CodeValue();
			Map<String, Serializable> properties = new HashMap<>();
			properties.put("en", "labelFromCodeValue");
			codevalue.setProperties(properties);
			Mockito.when(codelistService.getCodeValue(Matchers.anyInt(), Matchers.anyString())).thenReturn(codevalue);
		}
		Mockito.when(dataType.getJavaClass()).thenReturn(clazz);
		Mockito.when(definitionService.getDataTypeDefinition(Matchers.anyString())).thenReturn(dataType);
		Mockito.when(definitionService.find(Matchers.anyString())).thenReturn(model);
	}

	/**
	 * Mock authentication service.
	 *
	 * @param retriever
	 *            the retriever where to inject the mock
	 */
	private void mockAuthenticationService(ObjectSubTypeFieldValueRetriever retriever) {
		Mockito.when(userPreferences.getLanguage()).thenReturn("en");
	}

	/**
	 * Mock object sub type field values retriever instance. When no filter and pagination is applied it should return
	 * 20 results.
	 *
	 * @return the instance
	 */
	// FIXME: this method should be refactored not to use reflection
	private FieldValueRetriever mockObjectSubTypeFieldValuesRetrieverInstance() {
		ObjectSubTypeFieldValueRetriever objectSubTypeFieldValueRetriever = new ObjectSubTypeFieldValueRetriever();

		SemanticDefinitionService semanticService = Mockito.mock(SemanticDefinitionService.class);
		List<ClassInstance> classes = new ArrayList<>();
		ClassInstance clazz = new ClassInstance();
		Map<String, Serializable> properties1 = new HashMap<>();
		properties1.put("instance", "emf:1");
		properties1.put("searchable", true);
		clazz.setProperties(properties1);

		Map<String, ClassInstance> subClasses = new LinkedHashMap<>();
		for (int i = 0; i < 20; i++) {
			ClassInstance subClazz = new ClassInstance();
			Map<String, Serializable> properties = new HashMap<>();
			properties.put("instance", "instance:" + i);
			properties.put("title", "Title: " + i);
			properties.put("searchable", true);
			subClazz.setProperties(properties);
			subClasses.put("instance:" + i, subClazz);
		}
		clazz.setSubClasses(subClasses);

		classes.add(clazz);

		NamespaceRegistryService namespaceService = Mockito.mock(NamespaceRegistryService.class);
		Mockito.when(namespaceService.buildFullUri(Matchers.anyString())).thenReturn("emf:1");
		ReflectionUtils.setFieldValue(objectSubTypeFieldValueRetriever, "namespaceRegistryService", namespaceService);

		DefinitionService localdefinitionService = Mockito.mock(DefinitionService.class);
		ReflectionUtils.setFieldValue(objectSubTypeFieldValueRetriever, "definitionService", localdefinitionService);

		DataTypeDefinition typeDefinition = Mockito.mock(DataTypeDefinition.class);
		Mockito.when(typeDefinition.getJavaClass()).thenAnswer(invocation -> ObjectInstance.class);
		Mockito.when(typeDefinition.getName()).thenReturn("objectinstance");

		Mockito.when(typeDefinition.getFirstUri()).thenReturn("emf:1");

		Mockito.when(localdefinitionService.getDataTypeDefinition(Matchers.anyString())).thenReturn(typeDefinition);

		ReflectionUtils.setFieldValue(objectSubTypeFieldValueRetriever, "userPreferences", userPreferences);

		CodelistService codelists = Mockito.mock(CodelistService.class);
		ReflectionUtils.setFieldValue(objectSubTypeFieldValueRetriever, "codelistService", codelists);

		List<DefinitionModel> models = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {
			GenericDefinitionImpl model = new GenericDefinitionImpl();
			model.setIdentifier("mockedPath" + i);
			model.setType("object");

			PropertyDefinitionMock property = new PropertyDefinitionMock();
			property.setCodelist(i);
			String value = "Default " + i;
			property.setValue(value);

			CodeValue codeValue = Mockito.mock(CodeValue.class);
			Mockito.when(codeValue.getValue()).thenReturn("CODEVALUE_" + i);
			Map<String, Serializable> properties = new HashMap<>();

			properties.put("en", "Definition Label " + i);

			Mockito.when(codeValue.getProperties()).thenReturn(properties);
			Mockito.when(codelists.getCodeValue(property.getCodelist(), property.getDefaultValue())).thenReturn(codeValue);
			model.getFields().add(property);

			property.setIdentifier("type");

			models.add(model);
		}

		Mockito.when(localdefinitionService.getAllDefinitions(any(InstanceType.class))).then(a -> models.stream());
		Mockito.when(semanticService.getClasses()).thenReturn(classes);
		Mockito.when(semanticService.getSearchableRelations()).thenReturn(new ArrayList<PropertyInstance>());
		ReflectionUtils.setFieldValue(objectSubTypeFieldValueRetriever, "semanticDefinitionService", semanticService);
		ReflectionUtils.setFieldValue(objectSubTypeFieldValueRetriever, "instanceTypes", instanceTypes);

		return objectSubTypeFieldValueRetriever;
	}

}
