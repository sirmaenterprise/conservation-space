package com.sirma.itt.seip.tasks;

import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.exception.ContextNotActiveException;
import com.sirma.itt.seip.tasks.entity.EventTriggerEntity;
import com.sirma.itt.seip.tasks.entity.SchedulerEntity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.sirma.itt.seip.tasks.entity.SchedulerEntity.QUERY_ALL_SCHEDULER_ENTRIES_BY_IDS_KEY;
import static com.sirma.itt.seip.tasks.entity.SchedulerEntity.QUERY_SCHEDULER_ENTRY_BY_UID_KEY;
import static com.sirma.itt.seip.tasks.entity.SchedulerEntity.QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_KEY;
import static com.sirma.itt.seip.tasks.entity.SchedulerEntity.QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP_AND_USER_OP_KEY;
import static com.sirma.itt.seip.tasks.entity.SchedulerEntity.QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP_KEY;
import static com.sirma.itt.seip.tasks.entity.SchedulerEntity.QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_USER_OP_KEY;
import static com.sirma.itt.seip.tasks.entity.SchedulerEntity.QUERY_SCHEDULER_TASKS_FOR_TIMED_EXECUTION_KEY;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Test for {@link SchedulerDao}
 *
 * @author BBonev
 */
@SuppressWarnings("boxing")
public class SchedulerDaoTest {
	@InjectMocks
	private SchedulerDao schedulerDao;

	@Mock
	private DbDao dbDao;
	@Mock
	private DbDao coreDbDao;
	@Mock
	private SecurityContext securityContext;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(securityContext.isActive()).thenReturn(Boolean.TRUE);
		when(securityContext.getCurrentTenantId()).thenReturn("tenant");
	}

	@Test
	public void withEmptyData() throws Exception {
		assertNonNullEmptyCollection(schedulerDao.findEntitiesByPrimaryKey(null));
		assertNonNullEmptyCollection(schedulerDao.findEntitiesByPrimaryKey(Collections.emptyList()));

		assertNonNullEmptyCollection(schedulerDao.findEntitiesForTrigger(null));

		assertNonNullEmptyCollection(schedulerDao.findEntriesForIdentifier(null));

		assertNonNullEmptyCollection(schedulerDao.getTasksForExecution(null, null, null));
		assertNonNullEmptyCollection(schedulerDao.getTasksForExecution(Collections.emptySet(),
				EnumSet.of(SchedulerEntryStatus.FAILED), new Date()));
		assertNonNullEmptyCollection(schedulerDao.getTasksForExecution(EnumSet.of(SchedulerEntryType.CRON),
				Collections.emptySet(), new Date()));
		assertNonNullEmptyCollection(schedulerDao.getTasksForExecution(EnumSet.of(SchedulerEntryType.CRON),
				EnumSet.of(SchedulerEntryStatus.FAILED), null));
	}

	@Test
	public void findEntitiesByPrimaryKey() throws Exception {
		mockQueryResult(QUERY_ALL_SCHEDULER_ENTRIES_BY_IDS_KEY, Arrays.asList(new SchedulerEntity()));
		List<SchedulerEntity> list = schedulerDao.findEntitiesByPrimaryKey(Arrays.asList(1L));
		assertNotNull(list);
		assertFalse(list.isEmpty());
	}

	@Test
	public void findEntitiesForTrigger() throws Exception {
		mockQueryResult(QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_KEY, Arrays.asList(1L));
		List<Long> list = schedulerDao.findEntitiesForTrigger(new EventTriggerEntity());
		assertNotNull(list);
		assertFalse(list.isEmpty());
	}

	@Test
	public void findEntitiesForTriggerForOperation() throws Exception {
		mockQueryResult(QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP_KEY, Arrays.asList(1L));
		EventTriggerEntity trigger = new EventTriggerEntity();
		trigger.setServerOperation("operationId");
		List<Long> list = schedulerDao.findEntitiesForTrigger(trigger);
		assertNotNull(list);
		assertFalse(list.isEmpty());
	}

	@Test
	public void findEntitiesForTriggerForUserOperation() throws Exception {
		mockQueryResult(QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_USER_OP_KEY, Arrays.asList(1L));
		EventTriggerEntity trigger = new EventTriggerEntity();
		trigger.setUserOperation("userOperationId");
		List<Long> list = schedulerDao.findEntitiesForTrigger(trigger);
		assertNotNull(list);
		assertFalse(list.isEmpty());
	}

	@Test
	public void findEntitiesForTriggerForServerOperationAndUserOperation() throws Exception {
		mockQueryResult(QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP_AND_USER_OP_KEY, Arrays.asList(1L));
		EventTriggerEntity trigger = new EventTriggerEntity();
		trigger.setUserOperation("userOperationId");
		trigger.setServerOperation("operationId");
		List<Long> list = schedulerDao.findEntitiesForTrigger(trigger);
		assertNotNull(list);
		assertFalse(list.isEmpty());
	}

	@Test
	public void findEntriesForIdentifier() throws Exception {
		mockQueryResult(QUERY_SCHEDULER_ENTRY_BY_UID_KEY, Arrays.asList(new SchedulerEntity()));
		List<SchedulerEntity> list = schedulerDao.findEntriesForIdentifier("testId");
		assertNotNull(list);
		assertFalse(list.isEmpty());
	}

	@Test
	public void getTasksForExecution() throws Exception {
		mockQueryResult(QUERY_SCHEDULER_TASKS_FOR_TIMED_EXECUTION_KEY, Arrays.asList(1L));
		Set<Long> list = schedulerDao.getTasksForExecution(EnumSet.of(SchedulerEntryType.CRON),
				EnumSet.of(SchedulerEntryStatus.FAILED), new Date());
		assertNotNull(list);
		assertFalse(list.isEmpty());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void systemTenant() throws Exception {
		reset(securityContext);
		when(securityContext.isActive()).thenReturn(Boolean.TRUE);
		when(securityContext.isSystemTenant()).thenReturn(Boolean.TRUE);

		when(coreDbDao.fetchWithNamed(eq(QUERY_ALL_SCHEDULER_ENTRIES_BY_IDS_KEY), anyList()))
				.thenReturn(Arrays.asList(new SchedulerEntity()));

		List<SchedulerEntity> list = schedulerDao.findEntitiesByPrimaryKey(Arrays.asList(1L));
		assertNotNull(list);
		assertFalse(list.isEmpty());
	}

	@Test(expected = ContextNotActiveException.class)
	public void invalidSecurityContext() throws Exception {
		reset(securityContext);

		schedulerDao.findEntitiesByPrimaryKey(Arrays.asList(1L));
	}

	@SuppressWarnings("unchecked")
	private void mockQueryResult(String query, List<?> result) {
		when(dbDao.fetchWithNamed(eq(query), anyList())).thenReturn(result);
	}

	private static void assertNonNullEmptyCollection(Collection<?> c) {
		assertNotNull(c);
		assertTrue(c.isEmpty());
	}
}
