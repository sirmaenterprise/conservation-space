package com.sirma.itt.emf.semantic;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static java.util.Collections.emptyList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.mocks.SemanticDefinitionServiceMock;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.instance.HeadersService;
import com.sirma.itt.seip.instance.library.LibraryProvider;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Proton;

/**
 * @author kirq4e
 */
public class SemanticDefinitionServiceTest extends GeneralSemanticTest<SemanticDefinitionServiceImpl> {

	private NamespaceRegistryService namespaceRegistryService;

	@Mock
	private DictionaryService dictionaryService;

	@Mock
	private HeadersService headersService;

	@Mock
	private SemanticConfiguration semanticConfiguration;

	/**
	 * Initializes service
	 *
	 * @throws RepositoryException
	 */
	@BeforeClass
	public void init() throws RepositoryException {
		MockitoAnnotations.initMocks(this);
		service = new SemanticDefinitionServiceMock(context);
		ReflectionUtils.setField(service, "headersService", headersService);
		ReflectionUtils.setField(service, "dictionaryService", dictionaryService);
		service.initializeCache();
		namespaceRegistryService = new NamespaceRegistryMock(context);

	}

	/**
	 * Tests all methods if they return filled lists
	 */
	@Test
	public void testAllMethods() {
		List<ClassInstance> classes = service.getClasses();
		Assert.assertFalse(classes.isEmpty());
		List<PropertyInstance> properties = service.getProperties();
		Assert.assertFalse(properties.isEmpty());
		List<PropertyInstance> relations = service.getRelations();
		Assert.assertFalse(relations.isEmpty());
		for (PropertyInstance propertyInstance : relations) {
			Assert.assertNotNull(propertyInstance.getId());
			Assert.assertNotNull(propertyInstance.getProperties().get("title"));
		}
		List<PropertyInstance> searchableRelations = service.getSearchableRelations();
		Assert.assertFalse(searchableRelations.isEmpty());
		for (PropertyInstance propertyInstance : searchableRelations) {
			Assert.assertNotNull(propertyInstance.getId());
			Assert.assertNotNull(propertyInstance.getProperties().get("title"));
		}
	}

	/**
	 * Test system relations
	 */
	@Test
	public void testSystemRelations() {
		/**
		 * Relations listed below are defined as system in EMF ontology (see emf.ttl)
		 */
		String[] expectedSystemRelations = new String[] { "emf:hasChild", "emf:isThumbnailOf" };

		for (String propertyUri : expectedSystemRelations) {
			Assert.assertTrue(service.isSystemRelation(propertyUri),
					"Property [" + propertyUri + "] is not system property!");
		}

		/**
		 * Relations listed below are NOT defined as system in EMF ontology (see emf.ttl)
		 */
		String[] expectedNonSystemRelations = new String[] { "emf:processedBy", "emf:isMemberOf" };

		for (String propertyUri : expectedNonSystemRelations) {
			Assert.assertFalse(service.isSystemRelation(propertyUri),
					"Property [" + propertyUri + "] is system property!");
		}

	}

	/**
	 * Test get hierarchy method with existing class
	 */
	@Test
	public void testGetHierarchyForExistingClass() {
		String classId = "emf:Customer";

		List<String> hierarchy = service.getHierarchy(classId);

		Assert.assertFalse(hierarchy.isEmpty());
	}

	/**
	 * Test get hierarchy method with non existing class
	 */
	@Test
	public void testGetHierarchyForNonExistingClass() {
		String classId = "emf:asfqwfqwr";

		List<String> hierarchy = service.getHierarchy(classId);

		Assert.assertTrue(hierarchy.isEmpty());
	}

	/**
	 * Tests filter properties method
	 */
	@Test
	public void testFilterProperties() {
		List<PropertyInstance> properties = service.getProperties("");
		Assert.assertTrue(properties.isEmpty());
		properties = service.getProperties(Proton.ENTITY.toString());
		Assert.assertFalse(properties.isEmpty());
	}

	/**
	 * Tests filter properties method with existing class
	 */
	@Test
	public void testGetPropertiesWithExistingClass() {
		List<PropertyInstance> properties = service.getProperties(EMF.NAMESPACE + "Customer");
		Assert.assertFalse(properties.isEmpty());
	}

	/**
	 * Tests filter properties method with non existing class
	 */
	@Test
	public void testGetPropertiesWithNonExistingClass() {
		List<PropertyInstance> properties = service.getProperties("emf:asdasd");
		Assert.assertTrue(properties.isEmpty());
	}

