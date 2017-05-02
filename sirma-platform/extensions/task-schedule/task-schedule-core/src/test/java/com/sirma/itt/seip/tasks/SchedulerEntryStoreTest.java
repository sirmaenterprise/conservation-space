package com.sirma.itt.seip.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.serialization.SerializationHelper;
import com.sirma.itt.seip.tasks.entity.EventTriggerEntity;
import com.sirma.itt.seip.tasks.entity.SchedulerEntity;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.fakes.EntityLookupCacheContextFake;

/**
 * Test for {@link SchedulerEntryStore}
 *
 * @author BBonev
 */
@SuppressWarnings("boxing")
public class SchedulerEntryStoreTest {

	private static final String TENANT = "testTenant";

	@InjectMocks
	private SchedulerEntryStore entryStore;

	@Mock
	private DbDao dbDao;
	@Mock
	private DbDao coreDbDao;
	@Spy
	private EntityLookupCacheContext cacheContext = EntityLookupCacheContextFake.createNoCache();
	@Mock
	private SecurityContext securityContext;
	@Mock
	private SchedulerDao schedulerDao;
	@Mock
	private SerializationHelper serializationHelper;

	@Mock
	private SchedulerConfiguration configuration;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(securityContext.getCurrentTenantId()).thenReturn(TENANT);
		when(securityContext.isActive()).thenReturn(Boolean.TRUE);

		entryStore.initialize();

