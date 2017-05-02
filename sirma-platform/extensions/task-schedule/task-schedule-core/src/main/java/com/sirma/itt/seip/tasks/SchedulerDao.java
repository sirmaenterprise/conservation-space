package com.sirma.itt.seip.tasks;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.Triplet;
import com.sirma.itt.seip.db.CoreDb;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.exception.ContextNotActiveException;
import com.sirma.itt.seip.tasks.entity.EventTriggerEntity;
import com.sirma.itt.seip.tasks.entity.SchedulerEntity;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.tasks.entity.SchedulerEntity.QUERY_ALL_SCHEDULER_ENTRIES_BY_IDS_KEY;
import static com.sirma.itt.seip.tasks.entity.SchedulerEntity.QUERY_SCHEDULER_ENTRY_BY_UID_KEY;
import static com.sirma.itt.seip.tasks.entity.SchedulerEntity.QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_KEY;
import static com.sirma.itt.seip.tasks.entity.SchedulerEntity.QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP_KEY;
import static com.sirma.itt.seip.tasks.entity.SchedulerEntity.QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_USER_OP_KEY;
import static com.sirma.itt.seip.tasks.entity.SchedulerEntity.QUERY_SCHEDULER_TASKS_FOR_TIMED_EXECUTION_KEY;
import static com.sirma.itt.seip.tasks.entity.SchedulerEntity.QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP_AND_USER_OP_KEY;

/**
 * Provides direct database access for scheduler entity operations.
 *
 * @author BBonev
 */
class SchedulerDao {

	@Inject
	private DbDao dbDao;
	@Inject
	@CoreDb
	private DbDao coreDbDao;
	@Inject
	private SecurityContext securityContext;

	/**
	 * Find entities by primary key.
	 *
	 * @param ids
	 *            the list of primary ids to load
	 * @return the list of found entities
	 */
	List<SchedulerEntity> findEntitiesByPrimaryKey(Collection<Long> ids) {
		if (isEmpty(ids)) {
			return Collections.emptyList();
		}
		List<Pair<String, Object>> args = new ArrayList<>(1);
		args.add(new Pair<>("ids", ids));
		return getDbDao().fetchWithNamed(QUERY_ALL_SCHEDULER_ENTRIES_BY_IDS_KEY, args);
	}

	/**
	 * Find entities for the given event trigger.
	 *
	 * @param trigger
	 *            the trigger to use for the search
	 * @return the list of found entities or empty list if non are found.
	 */
	List<Long> findEntitiesForTrigger(EventTriggerEntity trigger) {
		if (trigger == null) {
			return Collections.emptyList();
		}
		List<Pair<String, Object>> args = new ArrayList<>(4);
		String namedQuery = QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_KEY;
		args.add(new Pair<String, Object>("eventClassId", trigger.getEventClassId()));
		args.add(new Pair<String, Object>("targetSemanticClass", trigger.getTargetSemanticClass()));
		args.add(new Pair<String, Object>("targetId", trigger.getTargetId()));
		String serverOperation = trigger.getServerOperation();
		String userOperation = trigger.getUserOperation();
		if (StringUtils.isNotBlank(serverOperation) && StringUtils.isNotBlank(userOperation)) {
			args.add(new Pair<String, Object>("serverOperation", serverOperation));
			args.add(new Pair<String, Object>("userOperation", userOperation));
			namedQuery = QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP_AND_USER_OP_KEY;
		} else if (StringUtils.isNotBlank(serverOperation)) {
			args.add(new Pair<String, Object>("serverOperation", serverOperation));
			namedQuery = QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP_KEY;
		} else if (StringUtils.isNotBlank(userOperation)) {
			args.add(new Pair<String, Object>("userOperation", userOperation));
			namedQuery = QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_USER_OP_KEY;
		}
		return getDbDao().fetchWithNamed(namedQuery, args);
	}

	/**
	 * Find entries that match the given identifier.
	 *
	 * @param identifier
	 *            the identifier
	 * @return the list of entities or empty list if non match
	 */
	List<SchedulerEntity> findEntriesForIdentifier(String identifier) {
		if (StringUtils.isBlank(identifier)) {
			return Collections.emptyList();
		}

		List<Pair<String, Object>> args = new ArrayList<>(1);
		args.add(new Pair<String, Object>("identifier", identifier));
		return getDbDao().fetchWithNamed(QUERY_SCHEDULER_ENTRY_BY_UID_KEY, args);
	}

	/**
	 * Gets tasks that need to be run before the given reference time
	 *
	 * @param types
	 *            will return only entities that match the given types. If empty non will be returned
	 * @param statuses
	 *            will return only entities that match the given statuses. if empty non will be returned
	 * @param referenceTime
	 *            the reference time to check for entities. Will return entities that have schedule time and that times
	 *            is before or equal the given date.
	 * @return the ids of the found tasks that match the given criteria
	 */
	Set<Long> getTasksForExecution(Collection<SchedulerEntryType> types, Collection<SchedulerEntryStatus> statuses,
			Date referenceTime) {
		if (isEmpty(types) || isEmpty(statuses) || referenceTime == null) {
			return Collections.emptySet();
		}
		List<Pair<String, Object>> args = new ArrayList<>(3);
		args.add(new Pair<>("type", types));
		args.add(new Pair<>("status", statuses));
		args.add(new Triplet<>("next", referenceTime, TemporalType.TIMESTAMP));
		List<Long> list = getDbDao().fetchWithNamed(QUERY_SCHEDULER_TASKS_FOR_TIMED_EXECUTION_KEY, args);
		return new LinkedHashSet<>(list);
	}

	private DbDao getDbDao() {
		if (!securityContext.isActive()) {
			throw new ContextNotActiveException();
		}
		if (securityContext.isSystemTenant()) {
			return coreDbDao;
		}
		return dbDao;
	}
}