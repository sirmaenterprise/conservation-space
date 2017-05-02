package com.sirma.itt.emf.semantic.persistence;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.CREATED_BY;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.MODIFIED_ON;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBException;

import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.emf.mocks.DictionaryServiceMock;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.mocks.SemanticDefinitionServiceMock;
import com.sirma.itt.emf.mocks.TransactionalRepositoryConnectionMock;
import com.sirma.itt.emf.mocks.search.QueryBuilderMock;
import com.sirma.itt.emf.mocks.search.SearchServiceMock;
import com.sirma.itt.emf.mocks.search.SemanticDaoMock;
import com.sirma.itt.emf.mocks.search.SemanticPropertiesWriteConverterMock;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkSearchArguments;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.testutil.fakes.InstanceTypeFake;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.TransactionalRepositoryConnection;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * @author BBonev
 */
public class SemanticLinkServiceImplTest extends GeneralSemanticTest<LinkService> {

	private NamespaceRegistryService namespaceRegistryService;

	private final LinkService cut = new SemanticLinkServiceImpl();
	private TransactionalRepositoryConnection transactionalRepositoryConnection;
	private DbDao dbDao;

	/**
	 * Initialize class under test.
	 */
	@BeforeClass
	public void initializeClassUnderTest() {
		namespaceRegistryService = new NamespaceRegistryMock(context);
		ReflectionUtils.setField(cut, "namespaceRegistryService", namespaceRegistryService);
		ReflectionUtils.setField(cut, "dictionaryService", new DictionaryServiceMock());
		ReflectionUtils.setField(cut, "valueFactory", connectionFactory.produceValueFactory());
		ReflectionUtils.setField(cut, "queryBuilder", new QueryBuilderMock(context));
		TypeConverter typeConverter = createTypeConverter();
		ReflectionUtils.setField(cut, "typeConverter", typeConverter);
		ReflectionUtils.setField(cut, "eventService", Mockito.mock(EventService.class));
		SemanticPropertiesWriteConverter writeConverter = new SemanticPropertiesWriteConverterMock(context);
		ReflectionUtils.setField(cut, "writeConverter", writeConverter);
		SemanticDefinitionServiceMock semanticDefinitionServiceMock = new SemanticDefinitionServiceMock(context);
		ReflectionUtils.setField(cut, "semanticDefinitionService", semanticDefinitionServiceMock);

		SemanticPropertiesReadConverter readConverter = new SemanticPropertiesReadConverter();
		ReflectionUtils.setField(readConverter, "namespaceRegistryService", namespaceRegistryService);
		ReflectionUtils.setField(cut, "readConverter", readConverter);

		ReflectionUtils.setField(cut, "searchService", new SearchServiceMock(context));
		ReflectionUtils.setField(cut, "stateService", mock(StateService.class));

		dbDao = new SemanticDaoMock(context);
	}

	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();
		transactionalRepositoryConnection = new TransactionalRepositoryConnectionMock(context);

		ReflectionUtils.setField(cut, "repositoryConnection",
				new InstanceProxyMock<>(transactionalRepositoryConnection));
		ReflectionUtils.setField(dbDao, "repositoryConnection",
				new InstanceProxyMock<>(transactionalRepositoryConnection));

