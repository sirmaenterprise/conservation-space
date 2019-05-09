package com.sirma.itt.emf.solr.services.properties;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.solr.common.util.SimpleOrderedMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.emf.solr.connector.SolrConfigurationProperties;
import com.sirma.itt.emf.solr.exception.SolrClientException;
import com.sirma.itt.emf.solr.services.SolrConnector;
import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.context.ContextualReference;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.AllowedChildDefinition;
import com.sirma.itt.seip.definition.AllowedChildrenModel;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.model.BaseDefinition;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.search.SearchableProperty;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.search.SearchablePropertiesService;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.testutil.fakes.InstanceTypeFake;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * The Class SearchablePropertiesServiceTest.
 *
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchablePropertiesServiceTest {

	@Mock
	private SolrConnector solrConnector;

	@Mock
	private SemanticDefinitionService semanticDefinitionService;

	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private UserPreferences userPreferences;

	@Mock
	private LabelProvider labelProvider;

	@Spy
	private ContextualMap<String, List<SearchableProperty>> searchablePropertiesCache = ContextualMap.create();

	@Spy
	private ContextualMap<String, String> semanticDefinitionMapping = ContextualMap.create();

	@Spy
	private ContextualMap<String, String> rangeClassMapping = ContextualMap.create();

	@Spy
	private Contextual<SimpleOrderedMap<Object>> solrSchema = ContextualReference.create();

	@Mock
	private SolrConfigurationProperties solrConfigurationProperties;

	@InjectMocks
	private SearchablePropertiesServiceImpl searchablePropertiesService = new SearchablePropertiesServiceImpl();

	/**
	 * Initialize the mocks.
	 */
	@Before
	public void init() {
		mockLabelProvider();
		Mockito.when(userPreferences.getLanguage()).thenReturn("en");
		searchablePropertiesService.init();
		searchablePropertiesCache.clear();
	}

	/**
	 * Test the reset method of the {@link SearchablePropertiesService} with missing rdf type, covering all if
	 * statements.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testresetMissingRdfType() throws SolrClientException {
		mockSolrConnector("tokenizedField", "fieldName", "fieldType");
		mockSemanticDefinitionService("hasChild", "testRangeClass", "testClassId");
		mockNamespaceRegistryService("hasChild",
				"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#hasChild");
		mockDefinitionService("definitionId", "label", DisplayType.EDITABLE, "emf:testShortUri", "name", 3, false);

		searchablePropertiesService.reset();
		Map<String, String> definitionMapping = (Map<String, String>) ReflectionUtils
				.getFieldValue(searchablePropertiesService, "semanticDefinitionMapping");
		Assert.assertTrue(definitionMapping.isEmpty());

	}

	/**
	 * Test the searchable semantic properties. Performs a reset first, then looks in the searchableProperties map for
	 * the loaded property.
	 */
	@Test
	public void testGetSearchableSemanticPropertiesForDefinition() throws SolrClientException {
		mockSolrConnector("tokenizedField", "name", "fieldType");
		mockSemanticDefinitionService("testPropertyId", "testRangeClass", "testClassId");
		mockNamespaceRegistryService("emf:testShortUri", "testFullUri");
		mockDefinitionService("definitionId", "label", DisplayType.EDITABLE, "emf:testShortUri", "name", 3, true);

		when(semanticDefinitionService.getClassInstance("semanticid")).thenReturn(new ClassInstance());
		when(semanticDefinitionService.getClassInstance("definitionId")).thenReturn(new ClassInstance());


		searchablePropertiesService.reset();
		List<SearchableProperty> searchableProperties = searchablePropertiesService
				.getSearchableSemanticProperties("definitionId");
		Assert.assertEquals(3, searchableProperties.get(0).getCodelists().iterator().next().intValue());
		Assert.assertEquals("label", searchableProperties.get(0).getText());
		Assert.assertEquals("fieldType", searchableProperties.get(0).getSolrType());
	}

	/**
	 * Test the searchable semantic properties. Performs a reset first, then looks in the searchableProperties map for
	 * the loaded property.
	 */
	@Test
	public void testGetSearchableSemanticPropertiesForSemanticClass() throws SolrClientException {
		mockSolrConnector("tokenizedField", "name", "fieldType");
		mockSemanticDefinitionService("testPropertyId", "testRangeClass", "testClassId");
		mockNamespaceRegistryService("emf:testShortUri", "testFullUri");
		mockDefinitionService("definitionId", "label", DisplayType.EDITABLE, "emf:testShortUri", "name", 3, true);

		when(semanticDefinitionService.getClassInstance("semanticid")).thenReturn(new ClassInstance());
		when(semanticDefinitionService.getClassInstance("definitionId")).thenReturn(new ClassInstance());


		searchablePropertiesService.reset();
		List<SearchableProperty> searchableProperties = searchablePropertiesService
				.getSearchableSemanticProperties("semanticid_definitionId");
		org.testng.Assert.assertEquals(3, searchableProperties.get(0).getCodelists().iterator().next().intValue());
		Assert.assertEquals("label", searchableProperties.get(0).getText());
		Assert.assertEquals("fieldType", searchableProperties.get(0).getSolrType());
	}

	/**
	 * Test the searchable solr properties when everything is in tact, meaning that there is a valid field in the
	 * solrType mapping. Both the names of the solr field and the definition field are 'name'.
	 */
	@Test
	public void testGetSearchableSinglevaluedSolrProperties() throws SolrClientException {
		mockSolrConnector("tokenizedField", "hasChild", "fieldType");
		mockSemanticDefinitionService("testPropertyId", "testRangeClass", "testClassId");
		mockNamespaceRegistryService("emf:hasChild",
				"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#hasChild");
		mockDefinitionService("definitionId", "label", DisplayType.EDITABLE, "emf:hasChild", "name", 3, true);

		searchablePropertiesService.reset();
		List<SearchableProperty> searchableProperties = searchablePropertiesService
				.getSearchableSolrProperties("definitionId", false, false, false);
		Assert.assertEquals(3, searchableProperties.get(0).getCodelists().iterator().next().intValue());
		Assert.assertEquals("label", searchableProperties.get(0).getText());
		Assert.assertEquals("fieldType", searchableProperties.get(0).getSolrType());
	}

	/**
	 * Test the searchable semantic properties. Performs a reset first, then looks in the searchableProperties map for
	 * the loaded property. This will perform a search for multi valued properties.
	 */
	@Test
	public void testGetSearchableMultivaluedSolrProperties() throws SolrClientException {
		mockSolrConnector("tokenizedField", "hasChild", "fieldType");
		mockSemanticDefinitionService("testPropertyId", "testRangeClass", "testClassId");
		mockNamespaceRegistryService("emf:hasChild",
				"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#hasChild");
		mockDefinitionService("definitionId", "label", DisplayType.EDITABLE, "emf:hasChild", "name", 3, true);

		searchablePropertiesService.reset();
		List<SearchableProperty> searchableProperties = searchablePropertiesService
				.getSearchableSolrProperties("definitionId", false, true, false);
		Assert.assertEquals(3, searchableProperties.get(0).getCodelists().iterator().next().intValue());
		Assert.assertEquals("label", searchableProperties.get(0).getText());
		Assert.assertEquals("fieldType", searchableProperties.get(0).getSolrType());
	}

	/**
	 * Test the searchable solr properties when the solrType is missing, meaning that there is a valid field in the
	 * definition, however there is no such field in solr, so the field should not be returned.
	 */
	@Test
	public void testGetSearchableSolrPropertiesNoSolrType() throws SolrClientException {
		mockSolrConnector("tokenizedField", "name", "fieldType");
		mockSemanticDefinitionService("testPropertyId", "testRangeClass", "testClassId");
		mockNamespaceRegistryService("emf:testShortUri", "testFullUri");
		mockDefinitionService("definitionId", "label", DisplayType.EDITABLE, "emf:testShortUri", "anotherName", 3,
				true);

		searchablePropertiesService.reset();
		List<SearchableProperty> searchableProperties = searchablePropertiesService
				.getSearchableSolrProperties("definitionId", false, false, false);
		Assert.assertTrue(searchableProperties.isEmpty());

	}

	/**
	 * Test the retrieval of the searchable solr properties when no forType is specified.
	 */
	@Test
	public void testGetSearchableSolrPropertiesNoForType() throws SolrClientException {
		mockSolrConnector("tokenizedField", "hasChild", "fieldType");
		mockSemanticDefinitionService("testPropertyId", "testRangeClass", "testClassId");
		mockNamespaceRegistryService("emf:hasChild",
				"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#hasChild");
		mockDefinitionService("definitionId", "label", DisplayType.EDITABLE, "emf:hasChild", "name", 3, true);

		searchablePropertiesService.reset();
		List<SearchableProperty> searchableProperties = searchablePropertiesService.getSearchableSolrProperties("",
				false, false, false);
		Assert.assertEquals(3, searchableProperties.get(0).getCodelists().iterator().next().intValue());
		Assert.assertEquals("label", searchableProperties.get(0).getText());
		Assert.assertEquals("fieldType", searchableProperties.get(0).getSolrType());
	}

	/**
	 * Test get all type fields when type name is not provided.
	 */
	@Test
	public void testGetTypeFields() {
		mockSemanticDefinitionService("testPropertyId", "testRangeClass", "testClassId");
		mockNamespaceRegistryService("emf:testShortUri", "testFullUri");
		mockDefinitionService("definitionId", "label", DisplayType.EDITABLE, "emf:testShortUri", "name", 3, true);

		searchablePropertiesService.reset();
		Map<String, List<PropertyDefinition>> allSearchableProperties = searchablePropertiesService.getTypeFields(null,
				null, null);
		Assert.assertEquals(1, allSearchableProperties.size());
	}

	/**
	 * Test get all type fields when type name is provided.
	 */
	@Test
	public void testGetTypeFieldsByClassName() {
		mockSemanticDefinitionService("testPropertyId", "testRangeClass", "testClassId");
		mockNamespaceRegistryService("emf:testShortUri", "testFullUri");
		mockDefinitionService("definitionId", "label", DisplayType.EDITABLE, "emf:testShortUri", "name", 3, true);

		searchablePropertiesService.reset();
		Mockito.when(typeConverter.convert(Uri.class, "testClassId")).thenReturn(new ShortUri("emf:testClassId"));
		Map<String, List<PropertyDefinition>> allSearchableProperties = searchablePropertiesService.getTypeFields(null,
				"testClassId", "testClassId");
		Assert.assertEquals(1, allSearchableProperties.size());
	}

	/**
	 * Test get all type fields when instance is provided.
	 */
	@Test
	public void testGetTypeFieldsByInstance() {
		mockSemanticDefinitionService("testPropertyId", "testRangeClass", "testClassId");
		mockNamespaceRegistryService("emf:testShortUri", "testFullUri");
		mockDefinitionService("definitionId", "label", DisplayType.EDITABLE, "emf:testShortUri", "name", 3, true);

		searchablePropertiesService.reset();
		when(typeConverter.convert(String.class, "emf:Case")).thenReturn("emf:Case");

		ObjectInstance instance = new ObjectInstance();
		instance.add(SEMANTIC_TYPE, "emf:Case");
		InstanceTypeFake.setType(instance, "emf:Case", "caseinstance");
		Map<String, List<PropertyDefinition>> allSearchableProperties = searchablePropertiesService
				.getTypeFields(instance, "emf:Case", null);
		Assert.assertEquals(1, allSearchableProperties.size());
	}

	/**
	 * Test the get searchable property method. It should retrieve only the searchable property we are looking for.
	 */
	@Test
	public void testGetSearchableProperty() throws SolrClientException {
		mockSolrConnector("tokenizedField", "hasChild", "fieldType");
		mockSemanticDefinitionService("testPropertyId", "testRangeClass", "testClassId");
		mockNamespaceRegistryService("emf:hasChild",
				"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#hasChild");
		mockDefinitionService("definitionId", "label", DisplayType.EDITABLE, "emf:hasChild", "name", 3, true);

		searchablePropertiesService.reset();

		Optional<SearchableProperty> searchableProperty = searchablePropertiesService
				.getSearchableProperty("definitionId", "name");
		Assert.assertEquals("name", searchableProperty.get().getId());
	}

	/**
	 * Tests filtering the searchable properties by definition with existing property in the definition.
	 */
	@Test
	public void testFilterPropertiesByDefinition_withAvailableProperty() throws SolrClientException {
		mockSolrConnector("tokenizedField", "createdBy", "fieldType");
		mockNamespaceRegistryService("createdBy", "createdBy");
		mockSemanticDefinitionService("createdBy", "user", "createdBy");
		mockDefinitionService("definitionId", "label", DisplayType.EDITABLE, "emf:shortUri", "createdBy", 3, true);

		searchablePropertiesService.reset();

		List<SearchableProperty> properties = searchablePropertiesCache.get("definitionId");
		Assert.assertEquals(1, properties.size());

		SearchableProperty property = properties.get(0);
		Assert.assertEquals("createdBy", property.getId());
	}

	@Test
	public void testFilterPropertiesByDefinition_withSystemProperty() throws SolrClientException {
		mockSolrConnector("tokenizedField", "createdBy", "fieldType");
		mockNamespaceRegistryService("createdBy", "createdBy");
		mockSemanticDefinitionService("createdBy", "user", "createdBy");
		mockDefinitionService("definitionId", "label", DisplayType.SYSTEM, "emf:shortUri", "createdBy", 3, true);

		searchablePropertiesService.reset();

		List<SearchableProperty> properties = searchablePropertiesCache.get("definitionId");
		Assert.assertNull(properties);
	}

	/**
	 * Tests filtering the searchable properties by definition with not existing property in the definition.
	 */
	@Test
	public void testFilterPropertiesByDefinition_withNotAvailableProperty() throws SolrClientException {
		mockSolrConnector("tokenizedField", "createdBy", "fieldType");
		mockNamespaceRegistryService("name", "name");
		mockSemanticDefinitionService("name", "string", "name");
		mockDefinitionService("definitionId", "label", DisplayType.EDITABLE, "emf:shortUri", "createdBy", 3, true);

		searchablePropertiesService.reset();

		List<SearchableProperty> properties = searchablePropertiesCache.get("definitionId");
		Assert.assertNull(properties);
	}

	@Test
	public void testGetSearchableSolrPropertiesFilterRelations() throws SolrClientException {
		mockSolrConnector("tokenizedField", "hasChild", "fieldType");
		mockSemanticDefinitionService("testPropertyId", "testRangeClass", "testClassId");
		mockNamespaceRegistryService("emf:hasChild",
				"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#hasChild");
		mockDefinitionService("definitionId", "label", DisplayType.EDITABLE, "emf:hasChild", "name", 3, true);

		searchablePropertiesService.reset();
		List<SearchableProperty> searchableProperties = searchablePropertiesService
				.getSearchableSolrProperties("definitionId", false, false, true);
		Assert.assertEquals(2, searchableProperties.size());
		Assert.assertEquals("rdf:type", searchableProperties.get(1).getText());
		Assert.assertEquals("label", searchableProperties.get(0).getText());
	}

	/**
	 * Tests the semantic properties retrieval if given without a definition id but there is one in the mapping.
	 */
	@Test
	public void testGetSemanticTypeWithoutProvidedDefinition() throws SolrClientException {
		mockSolrConnector("tokenizedField", "name", "fieldType");
		mockSemanticDefinitionService("testPropertyId", "testRangeClass", "testClassId");
		mockNamespaceRegistryService("emf:testShortUri", "testFullUri");
		mockDefinitionService("definitionId", "label", DisplayType.EDITABLE, "emf:testShortUri", "name", 3, true);
		searchablePropertiesService.reset();
		semanticDefinitionMapping.put("semanticId", "definitionId");
		List<SearchableProperty> searchableProperties = searchablePropertiesService
				.getSearchableSemanticProperties("semanticId");
		Assert.assertFalse(searchableProperties.isEmpty());
	}

	/**
	 * Mock solr connector. If the tokenizedfieldTypeName and the fieldType are the same, the field won't be returned.
	 */
	public void mockSolrConnector(String tokenizedFieldTypeName, String fieldName, String fieldType)
			throws SolrClientException {
		Map<String, Object> schema = new LinkedHashMap<>();

		List<SimpleOrderedMap<Object>> fields = new ArrayList<>();
		SimpleOrderedMap<Object> field = new SimpleOrderedMap<>();
		field.add("type", fieldType);
		field.add("name", fieldName);
		fields.add(field);

		List<SimpleOrderedMap<Object>> fieldTypes = new ArrayList<>();
		SimpleOrderedMap<Object> fieldTypeObject = new SimpleOrderedMap<>();
		fieldTypeObject.add("name", tokenizedFieldTypeName);
		SimpleOrderedMap<Object> analyzer = new SimpleOrderedMap<>();
		analyzer.add("tokenizer", new SimpleOrderedMap<>());
		fieldTypeObject.add("analyzer", analyzer);
		fieldTypes.add(fieldTypeObject);

		schema.put("fields", fields);
		schema.put("fieldTypes", fieldTypes);

		Mockito.when(solrConnector.retrieveSchema()).thenReturn(schema);
	}

	/**
	 * Mock the semantic definition service. This method mocks the following logic:
	 * <ul>
	 * <li>rangeClass retrieval;</li>
	 * <li>all semantic classes retrieval;</li>
	 * </ul>
	 * When mocking the rangeClass retrieval, it will return one {@link PropertyInstance} with the specified id and
	 * rangeClass. When mocking the all semantic classes retrieval, it will return one {@link ClassInstance} with the
	 * specified id.
	 */
	public void mockSemanticDefinitionService(String propertyInstanceId, String rangeClass, String classInstanceId) {
		// Mock returned properties
		List<PropertyInstance> propertyInstances = new ArrayList<>();
		PropertyInstance propertyInstance = new PropertyInstance();
		propertyInstance.setId(propertyInstanceId);
		propertyInstance.setRangeClass(rangeClass);
		propertyInstance.setLabel("en", propertyInstanceId);
		propertyInstances.add(propertyInstance);
		Map<String, Serializable> properties = new HashMap<>();
		properties.put("title", "title");
		propertyInstance.setProperties(properties);

		// Mock returned class instances
		List<ClassInstance> classInstances = new ArrayList<>();
		ClassInstance classInstance = new ClassInstance();
		classInstance.setId(classInstanceId);
		classInstances.add(classInstance);

		Mockito.when(semanticDefinitionService.getRelations(Matchers.anyString(), Matchers.any())).thenReturn(
				propertyInstances);
		Mockito.when(semanticDefinitionService.getProperties()).thenReturn(propertyInstances);
		Mockito.when(semanticDefinitionService.getSearchableClasses()).thenReturn(classInstances);
		Mockito.when(semanticDefinitionService.getClassInstance(classInstanceId)).thenReturn(classInstance);
	}

	/**
	 * Mock the namespace registry service.
	 */
	public void mockNamespaceRegistryService(String shortUri, String fullUri) {
		Mockito.when(namespaceRegistryService.getShortUri(anyString())).thenReturn(shortUri);
		Mockito.when(namespaceRegistryService.buildFullUri(anyString())).thenReturn(fullUri);
	}

	/**
	 * Mock dictionary service. When {@link DefinitionService#getAllDefinitions(Class)} is called, returns one
	 * {@link DefinitionModel} with one field with it's properties set. When
	 *
	 * @param definitionId
	 *            the definition id
	 * @param fieldLabel
	 *            the field label
	 * @param fieldDisplayType
	 *            the field display type
	 * @param fieldUri
	 *            the field uri
	 * @param fieldName
	 *            the field name
	 * @param fieldCodelist
	 *            the field codelist {@link DefinitionService#find(String)} is called, returns one
	 *            {@link DefinitionModel} with one field with it's properties set.
	 * @param setRdfType
	 *            indicates whether the rdf type of the returned definition field should be set
	 */
	public void mockDefinitionService(String definitionId, String fieldLabel, DisplayType fieldDisplayType,
			String fieldUri, String fieldName, int fieldCodelist, boolean setRdfType) {
		DataTypeDefinition dataTypeDefinition = Mockito.mock(DataTypeDefinition.class);
		DefinitionModel definitionModel = new TestDefinition(fieldLabel, fieldDisplayType, fieldUri, fieldName,
				fieldCodelist, setRdfType);
		definitionModel.setIdentifier(definitionId);
		Mockito.when(definitionService.getDataTypeDefinition(anyString())).thenReturn(dataTypeDefinition);
		Mockito.when(definitionService.getAllDefinitions(any(InstanceType.class))).then(
				a -> Stream.of(definitionModel));
		Mockito.when(definitionService.find(anyString())).thenReturn(definitionModel);
		Mockito.when(definitionService.getInstanceDefinition(any(Instance.class))).thenReturn(definitionModel);
	}

	private void mockLabelProvider() {
		Function<String, String> function = Mockito.mock(Function.class);
		Mockito.when(function.apply(Matchers.anyString())).thenAnswer(answer -> answer.getArgumentAt(0, String.class));
		Mockito.when(labelProvider.getLabelProvider()).thenReturn(Function.identity());
	}

	private static class TestDefinition extends BaseDefinition<TestDefinition>
			implements AllowedChildrenModel, PathElement {

		private static final long serialVersionUID = -1949401309943007247L;

		/**
		 * Instantiates a new test definition.
		 *
		 * @param label
		 *            the label
		 * @param displayType
		 *            the display type
		 * @param uri
		 *            the uri
		 * @param name
		 *            the name
		 * @param codeList
		 *            the code list
		 * @param setRdfType
		 *            indicates whether the rdf type of the returned definition field should be set
		 */
		public TestDefinition(String label, DisplayType displayType, String uri, String name, int codeList,
				boolean setRdfType) {
			super();
			List<PropertyDefinition> properties = new ArrayList<>();
			PropertyDefinitionProxy property = new PropertyDefinitionProxy();
			property.setLabelId(label);
			property.setDisplayType(displayType);
			property.setUri(uri);
			property.setName(name);
			property.setCodelist(codeList);
			// Added twice so it covers more branch coverage
			properties.add(property);
			properties.add(property);

			if (setRdfType) {
				PropertyDefinitionProxy rdfType = new PropertyDefinitionProxy();
				rdfType.setName(DefaultProperties.SEMANTIC_TYPE);
				rdfType.setUri(uri);
				rdfType.setValue("emf:test");
				rdfType.setLabelId(DefaultProperties.SEMANTIC_TYPE);
				rdfType.setDisplayType(displayType);
				properties.add(rdfType);
			}
			setFields(properties);
		}

		private List<AllowedChildDefinition> allowedChildren = new LinkedList<>();

		@Override
		public List<AllowedChildDefinition> getAllowedChildren() {
			return allowedChildren;
		}

		@Override
		public PathElement getParentElement() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getPath() {
			return getIdentifier();
		}
	}
}
