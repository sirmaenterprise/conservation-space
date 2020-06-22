package com.sirma.itt.seip.db;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.testutil.fakes.EntityLookupCacheContextFake;

/**
 * Test for {@link DbDaoWrapper}
 *
 * @author BBonev
 */
public class VirtualDbDaoIntegrationTest {

	@Mock
	private DbDao primary;
	@InjectMocks
	private VirtualDbDao secondary;

	private DbDao dao;

	@Spy
	private EntityLookupCacheContextFake cacheContext = EntityLookupCacheContextFake.createInMemory();

	private List<VirtualDbQueryParser> queryParsers = new LinkedList<>();
	@Spy
	private Plugins<VirtualDbQueryParser> parsers = new Plugins<>("", queryParsers);
	@Mock
	private VirtualDbQueryParser queryParser;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		cacheContext.reset();
		secondary.initialize();

		dao = new DbDaoWrapper(primary, secondary);

		queryParsers.clear();
		queryParsers.add(queryParser);
		when(queryParser.parse(any(), any())).then(a -> {
			List<Pair<String, Object>> params = a.getArgumentAt(1, List.class);
			return params.stream().filter(pair -> nullSafeEquals(pair.getFirst(), "ids")).findAny().map(
					Pair::getSecond);
		});
		when(queryParser.parseNamed(any(), any())).then(a -> {
			List<Pair<String, Object>> params = a.getArgumentAt(1, List.class);
			return params.stream().filter(pair -> nullSafeEquals(pair.getFirst(), "ids")).findAny().map(
					Pair::getSecond);
		});

	}

	@Test
	public void shouldReturnFromDatabaseEventIfPresentInCache() {
		withDatabase("1");
		withCache("1");
		Instance instance = dao.find(Instance.class, "1");
		assertNotNull(instance);
		verifyFromDatabase(instance);
	}

	@Test
	public void shouldReturnFromCacheWhenInstanceIsNotPresentInDatabase() {
		withCache("1");
		Instance instance = dao.find(Instance.class, "1");
		assertNotNull(instance);
		verifyFromCache(instance);
	}

	@Test
	public void fetchWithNamed_shouldMergeFromDatabaseAndCache_withDatabaseAsFirstPriority() {
		withDatabase("1", "2", "3");
		withCache("2", "3", "4", "5");
		List<Instance> result = dao.fetchWithNamed("query", params("1", "2", "3", "4", "5"));
		assertNotNull(result);
		assertEquals(5, result.size());
		verify(result, this::verifyFromDatabase, "1", "2", "3");
		verify(result, this::verifyFromCache, "4", "5");
	}

	@Test
	public void fetch_shouldMergeFromDatabaseAndCache_withDatabaseAsFirstPriority() {
		withDatabase("1", "2", "3");
		withCache("2", "3", "4", "5");
		List<Instance> result = dao.fetch("query", params("1", "2", "3", "4", "5"));
		assertNotNull(result);
		assertEquals(5, result.size());
		verify(result, this::verifyFromDatabase, "1", "2", "3");
		verify(result, this::verifyFromCache, "4", "5");
	}

	@Test
	public void fetchWithNamedAndLimit_shouldMergeFromDatabaseAndCache_withDatabaseAsFirstPriority() {
		withDatabase("1", "2", "3");
		withCache("2", "3", "4", "5");
		List<Instance> result = dao.fetchWithNamed("query", params("1", "2", "3", "4", "5"), 0, 10);
		assertNotNull(result);
		assertEquals(5, result.size());
		verify(result, this::verifyFromDatabase, "1", "2", "3");
		verify(result, this::verifyFromCache, "4", "5");
	}

	@Test
	public void fetchAndLimit_shouldMergeFromDatabaseAndCache_withDatabaseAsFirstPriority() {
		withDatabase("1", "2", "3");
		withCache("2", "3", "4", "5");
		List<Instance> result = dao.fetch("query", params("1", "2", "3", "4", "5"), 0, 10);
		assertNotNull(result);
		assertEquals(5, result.size());
		verify(result, this::verifyFromDatabase, "1", "2", "3");
		verify(result, this::verifyFromCache, "4", "5");
	}

	@Test
	public void fetchWithNative_shouldMergeFromDatabaseAndCache_withDatabaseAsFirstPriority() {
		withDatabase("1", "2", "3");
		withCache("2", "3", "4", "5");
		List<Instance> result = dao.fetchWithNative("query", params("1", "2", "3", "4", "5"));
		assertNotNull(result);
		assertEquals(5, result.size());
		verify(result, this::verifyFromDatabase, "1", "2", "3");
		verify(result, this::verifyFromCache, "4", "5");
	}

	private List<Pair<String, Object>> params(String... ids) {
		return Collections.singletonList(new Pair<>("ids", Arrays.asList(ids)));
	}

	private void withDatabase(String id) {
		EmfInstance instance = new EmfInstance(id);
		instance.add("inDatabase", true);
		when(primary.find(any(), eq(id))).thenReturn(instance);
		withDatabase(new String[] { id });
	}
	@SuppressWarnings("unchecked")
	private void withDatabase(String... ids) {
		List<EmfInstance> entities = Arrays.stream(ids)
				.map(EmfInstance::new)
				.peek(instance -> instance.add("inDatabase", true))
				.collect(Collectors.toList());
		when(primary.fetch(anyString(), anyList())).thenReturn(entities);
		when(primary.fetchWithNamed(anyString(), anyList())).thenReturn(entities);
		when(primary.fetch(anyString(), anyList(), anyInt(), anyInt())).thenReturn(entities);
		when(primary.fetchWithNamed(anyString(), anyList(), anyInt(), anyInt())).thenReturn(entities);
		when(primary.fetchWithNative(anyString(), anyList())).thenReturn(entities);
	}

	private void withCache(String... ids) {
		for (String id: ids) {
			EmfInstance instance = new EmfInstance(id);
			instance.add("isFromCache", true);
			cacheContext.getCache(VirtualDbDao.VIRTUAL_STORE_CACHE).setValue(id, instance);
		}
	}

	private void verify(List<Instance> instances, Consumer<Instance> verifier, String... ids) {
		Map<Serializable, Instance> mapping = instances.stream()
				.collect(Collectors.toMap(Instance::getId, Function.identity()));
		for (String id: ids) {
			Instance instance = mapping.get(id);
			assertNotNull("Instance " + id + " should have been found", instance);
			verifier.accept(instance);
		}
	}

	private void verifyFromDatabase(Instance instance) {
		assertTrue("The instance " + instance.getId() + " should be found in the database", instance.getBoolean("inDatabase", false));
	}

	private void verifyFromCache(Instance instance) {
		assertTrue("The instance " + instance.getId() + " should be found in the cache", instance.getBoolean("isFromCache", false));
	}

}
