package com.sirma.itt.emf.semantic.persistence;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ejb.EJBException;

import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.emf.mocks.DefinitionServiceMock;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.mocks.SemanticDefinitionServiceMock;
import com.sirma.itt.emf.mocks.search.QueryBuilderMock;
import com.sirma.itt.emf.mocks.search.SemanticPropertiesWriteConverterMock;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.monitor.NoOpStatistics;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.fakes.InstanceTypeFake;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Proton;
import com.sirma.itt.semantic.queries.QueryBuilder;

/**
 * Test for {@link SemanticDbDaoImpl}
 *
 * @author Valeri Tishev
 */
public class SemanticDaoTest extends GeneralSemanticTest<DbDao> {

	final String expectedSubInstancePropertyName = "subInstancePropertyName";

	final String expectedStringPropertyName = "emf:stringPropertyName";
	final String expectedStringPropertyValue = "sample string property value";

	final String expectedBooleanPropertyName = "booleanPropertyName";
	final Boolean expectedBooleanPropertyValue = Boolean.TRUE;

	private final DbDao cut = new SemanticDbDaoImpl();
	private NamespaceRegistryService namespaceRegistryService;

	final String expectedOwningInstanceId = "emf:my-case";
	private RepositoryConnection repositoryConnection;
	private EventService eventService;

	/**
	 * Initialize class under test.
	 */
	@BeforeClass
	public void initializeClassUnderTest() {
		eventService = mock(EventService.class);
		context.put("eventService", eventService);
		namespaceRegistryService = new NamespaceRegistryMock(context);
		ReflectionUtils.setFieldValue(cut, "namespaceRegistryService", namespaceRegistryService);
		ReflectionUtils.setFieldValue(cut, "definitionService", new DefinitionServiceMock());
		ReflectionUtils.setFieldValue(cut, "valueFactory", SimpleValueFactory.getInstance());

		ReflectionUtils.setFieldValue(cut, "idManager", idManager);

		QueryBuilder queryBuilder = new QueryBuilderMock(context);
		ReflectionUtils.setFieldValue(cut, "queryBuilder", queryBuilder);

		SemanticPropertiesReadConverter readConverter = new SemanticPropertiesReadConverter();
		ReflectionUtils.setFieldValue(readConverter, "namespaceRegistryService", namespaceRegistryService);

		SemanticDefinitionServiceMock semanticDefinitionService = new SemanticDefinitionServiceMock(context);
		SemanticPropertiesWriteConverter writeConverter = new SemanticPropertiesWriteConverterMock(context);

		ReflectionUtils.setFieldValue(cut, "readConverter", readConverter);
		ReflectionUtils.setFieldValue(cut, "writeConverter", writeConverter);

		ReflectionUtils.setFieldValue(cut, "semanticDefinitionService", semanticDefinitionService);
		InstanceTypes instanceTypes = mock(InstanceTypes.class);
		when(instanceTypes.from(any(Instance.class))).then(a -> Optional
				.ofNullable(semanticDefinitionService
						.getClassInstance(a.getArgumentAt(0, Instance.class).getAsString(SEMANTIC_TYPE)))
					.map(Instance::type));
		ReflectionUtils.setFieldValue(cut, "instanceTypes", instanceTypes);

		// mock the type converter
		InstanceReference instanceReferenceMock = mockInstanceReference();
		TypeConverter typeConverterMock = Mockito.mock(TypeConverter.class);
		Mockito.when(typeConverterMock.convert(InstanceReference.class, EMF.DOMAIN_OBJECT)).thenReturn(
				instanceReferenceMock);

		Mockito.when(typeConverterMock.convert(Class.class, ObjectInstance.class.getName())).thenReturn(
				ObjectInstance.class);

		SecurityContext securityCtx = mock(SecurityContext.class);
		EmfUser user = new EmfUser();
		user.setId("emf:admin");
		when(securityCtx.getAuthenticated()).thenReturn(user);
		ReflectionUtils.setFieldValue(cut, "securityContext", securityCtx);
		ReflectionUtils.setFieldValue(cut, "statistics", new NoOpStatistics());
	}

	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();
		repositoryConnection = connectionFactory.produceManagedConnection();

