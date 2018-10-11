package com.sirma.itt.seip.definition;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.model.DataType;
import com.sirma.itt.seip.testutil.fakes.EntityLookupCacheContextFake;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;

/**
 * Test for {@link MutableDefinitionServiceImpl}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 07/12/2017
 */
public class MutableDefinitionServiceImplTest {
	@InjectMocks
	private MutableDefinitionServiceImpl definitionService;

	@Mock
	protected DbDao dbDao;
	@Spy
	private EntityLookupCacheContext cacheContext = EntityLookupCacheContextFake.createNoCache();
	@Spy
	protected javax.enterprise.inject.Instance<DefinitionAccessor> accessors = new InstanceProxyMock<>();

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		definitionService.init();
		when(dbDao.saveOrUpdate(any())).then(a -> {
			Entity<Long> entity = a.getArgumentAt(0, Entity.class);
			if (entity.getId() == null) {
				entity.setId(1L);
			}
			return entity;
		});
	}

	@Test
	public void saveDataTypeDefinition_shouldPersistNewDataTypeDefinition() throws Exception {
		DataType definition = new DataType();
		definition.setName("testType");
		DataTypeDefinition result = definitionService.saveDataTypeDefinition(definition);
		assertNotNull(result);

		assertEquals(Long.valueOf(1L), result.getId());
	}

	@Test
	public void saveDataTypeDefinition_shouldOverrideExistingDataTypeDefinition() throws Exception {
		DataType existing = new DataType();
		existing.setName("testType");
		existing.setUri("emf:test");
		existing.setId(2L);
		when(dbDao.fetchWithNamed(eq(DataType.QUERY_TYPE_DEFINITION_KEY), anyList())).thenReturn(Collections.singletonList(existing));

		DataType definition = new DataType();
		definition.setName("testType");
		definition.setUri("emf:newTest");
		DataTypeDefinition result = definitionService.saveDataTypeDefinition(definition);
		assertNotNull(result);

		assertEquals(Long.valueOf(2L), result.getId());
		assertEquals(Collections.singleton("emf:newTest"), result.getUries());

	}
}
