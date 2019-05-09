package com.sirma.itt.seip.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Entity;

/**
 * Test for {@link DbDaoWrapper}
 *
 * @author BBonev
 */
public class DbDaoWrapperTest {

	@Mock
	private DbDao primary;
	@Mock
	private VirtualDbDao secondary;

	private DbDao dao;

	@Mock
	private Entity<String> entity1;
	@Mock
	private Entity<String> entity2;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		dao = new DbDaoWrapper(primary, secondary);
	}

	@Test
	public void testSaveOrUpdateE() {
		when(primary.saveOrUpdate(any())).then(a -> a.getArgumentAt(0, Entity.class));
		Entity<String> result = dao.saveOrUpdate(entity1);
		assertNotNull(result);
		verify(primary).saveOrUpdate(entity1);
		verify(secondary).saveOrUpdate(entity1);
	}

	@Test
	public void testSaveOrUpdateEE() {
		when(primary.saveOrUpdate(any(), any())).then(a -> a.getArgumentAt(0, Entity.class));
		Entity<String> result = dao.saveOrUpdate(entity1, entity2);
		assertNotNull(result);
		verify(primary).saveOrUpdate(entity1, entity2);
		verify(secondary).saveOrUpdate(entity1, entity2);
	}

	@Test
	public void testFind() {
		dao.find(Entity.class, "id");
		verify(primary).find(Entity.class, "id");
		verify(secondary).find(Entity.class, "id", null);
	}

	@Test
	public void testFind_foundInPrimary() {
		when(primary.find(Entity.class, "id")).thenReturn(mock(Entity.class));
		when(secondary.find(any(), any(), any())).then(a -> a.getArgumentAt(2, Object.class));
		assertNotNull(dao.find(Entity.class, "id"));
		verify(primary).find(Entity.class, "id");
		verify(secondary).find(eq(Entity.class), eq("id"), any());
	}

	@Test
	public void testRefresh() {
		dao.refresh(entity1);
		verify(primary).refresh(entity1);
		verify(secondary).refresh(entity1);
	}

	@Test
	public void testSaveOrUpdateInNewTxE() {
		when(primary.saveOrUpdateInNewTx(any())).then(a -> a.getArgumentAt(0, Entity.class));
		Entity<String> result = dao.saveOrUpdateInNewTx(entity1);
		assertNotNull(result);
		verify(primary).saveOrUpdateInNewTx(entity1);
		verify(secondary).saveOrUpdateInNewTx(entity1);
	}

	@Test
	public void testDelete() {
		dao.delete(Entity.class, "id");
		verify(primary).delete(Entity.class, "id");
		verify(secondary).delete(Entity.class, "id");
	}

	@Test
	public void testFetchWithNamedStringListOfE() {
		when(primary.fetchWithNamed(anyString(), anyList())).thenReturn(Collections.singletonList("1"));
		when(secondary.fetchWithNamed(anyString(), anyList(), anyList())).thenReturn(Arrays.asList("1", "2"));
		List<Object> result = dao.fetchWithNamed("namedQuery", Collections.emptyList());
		assertNotNull(result);
		assertEquals(2, result.size());
		verify(primary).fetchWithNamed("namedQuery", Collections.emptyList());
		verify(secondary).fetchWithNamed("namedQuery", Collections.emptyList(), Collections.singletonList("1"));
	}

	@Test
	public void testFetchWithNamedStringListOfEIntInt() {
		when(primary.fetchWithNamed(anyString(), anyList(), anyInt(), anyInt())).thenReturn(Collections.singletonList("1"));
		when(secondary.fetchWithNamed(anyString(), anyList(), anyList(), anyInt(), anyInt())).thenReturn(Arrays.asList("1", "2"));
		List<Object> result = dao.fetchWithNamed("namedQuery", Collections.emptyList(), 0, 1);
		assertNotNull(result);
		assertEquals(2, result.size());
		verify(primary).fetchWithNamed("namedQuery", Collections.emptyList(), 0, 1);
		verify(secondary).fetchWithNamed("namedQuery", Collections.emptyList(), Collections.singletonList("1"), 0, 1);
	}

	@Test
	public void testFetchStringListOfE() {
		when(primary.fetch(anyString(), anyList())).thenReturn(Collections.singletonList("1"));
		when(secondary.fetch(anyString(), anyList(), anyList())).thenReturn(Arrays.asList("1", "2"));
		List<Object> result = dao.fetch("query", Collections.emptyList());
		assertNotNull(result);
		assertEquals(2, result.size());
		verify(primary).fetch("query", Collections.emptyList());
		verify(secondary).fetch("query", Collections.emptyList(), Collections.singletonList("1"));
	}

	@Test
	public void testFetchStringListOfEIntInt() {
		when(primary.fetch(anyString(), anyList(), anyInt(), anyInt())).thenReturn(Collections.singletonList("1"));
		when(secondary.fetch(anyString(), anyList(), anyList(), anyInt(), anyInt())).thenReturn(Arrays.asList("1", "2"));
		List<Object> result = dao.fetch("query", Collections.emptyList(), 0, 1);
		assertNotNull(result);
		assertEquals(2, result.size());
		verify(primary).fetch("query", Collections.emptyList(), 0, 1);
		verify(secondary).fetch("query", Collections.emptyList(), Collections.singletonList("1"), 0, 1);
	}

	@Test
	public void testExecuteUpdate() {
		dao.executeUpdate("namedQuery", Collections.emptyList());
		verify(primary).executeUpdate("namedQuery", Collections.emptyList());
		verify(secondary).executeUpdate("namedQuery", Collections.emptyList());
	}

	@Test
	public void testExecuteUpdateInNewTx() {
		dao.executeUpdateInNewTx("namedQuery", Collections.emptyList());
		verify(primary).executeUpdateInNewTx("namedQuery", Collections.emptyList());
		verify(secondary).executeUpdateInNewTx("namedQuery", Collections.emptyList());
	}

}
