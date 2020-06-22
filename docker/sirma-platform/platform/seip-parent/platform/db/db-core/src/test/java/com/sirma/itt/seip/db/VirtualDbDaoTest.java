package com.sirma.itt.seip.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.testutil.fakes.EntityLookupCacheContextFake;

/**
 * Test for {@link VirtualDbDao}
 *
 * @author BBonev
 */
public class VirtualDbDaoTest {

	@InjectMocks
	private VirtualDbDao dao;

	@Spy
	private EntityLookupCacheContextFake cacheContext = EntityLookupCacheContextFake.createInMemory();

	@Mock
	private VirtualDbQueryParser parser;

	private List<VirtualDbQueryParser> plugins = new ArrayList<>();
	@Spy
	private Plugins<VirtualDbQueryParser> parsers = new Plugins<>("", plugins);

	EmfInstance instance = new EmfInstance();

	@Before
	@SuppressWarnings("unchecked")
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		plugins.clear();
		plugins.add(parser);

		dao.initialize();

		when(parser.parse(eq("query"), anyList())).thenReturn(Optional.of(Arrays.asList("1", "2")));
		when(parser.parse(eq("queryNoData"), anyList())).thenReturn(Optional.empty());
		when(parser.parseNamed(eq("namedQuery"), anyList())).thenReturn(Optional.of(Arrays.asList("1", "2")));
		when(parser.parseNamed(eq("namedQueryNoData"), anyList())).thenReturn(Optional.empty());

		instance.setId("1");
		cacheContext.resetCacheData();
	}

	@Test
	public void testSaveOrUpdateE() {
		assertNull(dao.saveOrUpdate(null));
		assertNotNull(dao.saveOrUpdate(new EmfInstance()));

		EmfInstance result = dao.saveOrUpdate(instance);
		assertNotNull(result);
		verifyInCache("1");
	}

	private void verifyInCache(String key) {
		assertNotNull("Should be a value in the cache for key " + key,
				cacheContext.getCache(VirtualDbDao.VIRTUAL_STORE_CACHE).getByKey(key));
	}

	private void verifyNotInCache(String key) {
		assertNull("No value should be in the cache for key " + key,
				cacheContext.getCache(VirtualDbDao.VIRTUAL_STORE_CACHE).getByKey(key));
	}

	@Test
	public void testSaveOrUpdateEE() {
		EmfInstance result = dao.saveOrUpdate(instance, instance);
		assertNotNull(result);
		verifyInCache("1");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testFind() {
		verifyNotInCache("1");

		assertNull(dao.find(Entity.class, null));
		assertNull(dao.find(Entity.class, "1"));

		dao.saveOrUpdate(instance);

		assertNotNull(dao.find(Entity.class, "1"));
		assertNotNull(dao.find(Instance.class, "1"));
		assertNull(dao.find(CommonInstance.class, "1"));
	}
	@Test(expected = NullPointerException.class)
	public void testFind_invalid() {
		assertNull(dao.find(null, "1"));
	}

	@Test
	public void testRefresh() {
		assertNotNull(dao.refresh(instance));
	}

	@Test
	public void testFetchWithNamedStringListOfE() {
		dao.saveOrUpdate(instance);

		List<Object> result = dao.fetchWithNamed("namedQuery", Collections.emptyList());
		assertNotNull(result);
		assertEquals("Expected a result from the service ", 1, result.size());
		assertEquals(instance, result.get(0));

		result = dao.fetchWithNamed("namedQueryNoData", Collections.emptyList());
		assertNotNull(result);
		assertEquals("Didn't expect a result from the service ", 0, result.size());
	}

	@Test
	public void testFetchWithNamedStringListOfEIntInt() {
		dao.saveOrUpdate(instance);

		List<Object> result = dao.fetchWithNamed("namedQuery", Collections.emptyList(), 0, 1);
		assertNotNull(result);
		assertEquals("Expected a result from the service ", 1, result.size());
		assertEquals(instance, result.get(0));

		result = dao.fetchWithNamed("namedQueryNoData", Collections.emptyList(), 0, 1);
		assertNotNull(result);
		assertEquals("Didn't expect a result from the service ", 0, result.size());
	}

	@Test
	public void testFetchStringListOfE() {
		dao.saveOrUpdate(instance);

		List<Object> result = dao.fetch("query", Collections.emptyList());
		assertNotNull(result);
		assertEquals("Expected a result from the service ", 1, result.size());
		assertEquals(instance, result.get(0));

		result = dao.fetch("queryNoData", Collections.emptyList());
		assertNotNull(result);
		assertEquals("Didn't expect a result from the service ", 0, result.size());
	}

	@Test
	public void testFetchStringListOfEIntInt() {
		dao.saveOrUpdate(instance);

		List<Object> result = dao.fetch("query", Collections.emptyList(), 0, 1);
		assertNotNull(result);
		assertEquals("Expected a result from the service ", 1, result.size());
		assertEquals(instance, result.get(0));

		result = dao.fetch("queryNoData", Collections.emptyList(), 0, 1);
		assertNotNull(result);
		assertEquals("Didn't expect a result from the service ", 0, result.size());
	}

	@Test
	public void testExecuteUpdate() {
		assertEquals(0, dao.executeUpdate("", Collections.emptyList()));
	}

	@Test
	public void testSaveOrUpdateInNewTxE() {
		assertNull(dao.saveOrUpdateInNewTx(null));
		assertNotNull(dao.saveOrUpdateInNewTx(new EmfInstance()));

		EmfInstance result = dao.saveOrUpdateInNewTx(instance);
		assertNotNull(result);
		verifyInCache("1");
	}

	@Test
	public void testExecuteUpdateInNewTx() {
		assertEquals(0, dao.executeUpdate("", Collections.emptyList()));
	}

	@Test
	public void testDelete() {
		dao.delete(null, null);
		dao.delete(Instance.class, null);

		dao.saveOrUpdate(instance);
		verifyInCache("1");

		dao.delete(Instance.class, "1");
		verifyNotInCache("1");
	}

}