	/**
	 * Tests filter relations method This test isn`t accurate because of usage of different repositories for the test
	 * and for real work. Current repository doesn`t support OWL reasoning
	 */
	@Deprecated
	// @Test
	public void testFilterRelationsDeprecated() {
		String fromClass = Proton.ENTITY.toString();
		List<PropertyInstance> relations = service.getRelations(CollectionUtils.emptyList(),
				CollectionUtils.emptyList());
		Assert.assertFalse(relations.isEmpty());
		relations = service.getRelations(fromClass, null);
		Assert.assertFalse(relations.isEmpty());
		relations = service.getRelations(null, fromClass);
		Assert.assertFalse(relations.isEmpty());
		relations = service.getRelations(fromClass, fromClass);
		Assert.assertFalse(relations.isEmpty());

		PropertyInstance relation = service.getRelation("emf:hasChild");
		Assert.assertNotNull(relation);

		relations = service.getRelations("chd:Drawing", null);
		Assert.assertFalse(relations.isEmpty());
	}

	/**
	 * Tests filtering of relations
	 *
	 * @param domainClasses
	 *            Collection with domain classes to filter the relations
	 * @param rangeClasses
	 *            Collection with range classes to filter the relations
	 */
	@Test(dataProvider = "relationships-data-provider")
	public void testFilterRelations(Collection<String> domainClasses, Collection<String> rangeClasses) {

		List<PropertyInstance> filteredRelations = service.getRelations(domainClasses, rangeClasses);

		List<PropertyInstance> allRelations = service.getSearchableRelations();
		List<PropertyInstance> toMatch = allRelations.stream().filter(relation -> {
			boolean domainMatches = isEmpty(domainClasses)
					|| domainClasses.contains(namespaceRegistryService.buildFullUri(relation.getDomainClass()));
			boolean rangeMatches = isEmpty(rangeClasses)
					|| rangeClasses.contains(namespaceRegistryService.buildFullUri(relation.getRangeClass()));
			if (domainMatches && rangeMatches) {
				return true;
			}
			return false;
		}).collect(Collectors.toList());

		Assert.assertTrue(toMatch.isEmpty() && filteredRelations.isEmpty() || filteredRelations.containsAll(toMatch));
	}

	/**
	 * Data provider for {@link #testDomainFilter(List, List, String)}
	 *
	 * @return test data.
	 */
	@DataProvider(name = "relationships-data-provider")
	public Object[][] domainAndRangeFiltersDataProvider() {
		return new Object[][] { { emptyList(), emptyList() }, { emptyList(), null }, { null, emptyList() },
				{ null, null }, { Arrays.asList("ptop:Entity"), null }, { null, Arrays.asList("ptop:Entity") },
				{ Arrays.asList("ptop:Entity"), Arrays.asList("ptop:Entity") },
				{ Arrays.asList("emf:Unknown"), Arrays.asList("emf:Fake") },
				{ Arrays.asList("emf:Project", "emf:Document"), Arrays.asList("emf:Document", "ptop:Agent") } };
	}

	/**
	 * Tests list of object library
	 */
	@Test
	public void testGetObjectLibrary() {
		List<ClassInstance> objectLibrary = service.getLibrary(LibraryProvider.OBJECT_LIBRARY);
		Assert.assertFalse(objectLibrary.isEmpty());
	}