		ReflectionUtils.setFieldValue(cut, "repositoryConnection", repositoryConnection);
		reset(eventService);
	}

	/**
	 * Test saving new case instance.
	 *
	 * @throws RepositoryException
	 *             the repository exception
	 * @throws MalformedQueryException
	 *             the malformed query exception
	 * @throws QueryEvaluationException
	 *             the query evaluation exception
	 */
	@Test
	public void testSavingNewCaseInstance() throws RepositoryException, MalformedQueryException,
			QueryEvaluationException, EJBException, RemoteException {
		Instance caseInstance = createInstance(expectedStringPropertyValue, expectedBooleanPropertyValue);

		// save the case instance via the semantic DAO
		cut.saveOrUpdate(caseInstance);

		commitTransaction();

		// assert that the DAO has assigned id of the
		// newly created instance object
		Assert.assertNotNull(caseInstance.getId());

		// ask the semantic repository
		// whether there is any instance
		// which is part of the expected parent instance
		StringBuilder query = new StringBuilder();
		query
				.append(namespaceRegistryService.getNamespaces())
					.append("ask {")
					.append(caseInstance.getId().toString())
					.append(" ")
					.append(Proton.PREFIX)
					.append(":")
					.append("partOf")
					.append(" ")
					.append(expectedOwningInstanceId)
					.append("}");

		BooleanQuery booleanQuery = repositoryConnection.prepareBooleanQuery(QueryLanguage.SPARQL, query.toString());
		Assert.assertTrue(booleanQuery.evaluate());

		// clear the query builder
		query = new StringBuilder();

		// ask the semantic repository
		// whether the newly created case instance
		// has a stringPropertyName with the
		// expected stringPropertyValue
		query
				.append(namespaceRegistryService.getNamespaces())
					.append("ask {")
					.append(caseInstance.getId().toString())
					.append(" ")
					.append(expectedStringPropertyName)
					.append(" ")
					.append("?value.")
					// .append("'")
					// .append(expectedStringPropertyValue)
					// .append("'")
					.append("}");

		booleanQuery = repositoryConnection.prepareBooleanQuery(QueryLanguage.SPARQL, query.toString());
		Assert.assertTrue(booleanQuery.evaluate());
	}

	private Instance createInstance(Serializable property1Value, Serializable property2Value) {
		// create a new case instance
		Instance caseInstance = new EmfInstance("emf:" + System.currentTimeMillis());
		// set its case definition
		// see test/resources/definitions/genericCaseDev.xml
		caseInstance.setIdentifier("genericCaseDev");
		InstanceTypeFake.setType(caseInstance, EMF.CASE.toString(), "caseinstance");

		// set instance's properties map
		caseInstance.add(expectedStringPropertyName, property1Value);
		caseInstance.add(expectedBooleanPropertyName, property2Value);
		// caseInstance.add(expectedSubInstancePropertyName, "emf:link-to");
		// caseInstance.add("subInstanceFromRef", new LinkSourceId("emf:refId", mock(DataTypeDefinition.class)));
		// ObjectInstance instance = new ObjectInstance();
		// instance.getOrCreateProperties();
		// instance.setIdentifier("genericCaseDev");
		// instance.setId("emf:instId");
		// InstanceTypeFake.setType(instance, EMF.CASE.toString(), "caseinstance");
		// caseInstance.add("subInstanceFromInst", instance);
		caseInstance.add("uriField", "emf:link-to");
		caseInstance.add("userField", "emf:link-to");
		// caseInstance.add("uriFieldFromRef", new LinkSourceId("emf:refId", mock(DataTypeDefinition.class)));
		// ObjectInstance = instance = new ObjectInstance();
		// instance.getOrCreateProperties();
		// instance.setIdentifier("genericCaseDev");
		// instance.setId("emf:instId");
		// InstanceTypeFake.setType(instance, EMF.CASE.toString(), "caseinstance");
		// caseInstance.add("uriFieldFromInst", instance);
		caseInstance.add("multiUserField", (Serializable) Arrays.asList("emf:user1", "emf:user2"));
		caseInstance.add("multiValueField", (Serializable) Arrays.asList("value1", "value2"));
		caseInstance.add("emf:multiValueField", (Serializable) Arrays.asList("value1", "value2", null));
		// instance = new ObjectInstance();
		// instance.getOrCreateProperties();
		// instance.setIdentifier("genericCaseDev");
		// instance.setId("emf:instId");
		// InstanceTypeFake.setType(instance, EMF.CASE.toString(), "caseinstance");
		// caseInstance.add("emf:uriFieldFromInst", instance);

		// BaseStringIdEntity entity = new BaseStringIdEntity();
		// entity.setId("emf:someEntity");
		// caseInstance.add("subInstanceFromEntity", entity);

		// create and set owning instance of the case

		InstanceReferenceMock owning = InstanceReferenceMock.createGeneric(expectedOwningInstanceId);
		InstanceTypeFake.setType(owning.toInstance(), EMF.PROJECT.toString(), "projectinstance");
		contextService.bindContext(caseInstance, owning);
		return caseInstance;
	}

	@Test
	public void test_saveInstanceDiff() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		Instance instance = createInstance("some value that should be updated", Boolean.FALSE);

		// save the instance that will be updated later
		Instance updated = cut.saveOrUpdate(instance, null);

		Instance newInstance = createInstance(expectedStringPropertyValue, expectedBooleanPropertyValue);
		newInstance.getProperties().remove(expectedSubInstancePropertyName);
		newInstance.setId(updated.getId());

		cut.saveOrUpdate(newInstance, updated);

		commitTransaction();

		StringBuilder query = new StringBuilder();

		// ask the semantic repository
		// whether the updated created case instance
		// has a stringPropertyName with the
		// expected stringPropertyValue
		query
				.append(namespaceRegistryService.getNamespaces())
					.append("ask {")
					.append(updated.getId().toString())
					.append(" ")
					.append(expectedStringPropertyName)
					.append(" ")
					.append("?value.")
					// .append("'")
					// .append(expectedStringPropertyValue)
					// .append("'")
					.append("}");

		BooleanQuery booleanQuery = repositoryConnection.prepareBooleanQuery(QueryLanguage.SPARQL, query.toString());
		Assert.assertTrue(booleanQuery.evaluate());
	}

	@Test
	public void saveInstance_shouldFireRemoveRelationsBeforeAddRelations() throws RepositoryException, MalformedQueryException, QueryEvaluationException {

		Instance instance = createInstance("some value that should be updated", Boolean.FALSE);

		// save the instance that will be updated later
		Instance updated = cut.saveOrUpdate(instance, null);

		Instance newInstance = createInstance(expectedStringPropertyValue, expectedBooleanPropertyValue);
		newInstance.remove(expectedSubInstancePropertyName);
		newInstance.add("uriField", "emf:new-link-to");

		newInstance.setId(updated.getId());

		checkEventOrder();

		cut.saveOrUpdate(newInstance, updated);

		commitTransaction();

		verify(eventService, times(11)).fire(any());
	}

	private void checkEventOrder() {
		AtomicBoolean check = new AtomicBoolean(true);
		doAnswer(a -> {
			EmfEvent event = a.getArgumentAt(0, EmfEvent.class);
			if (event instanceof AddRelationEvent) {
				check.set(false);
			} else if (event instanceof RemoveRelationEvent) {
				assertTrue(check.get(), "Add event fired first. It should be after remove event:");
			}
			return null;
		}).when(eventService).fire(any());
	}

	@Test
	public void test_fetchById() {
		noTransaction();
		// see test/resources/data/SemanticDaoTestData.ttl
		Instance found = cut.find(Instance.class, "emf:my_case");
		assertNotNull(found);
		assertEquals(found.getId(), "emf:my_case");
		assertEquals(found.getString("stringPropertyName"), "sample case string property");
		assertEquals(found.getBoolean(expectedBooleanPropertyName), expectedBooleanPropertyValue.booleanValue());
	}

	/**
	 * Test find instance by id.
	 */
	@Test
	public void testFindInstanceById() {
		noTransaction();
		// see test/resources/data/SemanticDaoTestData.ttl
		final String expectedCaseSectionId = "emf:my-section";
		ObjectInstance found = cut.find(ObjectInstance.class, expectedCaseSectionId);

		Assert.assertNotNull(found);
		// see test/resources/data/SemanticDaoTestData.ttl
		Assert.assertEquals("dms_ID", found.getDmsId());
	}

	@Test
	public void test_delete() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		Instance instance = createInstance("some value that should be deleted", Boolean.FALSE);

		// save the instance that will be updated later
		Instance updated = cut.saveOrUpdate(instance, null);

		commitTransaction();
		beginTransaction();

		Instance found = cut.find(Instance.class, updated.getId());
		assertNotNull(found);
		assertEquals(found.getId(), updated.getId());

		cut.delete(ObjectInstance.class, updated.getId());

		commitTransaction();

		found = cut.find(Instance.class, updated.getId());
		assertNull(found);
	}

	@Test
	public void test_deleteMultiple() throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		Instance instance1 = createInstance("some value that should be deleted 1", Boolean.FALSE);
		Instance instance2 = createInstance("some value that should be deleted 2", Boolean.TRUE);

		// save the instance that will be updated later
		Instance updated1 = cut.saveOrUpdate(instance1);
		Instance updated2 = cut.saveOrUpdate(instance2);

		commitTransaction();
		beginTransaction();

		Instance found1 = cut.find(Instance.class, updated1.getId());
		assertNotNull(found1);
		assertEquals(found1.getId(), updated1.getId());
		Instance found2 = cut.find(Instance.class, updated2.getId());
		assertNotNull(found2);
		assertEquals(found2.getId(), updated2.getId());

		cut.delete(Instance.class, (Serializable) Arrays.asList(updated1.getId(), updated2.getId()));

		commitTransaction();

		found1 = cut.find(Instance.class, updated1.getId());
		assertNull(found1);
		found2 = cut.find(Instance.class, updated2.getId());
		assertNull(found2);
	}

	@Override
	protected String getTestDataFile() {
		return "SemanticDaoTestData.ttl";
	}

	/**
	 * Mock instance reference.
	 *
	 * @return the instance reference
	 */
	private static InstanceReference mockInstanceReference() {
		return new InstanceReferenceMock();
	}
}
