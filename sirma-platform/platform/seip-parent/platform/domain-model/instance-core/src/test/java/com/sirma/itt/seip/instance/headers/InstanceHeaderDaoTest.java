package com.sirma.itt.seip.instance.headers;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;

/**
 * Test for {@link InstanceHeaderDao}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 24/11/2017
 */
public class InstanceHeaderDaoTest {

	@InjectMocks
	private InstanceHeaderDao headerDao;
	@Mock
	private DbDao dbDao;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void findByDefinitionId_shouldReturnTheFoundEntity() throws Exception {
		when(dbDao.fetchWithNamed(eq(HeaderEntity.QUERY_BY_DEFINITION_ID_KEY), anyList())).thenReturn(
				Collections.singletonList(new HeaderEntity()));

		Optional<HeaderEntity> headerEntity = headerDao.findByDefinitionId("someDefinitionId");
		assertNotNull(headerEntity);
		assertTrue(headerEntity.isPresent());
		ArgumentCaptor<List<Pair<String, Object>>> captor = ArgumentCaptor.forClass(List.class);
		verify(dbDao).fetchWithNamed(any(), captor.capture());
		List<Pair<String, Object>> value = captor.getValue();
		assertEquals(new Pair<>("definitionId", "someDefinitionId"), value.get(0));
		assertEquals(1, value.size());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void findByDefinitionId_shouldReturnNothingIfNotFoundEntity() throws Exception {
		when(dbDao.fetchWithNamed(eq(HeaderEntity.QUERY_BY_DEFINITION_ID_KEY), anyList())).thenReturn(
				Collections.emptyList());

		Optional<HeaderEntity> headerEntity = headerDao.findByDefinitionId("someDefinitionId");
		assertNotNull(headerEntity);
		assertFalse(headerEntity.isPresent());
		verify(dbDao).fetchWithNamed(any(), any());
	}

	@Test
	public void persist_shouldPersistEntity() throws Exception {
		headerDao.persist(new HeaderEntity());
		verify(dbDao).saveOrUpdate(any());
	}

}