	/**
	 * Tests narrow down relations list
	 */
	@Test
	public void testNarrowDownRelations() {
		List<PropertyInstance> relationsList = service.getSearchableRelations();
		Assert.assertFalse(relationsList.isEmpty());

		RepositoryConnection connection = connectionFactory.produceReadOnlyConnection();

		Set<String> propertiesIds = new HashSet<>();
		for (PropertyInstance propertyInstance : relationsList) {
			propertiesIds.add(propertyInstance.getId().toString());
		}

		try {
			String query = namespaceRegistryService.getNamespaces() + "\n";
			query += "select ?instance where { ?instance a owl:ObjectProperty . filter ( not exists {?instance emf:isSearchable ?isSearchable }) }";
			TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);

			TupleQueryResult result = tupleQuery.evaluate();

			while (result.hasNext()) {
				BindingSet resultRow = result.next();
				URI binding = (URI) resultRow.getBinding("instance").getValue();
				String uri = namespaceRegistryService.getShortUri(binding);

				Assert.assertFalse(propertiesIds.contains(uri));
			}

			result.close();

		} catch (RepositoryException | MalformedQueryException | QueryEvaluationException e) {
			Assert.fail(e.getMessage(), e);
		}
	}

	/**
	 * Test filter by parent logic in DefinitionRestService
	 */
	@Test
	public void testGetTypesFiteredByParent() {
		List<String> filter = new ArrayList<>();
		filter.add("emf:DomainObject");

		List<ClassInstance> classes = service.getSearchableClasses();

		List<String> result = new ArrayList<>();
		for (ClassInstance classInstance : classes) {
			if (!hasParent(filter, classInstance)) {
				continue;
			}
			result.add(classInstance.getId().toString());

		}
	}

	/**
	 * Tests {@link SemanticDefinitionService#getRootClass()} if it returns the correct {@link ClassInstance}.
	 */
	@Test
	public void testGetRootClass() {
		ConfigurationProperty<String> rootName = new ConfigurationPropertyMock<>("ptop:Entity");
		Mockito.when(semanticConfiguration.getRootClassName()).thenReturn(rootName);
		ReflectionUtils.setField(service, "semanticConfiguration", semanticConfiguration);
		ClassInstance rootClass = service.getRootClass();
		Assert.assertEquals(rootClass.getId(), Proton.ENTITY.toString());
	}

	/**
	 * Tests fetching of definitions that are connected with the Class Project
	 */
	@Test
	public void testGetDefinitions() {
		ClassInstance classInstance = service.getClassInstance("emf:Project");
		Assert.assertNotNull(classInstance);
		Assert.assertFalse(classInstance.getProperties().isEmpty());
		Serializable definitions = classInstance.getProperties().get("definitions");
		Assert.assertNotNull(definitions);
		Assert.assertTrue(Set.class.isInstance(definitions));
		Set<Serializable> definitionsSet = (Set<Serializable>) definitions;
		Assert.assertTrue(definitionsSet.contains("TESTDEF123"));
		Assert.assertTrue(definitionsSet.contains("TESTDEF234"));
		Assert.assertTrue(definitionsSet.contains("TESTDEF345"));
	}

	/**
	 * Tests fetching of definitions that are connected with the Class Case which doesn't have connections to a
	 * definition
	 */
	@Test
	public void testGetDefinitionsForClassWithoutDefinition() {
		ClassInstance classInstance = service.getClassInstance("emf:Case");
		Assert.assertNotNull(classInstance);
		Assert.assertFalse(classInstance.getProperties().isEmpty());
		Serializable definitions = classInstance.getProperties().get("definitions");
		Assert.assertNull(definitions);
	}

	/**
	 * Verifies that all classes returned by {@link SemanticDefinitionService#getClassesForOntology(String)} have
	 * ontology property set and they belong to the requested ontology.
	 */
	@Test
	public void testGetClassesForOntology() {
		String emfOntology = "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework";
		List<ClassInstance> classes = service
				.getClassesForOntology(emfOntology);
		boolean allFromEmfOntology = true;
		for (ClassInstance classInstance : classes) {
			String ontology = classInstance.getString("ontology");
			if(StringUtils.isNullOrEmpty(ontology) || !emfOntology.equals(ontology)) {
				allFromEmfOntology = false;
				break;
			}
		}
		Assert.assertTrue(allFromEmfOntology);
	}

	@Test
	public void testMostConcreteClass() {
		Assert.assertNull(service.getMostConcreteClass(null));
		Assert.assertEquals(service.getMostConcreteClass(Arrays.asList("emf:Case")), EMF.CASE.toString());
		Assert.assertEquals(service.getMostConcreteClass(Arrays.asList("emf:Case", "emf:Activity")),
				EMF.CASE.toString());
		Assert.assertEquals(service.getMostConcreteClass(Arrays.asList("emf:Activity", "emf:Case")),
				EMF.CASE.toString());

		// this is a real case for document type resolving
		Assert.assertEquals(
				service.getMostConcreteClass(Arrays.asList("ptop:InformationResource", "ptop:Object", "ptop:Happening",
						"ptop:Entity", "ptop:Statement", "ptop:Document", "ptop:Event", "emf:Document")),
				EMF.DOCUMENT.toString());
		// passing full URIs in mixed order to ensure that even if most-concrete class can't be calculated, 
		// ClassDescription is used by default
		Assert.assertEquals(
				service.getMostConcreteClass(Arrays.asList("http://www.w3.org/2000/01/rdf-schema#Class",
						"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#ClassDescription",
						"http://www.ontotext.com/proton/protontop#Abstract",
						"http://www.ontotext.com/proton/protontop#Entity", "http://www.w3.org/2002/07/owl#Class")),
				EMF.CLASS_DESCRIPTION.stringValue());
	}

	/**
	 * Recursively checks the parents of a class to see if one of them is in a list of parents.
	 *
	 * @param parents
	 *            List of parents to look for.
	 * @param clazz
	 *            Class that we are checking.
	 * @return {@code true} if one of the parents of the class is in the list of filtered classes, {@code false}
	 *         otherwise.
	 */
	private boolean hasParent(List<String> parents, ClassInstance clazz) {
		if (clazz == null) {
			return false;
		}

		String clazzId = clazz.getId().toString();
		if (parents.contains(clazzId)) {
			return true;
		}

		HashSet<Serializable> superClasses = (HashSet<Serializable>) clazz.getProperties().get("superClasses");

		for (Serializable superClass : superClasses) {
			ClassInstance owningInstance = service.getClassInstance(superClass.toString());
			if (hasParent(parents, owningInstance)) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected String getTestDataFile() {
		return "SemanticDefinitionServiceTestData.ttl";
	}

}