		try {
			transactionalRepositoryConnection.afterBegin();
		} catch (EJBException | RemoteException e) {
			fail("", e);
		}
	}

	@AfterMethod
	public void commitTransaction() {
		try {
			transactionalRepositoryConnection.beforeCompletion();
			transactionalRepositoryConnection.afterCompletion(true);
		} catch (EJBException | RemoteException e) {
			fail("transaction commit failed", e);
		}
	}

	@Test
	public void test_createLink() {
		Instance from = createInstance();
		Instance to = createInstance();

		Pair<Serializable, Serializable> pair = cut.link(from, to, "emf:references", "emf:referencedBy", null);
		assertNotNull(pair.getFirst());
		assertNotNull(pair.getSecond());
	}

	@Test
	public void test_linkSimple_multiple() {
		Instance from = createInstance();
		Instance to1 = createInstance();
		Instance to2 = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to1);
		dbDao.saveOrUpdate(to2);

		assertTrue(cut.linkSimple(from.toReference(), Arrays.asList(to1.toReference(), to2.toReference()),
				"emf:references"));

		commitTransaction();

		assertTrue(cut.isLinkedSimple(from.toReference(), to1.toReference(), "emf:references"));
		assertTrue(cut.isLinkedSimple(from.toReference(), to2.toReference(), "emf:references"));
	}

	@Test
	public void test_getAllLinks() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		Pair<Serializable, Serializable> pair = cut.link(from, to, "emf:references", "emf:referencedBy", null);
		assertNotNull(pair.getFirst());
		assertNotNull(pair.getSecond());

		commitTransaction();

		List<LinkReference> list = cut.getLinks(from.toReference());
		assertNotNull(list);
		assertFalse(list.isEmpty());

		LinkReference linkReference = list.get(0);
		assertNotNull(linkReference.getFrom());
		assertEquals(linkReference.getFrom().getIdentifier(), from.toReference().getIdentifier());
		assertNotNull(linkReference.getTo());
		assertEquals(linkReference.getTo().getIdentifier(), to.toReference().getIdentifier());

		assertEquals(linkReference.getId(), pair.getFirst());
		assertEquals(linkReference.getIdentifier(), "emf:references");
	}

	@Test
	public void test_getLinks_Single() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DefaultProperties.CREATED_BY, securityContextManager.getSystemUser());

		Pair<Serializable, Serializable> pair = cut.link(from, to, "emf:references", "emf:referencedBy", properties);
		assertNotNull(pair.getFirst());
		assertNotNull(pair.getSecond());

		commitTransaction();

		List<LinkReference> list = cut.getLinks(from.toReference(), "emf:references");
		assertNotNull(list);
		assertFalse(list.isEmpty());

		LinkReference linkReference = list.get(0);
		assertNotNull(linkReference.getFrom());
		assertEquals(linkReference.getFrom().getIdentifier(), from.toReference().getIdentifier());
		assertNotNull(linkReference.getTo());
		assertEquals(linkReference.getTo().getIdentifier(), to.toReference().getIdentifier());

		assertEquals(linkReference.getId(), pair.getFirst());
		assertEquals(linkReference.getIdentifier(), "emf:references");
	}

	@Test
	public void test_getLinkReference() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DefaultProperties.CREATED_BY, securityContextManager.getSystemUser());

		Pair<Serializable, Serializable> pair = cut.link(from, to, "emf:references", "emf:referencedBy", properties);
		assertNotNull(pair.getFirst());
		assertNotNull(pair.getSecond());

		commitTransaction();

		LinkReference linkReference = cut.getLinkReference(pair.getFirst());
		assertNotNull(linkReference);
		assertNotNull(linkReference.getFrom());
		assertEquals(linkReference.getFrom().getIdentifier(), from.toReference().getIdentifier());
		assertNotNull(linkReference.getTo());
		assertEquals(linkReference.getTo().getIdentifier(), to.toReference().getIdentifier());

		assertEquals(linkReference.getId(), pair.getFirst());
		assertEquals(linkReference.getIdentifier(), "emf:references");

		assertFalse(linkReference.getProperties().isEmpty());
		assertTrue(linkReference.isPropertyPresent(CREATED_BY));
	}

	@Test
	public void test_getLinks_Multiple() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		Pair<Serializable, Serializable> pair1 = cut.link(from, to, "emf:references", "emf:referencedBy", null);
		assertNotNull(pair1.getFirst());
		assertNotNull(pair1.getSecond());
		Pair<Serializable, Serializable> pair2 = cut.link(from.toReference(), to.toReference(), "emf:dependsOn", null,
				null);
		assertNotNull(pair2.getFirst());
		assertNull(pair2.getSecond());

		commitTransaction();

		List<LinkReference> list = cut.getLinks(from.toReference(),
				new HashSet<>(Arrays.asList("emf:references", "emf:dependsOn")));
		assertNotNull(list);
		assertFalse(list.isEmpty());

		for (LinkReference linkReference : list) {
			Pair<Serializable, Serializable> pair = pair1;
			if ("emf:dependsOn".equals(linkReference.getIdentifier())) {
				pair = pair2;
			}

			assertNotNull(linkReference.getFrom());
			assertEquals(linkReference.getFrom().getIdentifier(), from.toReference().getIdentifier());
			assertNotNull(linkReference.getTo());
			assertEquals(linkReference.getTo().getIdentifier(), to.toReference().getIdentifier());

			assertEquals(linkReference.getId(), pair.getFirst());
		}
	}

	@Test
	public void test_getLinks_Simple() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		assertTrue(cut.linkSimple(from.toReference(), to.toReference(), "emf:references", "emf:referencedBy"));

		commitTransaction();

		List<LinkReference> list = cut.getSimpleLinks(from.toReference(), "emf:references");
		assertNotNull(list);
		assertFalse(list.isEmpty());

		LinkReference linkReference = list.get(0);
		assertNotNull(linkReference.getFrom());
		assertEquals(linkReference.getFrom().getIdentifier(), from.toReference().getIdentifier());
		assertNotNull(linkReference.getTo());
		assertEquals(linkReference.getTo().getIdentifier(), to.toReference().getIdentifier());

		assertEquals(linkReference.getIdentifier(), "emf:references");
	}

	/**
	 * Try to fetch a simple link after creating complex - the complex creation should create simple too
	 */
	@Test
	public void test_getLinks_SimpleFromComplex() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DefaultProperties.CREATED_BY, securityContextManager.getSystemUser());

		Pair<Serializable, Serializable> pair = cut.link(from, to, "emf:references", "emf:referencedBy", properties);
		assertNotNull(pair.getFirst());
		assertNotNull(pair.getSecond());

		commitTransaction();

		List<LinkReference> list = cut.getSimpleLinks(from.toReference(), "emf:references");
		assertNotNull(list);
		assertFalse(list.isEmpty());

		LinkReference linkReference = list.get(0);
		assertNotNull(linkReference.getFrom());
		assertEquals(linkReference.getFrom().getIdentifier(), from.toReference().getIdentifier());
		assertNotNull(linkReference.getTo());
		assertEquals(linkReference.getTo().getIdentifier(), to.toReference().getIdentifier());

		assertEquals(linkReference.getIdentifier(), "emf:references");
	}

	@Test
	public void test_getLinksTo() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DefaultProperties.CREATED_BY, securityContextManager.getSystemUser());

		Pair<Serializable, Serializable> pair = cut.link(from, to, "emf:references", "emf:referencedBy", properties);
		assertNotNull(pair.getFirst());
		assertNotNull(pair.getSecond());

		commitTransaction();

		List<LinkReference> list = cut.getLinksTo(to.toReference());
		assertNotNull(list);
		assertFalse(list.isEmpty());

		LinkReference linkReference = list.get(0);
		assertNotNull(linkReference.getFrom());
		assertEquals(linkReference.getFrom().getIdentifier(), from.toReference().getIdentifier());
		assertNotNull(linkReference.getTo());
		assertEquals(linkReference.getTo().getIdentifier(), to.toReference().getIdentifier());

		assertEquals(linkReference.getIdentifier(), "emf:references");
		assertEquals(linkReference.getId(), pair.getFirst());
	}

	@Test
	public void test_getLinks_inverseRelation() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DefaultProperties.CREATED_BY, securityContextManager.getSystemUser());

		Pair<Serializable, Serializable> pair = cut.link(from, to, "emf:references", "emf:referencedBy", properties);
		assertNotNull(pair.getFirst());
		assertNotNull(pair.getSecond());

		commitTransaction();

		List<LinkReference> list = cut.getLinks(to.toReference(), "emf:referencedBy");
		assertNotNull(list);
		assertFalse(list.isEmpty());

		LinkReference linkReference = list.get(0);
		assertNotNull(linkReference.getFrom());
		assertEquals(linkReference.getFrom().getIdentifier(), to.toReference().getIdentifier());
		assertNotNull(linkReference.getTo());
		assertEquals(linkReference.getTo().getIdentifier(), from.toReference().getIdentifier());

		assertEquals(linkReference.getIdentifier(), "emf:referencedBy");
		assertEquals(linkReference.getId(), pair.getSecond());
	}

	@Test
	public void test_getLinks_inverseRelation_Simple() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		assertTrue(cut.linkSimple(from.toReference(), to.toReference(), "emf:references", "emf:referencedBy"));

		commitTransaction();

		List<LinkReference> list = cut.getSimpleLinks(to.toReference(), "emf:referencedBy");
		assertNotNull(list);
		assertFalse(list.isEmpty());

		LinkReference linkReference = list.get(0);
		assertNotNull(linkReference.getFrom());
		assertEquals(linkReference.getFrom().getIdentifier(), to.toReference().getIdentifier());
		assertNotNull(linkReference.getTo());
		assertEquals(linkReference.getTo().getIdentifier(), from.toReference().getIdentifier());

		assertEquals(linkReference.getIdentifier(), "emf:referencedBy");
	}

	@Test
	public void test_isLinked_simple() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		assertTrue(cut.linkSimple(from.toReference(), to.toReference(), "emf:references", "emf:referencedBy"));

		commitTransaction();

		assertTrue(cut.isLinkedSimple(from.toReference(), to.toReference(), "emf:references"));
		assertTrue(cut.isLinkedSimple(to.toReference(), from.toReference(), "emf:referencedBy"));
	}

	@Test
	public void test_isLinked() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DefaultProperties.CREATED_BY, securityContextManager.getSystemUser());

		Pair<Serializable, Serializable> pair = cut.link(from, to, "emf:references", "emf:referencedBy", properties);
		assertNotNull(pair.getFirst());
		assertNotNull(pair.getSecond());

		commitTransaction();

		assertTrue(cut.isLinked(from.toReference(), to.toReference(), "emf:references"));
		assertTrue(cut.isLinked(to.toReference(), from.toReference(), "emf:referencedBy"));

		assertTrue(cut.isLinkedSimple(from.toReference(), to.toReference(), "emf:references"));
		assertTrue(cut.isLinkedSimple(to.toReference(), from.toReference(), "emf:referencedBy"));
	}

	@Test
	public void test_getLinksTo_Simple() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		assertTrue(cut.linkSimple(from.toReference(), to.toReference(), "emf:references", "emf:referencedBy"));

		commitTransaction();

		List<LinkReference> list = cut.getSimpleLinksTo(to.toReference(), "emf:references");
		assertNotNull(list);
		assertFalse(list.isEmpty());

		LinkReference linkReference = list.get(0);
		assertNotNull(linkReference.getFrom());
		assertEquals(linkReference.getFrom().getIdentifier(), from.toReference().getIdentifier());
		assertNotNull(linkReference.getTo());
		assertEquals(linkReference.getTo().getIdentifier(), to.toReference().getIdentifier());

		assertEquals(linkReference.getIdentifier(), "emf:references");
	}

	@Test
	public void test_unlink_all() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DefaultProperties.CREATED_BY, securityContextManager.getSystemUser());

		Pair<Serializable, Serializable> pair = cut.link(from, to, "emf:references", "emf:referencedBy", properties);
		assertNotNull(pair.getFirst());
		assertNotNull(pair.getSecond());

		assertTrue(cut.unlink(from.toReference(), to.toReference()));

		commitTransaction();

		assertFalse(cut.isLinked(from.toReference(), to.toReference(), "emf:references"));
	}

	@Test
	public void test_unlink_Single() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DefaultProperties.CREATED_BY, securityContextManager.getSystemUser());

		Pair<Serializable, Serializable> pair = cut.link(from, to, "emf:references", "emf:referencedBy", properties);
		assertNotNull(pair.getFirst());
		assertNotNull(pair.getSecond());

		assertTrue(cut.unlink(from.toReference(), to.toReference(), "emf:references", "emf:referencedBy"));

		commitTransaction();

		assertFalse(cut.isLinked(from.toReference(), to.toReference(), "emf:references"));
	}

	@Test
	public void test_removeLinkById() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DefaultProperties.CREATED_BY, securityContextManager.getSystemUser());

		Pair<Serializable, Serializable> pair = cut.link(from, to, "emf:references", "emf:referencedBy", properties);
		assertNotNull(pair.getFirst());
		assertNotNull(pair.getSecond());

		cut.removeLinkById(pair.getFirst());

		commitTransaction();

		assertFalse(cut.isLinked(from.toReference(), to.toReference(), "emf:references"));
	}

	@Test
	public void test_removeLink_reference() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DefaultProperties.CREATED_BY, securityContextManager.getSystemUser());

		Pair<Serializable, Serializable> pair = cut.link(from, to, "emf:references", "emf:referencedBy", properties);
		assertNotNull(pair.getFirst());
		assertNotNull(pair.getSecond());

		commitTransaction();

		LinkReference linkReference = cut.getLinkReference(pair.getFirst());
		assertNotNull(linkReference);

		cut.removeLink(linkReference);

		assertFalse(cut.isLinked(from.toReference(), to.toReference(), "emf:references"));
	}

	@Test
	public void test_removeLinksFor_all() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DefaultProperties.CREATED_BY, securityContextManager.getSystemUser());

		Pair<Serializable, Serializable> pair = cut.link(from, to, "emf:references", "emf:referencedBy", properties);
		assertNotNull(pair.getFirst());
		assertNotNull(pair.getSecond());

		assertTrue(cut.removeLinksFor(from.toReference()));

		commitTransaction();

		assertFalse(cut.isLinked(from.toReference(), to.toReference(), "emf:references"));
	}

	@Test
	public void test_removeLinksFor_single() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DefaultProperties.CREATED_BY, securityContextManager.getSystemUser());

		Pair<Serializable, Serializable> pair = cut.link(from, to, "emf:references", "emf:referencedBy", properties);
		assertNotNull(pair.getFirst());
		assertNotNull(pair.getSecond());

		assertTrue(cut.removeLinksFor(from.toReference(), new HashSet<>(Arrays.asList("emf:references"))));

		commitTransaction();

		assertFalse(cut.isLinked(from.toReference(), to.toReference(), "emf:references"));
	}

	@Test
	public void test_unlink_secondKnown_Simple() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		assertTrue(cut.linkSimple(from.toReference(), to.toReference(), "emf:references", "emf:referencedBy"));

		commitTransaction();

		cut.unlinkSimple(from.toReference(), to.toReference(), "emf:references");

		assertFalse(cut.isLinked(from.toReference(), to.toReference(), "emf:references"));
	}

	@Test
	public void test_unlink_secondUnknown_Simple() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		assertTrue(cut.linkSimple(from.toReference(), to.toReference(), "emf:references", "emf:referencedBy"));

		commitTransaction();

		cut.unlinkSimple(from.toReference(), "emf:references");

		assertFalse(cut.isLinked(from.toReference(), to.toReference(), "emf:references"));
	}

	@Test
	public void test_unlink_Simple_and_reversed() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		assertTrue(cut.linkSimple(from.toReference(), to.toReference(), "emf:references", "emf:referencedBy"));

		commitTransaction();

		cut.unlinkSimple(from.toReference(), to.toReference(), "emf:references", "emf:referencedBy");

		assertFalse(cut.isLinked(from.toReference(), to.toReference(), "emf:references"));
		assertFalse(cut.isLinked(to.toReference(), from.toReference(), "emf:referencedBy"));
	}

	@Test
	public void test_searchLinksFrom() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		assertTrue(cut.linkSimple(from.toReference(), to.toReference(), "emf:references", "emf:referencedBy"));

		commitTransaction();

		LinkSearchArguments arguments = new LinkSearchArguments();
		arguments.setFrom(from.toReference());
		arguments.setLinkId("emf:references");
		arguments.setPageNumber(1);
		arguments.setPageSize(10);
		arguments.setPermissionsType(QueryResultPermissionFilter.NONE);

		cut.searchLinks(arguments);

		List<LinkInstance> result = arguments.getResult();
		assertNotNull(result);
		assertFalse(result.isEmpty());

		LinkInstance instance = result.get(0);
		assertEquals(instance.getTo().getId(), to.getId());
	}

	@Test
	public void test_searchLinksTo() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		assertTrue(cut.linkSimple(from.toReference(), to.toReference(), "emf:references", "emf:referencedBy"));

		commitTransaction();

		LinkSearchArguments arguments = new LinkSearchArguments();
		arguments.setTo(to.toReference());
		arguments.setLinkId("emf:references");
		arguments.setPageNumber(1);
		arguments.setPageSize(10);
		arguments.setPermissionsType(QueryResultPermissionFilter.NONE);

		cut.searchLinks(arguments);

		List<LinkInstance> result = arguments.getResult();
		assertNotNull(result);
		assertFalse(result.isEmpty());

		LinkInstance instance = result.get(0);
		assertEquals(instance.getTo().getId(), to.getId());
	}

	@Test
	public void test_searchLinksFromTo() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		assertTrue(cut.linkSimple(from.toReference(), to.toReference(), "emf:references", "emf:referencedBy"));

		commitTransaction();

		LinkSearchArguments arguments = new LinkSearchArguments();
		arguments.setFrom(from.toReference());
		arguments.setTo(to.toReference());
		arguments.setLinkId("emf:references");
		arguments.setPageNumber(1);
		arguments.setPageSize(10);
		arguments.setPermissionsType(QueryResultPermissionFilter.NONE);

		cut.searchLinks(arguments);

		List<LinkInstance> result = arguments.getResult();
		assertNotNull(result);
		assertFalse(result.isEmpty());

		LinkInstance instance = result.get(0);
		assertEquals(instance.getTo().getId(), to.getId());
		assertEquals(instance.getFrom().getId(), from.getId());

		arguments = new LinkSearchArguments();
		arguments.setFrom(to.toReference());
		arguments.setTo(from.toReference());
		arguments.setLinkId("emf:referencedBy");
		arguments.setPageNumber(1);
		arguments.setPageSize(10);
		arguments.setPermissionsType(QueryResultPermissionFilter.NONE);

		cut.searchLinks(arguments);

		result = arguments.getResult();
		assertNotNull(result);
		assertFalse(result.isEmpty());

		instance = result.get(0);
		assertEquals(instance.getTo().getId(), from.getId());
		assertEquals(instance.getFrom().getId(), to.getId());
	}

	@Test
	public void test_updateLinkProperties() {
		Instance from = createInstance();
		Instance to = createInstance();

		dbDao.saveOrUpdate(from);
		dbDao.saveOrUpdate(to);

		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DefaultProperties.CREATED_BY, securityContextManager.getSystemUser());

		Pair<Serializable, Serializable> pair = cut.link(from, to, "emf:references", "emf:referencedBy", properties);
		assertNotNull(pair.getFirst());
		assertNotNull(pair.getSecond());

		properties.clear();
		properties.put(MODIFIED_ON, new Date());

		assertTrue(cut.updateLinkProperties(pair.getFirst(), properties));

		commitTransaction();

		LinkReference linkReference = cut.getLinkReference(pair.getFirst());
		assertTrue(linkReference.isPropertyPresent(MODIFIED_ON));
	}

	private Instance createInstance() {
		// create a new case instance
		Instance caseInstance = new ObjectInstance();
		// set its case definition
		// see test/resources/definitions/genericCaseDev.xml
		caseInstance.setIdentifier("genericCaseDev");
		caseInstance.add("stringPropertyName", "some value");
		InstanceTypeFake.setType(caseInstance, EMF.CASE.toString(), "caseinstance");
		idManager.generateStringId(caseInstance, true);
		return caseInstance;
	}

	@Override
	protected String getTestDataFile() {
		return "SemanticLinkService.ttl";
	}

}
