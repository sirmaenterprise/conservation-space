package com.sirma.itt.seip.instance;

import static com.sirma.itt.seip.collections.CollectionUtils.toArray;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.testutil.EmfTest;

/**
 * Test for {@link SolrInstanceTypeResolver}
 *
 * @author BBonev
 */
public class SolrInstanceTypeResolverTest extends EmfTest {

	@InjectMocks
	private SolrInstanceTypeResolver typeResolver;

	@Mock
	private SearchService searchService;
	@Spy
	private TypeConverter typeConverter;
	@Mock
	private InstanceService instanceService;
	@Mock
	private InstanceTypes instanceTypes;
	@Mock
	private SemanticDefinitionService semanticDefinitionService;
	@Mock
	private DbDao dbDao;

	@Override
	@BeforeMethod
	public void beforeMethod() {
		typeConverter = createTypeConverter();
		super.beforeMethod();

		when(searchService.escapeForDialect(anyString())).thenReturn((s) -> s);
		when(instanceTypes.from(any(Serializable.class))).then(a -> {
			ClassInstance instance = new ClassInstance();
			instance.setId(a.getArgumentAt(0, Serializable.class));
			return Optional.of(instance.type());
		});
	}

	@Test
	public void test_resolveNotFound() {
		Optional<InstanceType> resolved = typeResolver.resolve((String) null);
		assertNotNull(resolved);
		assertFalse(resolved.isPresent());

		resolved = typeResolver.resolve("emf:instance");
		assertNotNull(resolved);
		assertFalse(resolved.isPresent());
	}

	@Test
	public void test_resolve() {
		mockSearchResult("emf:instance");
		Optional<InstanceType> type = typeResolver.resolve("emf:instance");
		assertNotNull(type);
		assertTrue(type.isPresent());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test_resolveFromCollectionOfTypes() {

		doAnswer(a -> {
			Instance instance = new EmfInstance();
			instance.setId("emf:instance");
			instance.add("rdfType", new ArrayList<>(Arrays.asList("emf:Case", "ptop:Activity")));
			a.getArgumentAt(1, SearchArguments.class).setResult(Arrays.asList(instance));
			return null;
		}).when(searchService).search(any(), any(SearchArguments.class));

		when(semanticDefinitionService.getMostConcreteClass(anyCollection()))
				.then(a -> a.getArgumentAt(0, Collection.class).iterator().next());

		Optional<InstanceType> type = typeResolver.resolve("emf:instance");
		assertNotNull(type);
		assertTrue(type.isPresent());
		verify(semanticDefinitionService).getMostConcreteClass(anyCollection());
	}

	@Test
	public void test_resolveReference() {
		mockSearchResult("emf:instance");
		Optional<InstanceReference> reference = typeResolver.resolveReference("emf:instance");
		assertNotNull(reference);
		assertTrue(reference.isPresent());
		assertEquals(reference.get().getId(), "emf:instance");
	}

	@Test
	public void test_resolveReferenceNotFound() {
		Optional<InstanceReference> reference = typeResolver.resolveReference(null);
		assertNotNull(reference);
		assertFalse(reference.isPresent());
		reference = typeResolver.resolveReference("emf:instance");
		assertNotNull(reference);
		assertFalse(reference.isPresent());
	}

	@Test
	public void test_resolveCollection() {
		mockSearchResult("emf:instance1", "emf:instance3");
		Map<Serializable, InstanceType> map = typeResolver
				.resolve(Arrays.asList("emf:instance1", "emf:instance2", "emf:instance3"));
		assertNotNull(map);
		assertTrue(map.containsKey("emf:instance1"));
		assertTrue(map.containsKey("emf:instance3"));
		assertFalse(map.containsKey("emf:instance2"));
	}

	@Test
	public void test_resolveCollection_invalid() {
		Map<Serializable, InstanceType> map = typeResolver.resolve((List<Serializable>) null);
		assertNotNull(map);
		assertTrue(map.isEmpty());
		map = typeResolver.resolve(Arrays.asList("emf:instance1", "emf:instance2", "emf:instance3"));
		assertNotNull(map);
		assertTrue(map.isEmpty());
	}

	@Test
	public void test_resolveReferences() {
		mockSearchResult("emf:instance1", "emf:instance3");
		Collection<InstanceReference> references = typeResolver
				.resolveReferences(Arrays.asList("emf:instance1", "emf:instance2", "emf:instance3"));
		assertNotNull(references);
		assertFalse(references.isEmpty());
		assertEquals(references.size(), 2);
	}

	@Test
	public void test_exists() {
		mockSearchResult("emf:instance1", "emf:instance3");
		Map<String, Boolean> exits = typeResolver
				.exist(Arrays.asList("emf:instance1", "emf:instance1", "emf:instance2", "emf:instance3"));
		assertNotNull(exits);
		assertFalse(exits.isEmpty());
		assertEquals(exits.size(), 3);
		assertTrue(exits.get("emf:instance1"));
		assertFalse(exits.get("emf:instance2"));
		assertTrue(exits.get("emf:instance3"));
	}

	@Test
	public void test_resolveReferencesNotFound() {
		Collection<InstanceReference> references = typeResolver.resolveReferences(null);
		assertNotNull(references);
		assertTrue(references.isEmpty());
		references = typeResolver.resolveReferences(Arrays.asList("emf:instance1", "emf:instance2", "emf:instance3"));
		assertNotNull(references);
		assertTrue(references.isEmpty());
	}

	@Test
	public void test_resolveInstances_notFound() {
		Collection<Instance> references = typeResolver.resolveInstances(null);
		assertNotNull(references);
		assertTrue(references.isEmpty());

		references = typeResolver.resolveInstances(new LinkedList<>());
		assertNotNull(references);
		assertTrue(references.isEmpty());

		references = typeResolver.resolveInstances(Arrays.asList("emf:instance1", "emf:instance2", "emf:instance3"));
		assertNotNull(references);
		assertTrue(references.isEmpty());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test_resolveInstances() {
		when(instanceService.loadByDbId(anyList()))
				.then(a -> buildSearchResult(toArray(a.getArgumentAt(0, Collection.class), String.class)));

		Collection<Instance> references = typeResolver
				.resolveInstances(Arrays.asList("emf:instance1", "emf:instance2", "emf:instance3"));
		assertNotNull(references);
		assertFalse(references.isEmpty());
		assertEquals(references.size(), 3);
	}

	@SuppressWarnings("unchecked")
	private void mockSearchResult(String... ids) {
		doAnswer(a -> {
			a.getArgumentAt(1, SearchArguments.class).setResult(buildSearchResult(ids));
			return null;
		}).when(searchService).search(any(), any(SearchArguments.class));
	}

	static List<Instance> buildSearchResult(String... ids) {
		List<Instance> list = new ArrayList<>(ids.length);
		for (Object id : ids) {
			Instance instance = new EmfInstance();
			instance.setId(id.toString());
			instance.add(SEMANTIC_TYPE, "emf:Case");
			list.add(instance);
		}
		return list;
	}

}