		when(dbDao.saveOrUpdate(any())).then(a -> a.getArgumentAt(0, Entity.class));
		when(coreDbDao.saveOrUpdate(any())).then(a -> a.getArgumentAt(0, Entity.class));
	}

	@Test
	public void testPersist() throws Exception {
		SchedulerEntity entity = new SchedulerEntity();
		entity.setId(1L);
		entity.setTenantId(TENANT);

		entity = entryStore.persist(entity);
		assertNotNull(entity);
		assertEquals(TENANT, entity.getTenantId());
	}

	@Test
	public void testDelete() throws Exception {
		entryStore.delete(1L);
		verify(dbDao).delete(SchedulerEntity.class, 1L);
	}

	@Test
	public void testFindById() throws Exception {
		SchedulerEntity entity = entryStore.findById(1L);
		assertNull(entity);

		mockFindEntryById();

		entity = entryStore.findById(1L);
		assertNotNull(entity);
		assertEquals(TENANT, entity.getTenantId());
	}

	@Test
	public void testFindByIdentifier() throws Exception {
		assertNull(entryStore.findByIdentifier(null));

		mockFindEntityByIdentifier();

		SchedulerEntity entity = entryStore.findByIdentifier("test");
		assertNotNull(entity);
		assertEquals(TENANT, entity.getTenantId());
		assertEquals("test", entity.getIdentifier());
	}

	@Test
	public void testGetOrCreateEntityForIdentifier_nullId() throws Exception {
		SchedulerEntity entity = entryStore.getOrCreateEntityForIdentifier(null);
		assertNotNull(entity);
		assertNotNull(entity.getIdentifier());
		assertEquals(TENANT, entity.getTenantId());
	}

	@Test
	public void testGetOrCreateEntityForIdentifier_notFound() throws Exception {

		SchedulerEntity entity = entryStore.getOrCreateEntityForIdentifier("test");
		assertNotNull(entity);
		assertNotNull(entity.getIdentifier());
		assertEquals(TENANT, entity.getTenantId());
	}

	@Test
	public void testGetOrCreateEntityForIdentifier_found() throws Exception {
		mockFindEntityByIdentifier();

		SchedulerEntity entity = entryStore.getOrCreateEntityForIdentifier("test");
		assertNotNull(entity);
		assertNotNull(entity.getIdentifier());
		assertEquals(TENANT, entity.getTenantId());
	}

	@Test
	public void testGetOrCreateEntityForIdentifier_found_reactivate() throws Exception {
		when(schedulerDao.findEntriesForIdentifier(anyString())).then(a -> {
			SchedulerEntity schedulerEntity = new SchedulerEntity();
			schedulerEntity.setId(1L);
			schedulerEntity.setIdentifier(a.getArgumentAt(0, String.class));
			schedulerEntity.setStatus(SchedulerEntryStatus.COMPLETED);
			return Arrays.asList(schedulerEntity);
		});

		SchedulerEntity entity = entryStore.getOrCreateEntityForIdentifier("test");
		assertNotNull(entity);
		assertNotNull(entity.getIdentifier());
		assertEquals(TENANT, entity.getTenantId());
		assertEquals(SchedulerEntryStatus.PENDING, entity.getStatus());
	}

	@Test
	public void testFindEntities() throws Exception {
		when(schedulerDao.findEntitiesByPrimaryKey(anySet())).then(a -> {
			SchedulerEntity schedulerEntity = new SchedulerEntity();
			schedulerEntity.setId(1L);
			schedulerEntity.setIdentifier("test");
			return Arrays.asList(schedulerEntity);
		});

		List<SchedulerEntity> list = entryStore.findEntities(Arrays.asList(1L));
		assertNotNull(list);
		assertFalse(list.isEmpty());
		SchedulerEntity entity = list.get(0);
		assertEquals(TENANT, entity.getTenantId());
	}

	@Test
	public void testGetByEntryByEventTrigger() throws Exception {
		EventTriggerEntity triggerEntity = new EventTriggerEntity();
		when(schedulerDao.findEntitiesForTrigger(triggerEntity)).thenReturn(Arrays.asList(1L));
		SchedulerEntity entity = entryStore.getByEntryByEventTrigger(triggerEntity);
		assertNull(entity);

		mockFindEntryById();

		entity = entryStore.getByEntryByEventTrigger(triggerEntity);
		assertNotNull(entity);

		assertEquals(TENANT, entity.getTenantId());
	}

	@Test
	public void testSaveChanges_ForRemove() throws Exception {
		when(configuration.isRemoveOnSuccess()).thenReturn(Boolean.TRUE);

		SchedulerEntry entry = new SchedulerEntry();
		entry.setId(1L);
		entry.setStatus(SchedulerEntryStatus.COMPLETED);
		entry.setConfiguration(configuration);

		mockFindEntryById();

		SchedulerEntry oldEntry = entryStore.saveChanges(entry);
		assertNotNull(oldEntry);
		verify(dbDao).delete(SchedulerEntity.class, 1L);
	}

	@Test
	public void testSaveChanges_notFound() throws Exception {
		SchedulerEntry entry = new SchedulerEntry();
		entry.setId(1L);
		entry.setStatus(SchedulerEntryStatus.RUN_WITH_ERROR);
		entry.setConfiguration(configuration);

		assertNull(entryStore.saveChanges(entry));
		verify(dbDao, never()).saveOrUpdate(any());
		verify(dbDao, never()).delete(SchedulerEntity.class, 1L);
	}

	@Test
	public void testSaveChanges() throws Exception {
		when(configuration.isRemoveOnSuccess()).thenReturn(Boolean.TRUE);

		SchedulerEntry entry = new SchedulerEntry();
		entry.setId(1L);
		entry.setStatus(SchedulerEntryStatus.RUN_WITH_ERROR);
		entry.setConfiguration(configuration);

		mockFindEntryById();

		SchedulerEntry oldEntry = entryStore.saveChanges(entry);
		assertNotNull(oldEntry);
		verify(dbDao).saveOrUpdate(
				argThat(CustomMatcher.of((SchedulerEntity e) -> e.getStatus() == SchedulerEntryStatus.RUN_WITH_ERROR,
						"should have state copied")));
		verify(dbDao, never()).delete(SchedulerEntity.class, 1L);
	}

	@Test
	public void testSaveChanges_withRetries() throws Exception {
		when(configuration.isRemoveOnSuccess()).thenReturn(Boolean.TRUE);
		when(configuration.getRetryCount()).thenReturn(2);

		SchedulerEntry entry = new SchedulerEntry();
		entry.setId(1L);
		entry.setStatus(SchedulerEntryStatus.RUN_WITH_ERROR);
		entry.setConfiguration(configuration);

		mockFindEntryById();

		SchedulerEntry oldEntry = entryStore.saveChanges(entry);
		assertNotNull(oldEntry);
		verify(dbDao).saveOrUpdate(argThat(
				CustomMatcher.of((SchedulerEntity e) -> e.getRetries().intValue() == 2, "should have retries copied")));
		verify(dbDao, never()).delete(SchedulerEntity.class, 1L);
	}

	@Test
	public void testSaveChanges_withNewDate() throws Exception {
		when(configuration.isRemoveOnSuccess()).thenReturn(Boolean.TRUE);
		when(configuration.getNextScheduleTime()).thenReturn(new Date());

		SchedulerEntry entry = new SchedulerEntry();
		entry.setId(1L);
		entry.setStatus(SchedulerEntryStatus.RUN_WITH_ERROR);
		entry.setConfiguration(configuration);

		mockFindEntryById();

		SchedulerEntry oldEntry = entryStore.saveChanges(entry);
		assertNotNull(oldEntry);
		verify(dbDao).saveOrUpdate(argThat(CustomMatcher.of((SchedulerEntity e) -> e.getNextScheduleTime() != null,
				"should have new execution date")));
		verify(dbDao, never()).delete(SchedulerEntity.class, 1L);
	}

	private void mockFindEntryById() {
		when(dbDao.find(SchedulerEntity.class, 1L)).then(a -> {
			SchedulerEntity schedulerEntity = new SchedulerEntity();
			schedulerEntity.setId(1L);
			// because we cannot instantiate the context class in the SchedulerEntity
			// we simulate set of context data, capture the arguments and then return the same arguments back when
			// requested for deserialization
			HoldingConsumer<Object> consumer = new HoldingConsumer<>();
			when(serializationHelper.serialize(any())).then(aa -> {
				consumer.accept(aa.getArgumentAt(0, Object.class));
				return new byte[0];
			});
			schedulerEntity.setContextData(serializationHelper, configuration, new SchedulerContext());
			when(serializationHelper.deserialize(any())).then(aa -> consumer.value);
			return schedulerEntity;
		});
	}

	private void mockFindEntityByIdentifier() {
		when(schedulerDao.findEntriesForIdentifier(anyString())).then(a -> {
			SchedulerEntity schedulerEntity = new SchedulerEntity();
			schedulerEntity.setId(1L);
			schedulerEntity.setIdentifier(a.getArgumentAt(0, String.class));
			return Arrays.asList(schedulerEntity);
		});
	}

	static final class HoldingConsumer<T> implements Consumer<T> {

		/** The value. */
		Object value;

		@Override
		public void accept(T toHold) {
			this.value = toHold;
		}
	}
}
