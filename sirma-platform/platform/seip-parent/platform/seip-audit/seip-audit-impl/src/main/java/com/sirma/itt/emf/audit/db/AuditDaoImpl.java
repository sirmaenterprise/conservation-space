package com.sirma.itt.emf.audit.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.solr.query.ServiceResult;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Data access object for the audit log database. Persists and retrieves activities.
 *
 * @author Mihail Radkov
 * @author Vilizar Tsonev
 */
public class AuditDaoImpl implements AuditDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuditDaoImpl.class);

	@Inject
	@AuditDb
	private DbDao dbDao;

	@Override
	public void publish(AuditActivity activity) {
		if (activity != null) {
			TimeTracker tracker = TimeTracker.createAndStart();
			dbDao.saveOrUpdate(activity);
			LOGGER.debug("EMF activity persisted in {} ms", tracker.stop());
		}
	}

	@Override
	public ServiceResult getActivitiesByIDs(List<Long> ids) {
		ServiceResult result = new ServiceResult();

		if (!CollectionUtils.isEmpty(ids)) {
			TimeTracker tracker = TimeTracker.createAndStart();

			// TODO: Remove null ids!?

			List<AuditActivity> activities = dbDao.fetchWithNamed(AuditActivity.AUDIT_SELECT_KEY,
					Collections.singletonList(new Pair<>("ids", ids)));
			List<AuditActivity> ordered = order(ids, activities);

			result.setRecords(ordered);
			result.setTotal(ordered.size());

			LOGGER.debug("EMF activities selected and ordered in {} ms", tracker.stop());
			return result;
		}

		result.setRecords(new ArrayList<AuditActivity>(0));
		result.setTotal(0);
		return result;
	}

	@Override
	public Collection<AuditActivity> getActivitiesAfter(Date lastKnownDate, Integer requestIdLimit) {
		long limit = requestIdLimit == null ? 50 : requestIdLimit.longValue();
		// if the date is not specified then use the beginning of known time
		Date lastDate = lastKnownDate == null ? new Date(0L) : lastKnownDate;
		return dbDao.fetchWithNamed(AuditActivity.GET_ACTIVITIES_AFTER_KEY, Arrays.asList(
				new Pair<>("lastProcessedDate" , lastDate),
				new Pair<>("requestIdLimit", limit),
				new Pair<>("lastKnownDate", lastDate.getTime())));
	}

	/**
	 * Sorts given {@link List} of {@link AuditActivity} in specific order. The order is provided as {@link List} of
	 * IDs. Because the database returns results in a different order, we need to preserve the original one with this
	 * transformation.
	 *
	 * @param ids
	 *            the original order
	 * @param activities
	 *            the result from the database
	 * @return ordered {@link List} of {@link AuditActivity}
	 */
	private static List<AuditActivity> order(List<Long> ids, List<AuditActivity> activities) {
		Map<Long, AuditActivity> map = com.sirma.itt.seip.collections.CollectionUtils
				.createLinkedHashMap(activities.size());
		List<AuditActivity> ordered = new ArrayList<>(activities.size());

		for (AuditActivity activity : activities) {
			map.put(activity.getId(), activity);
		}

		for (Long id : ids) {
			AuditActivity activity = map.get(id);
			if (activity != null) {
				ordered.add(activity);
			} else {
				LOGGER.debug("Mismatch between RDB and Solr index detected. No record for ID={}", id);
			}
		}
		return ordered;
	}
}
