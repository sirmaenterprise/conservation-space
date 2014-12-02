package com.sirma.itt.emf.audit.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.solr.query.ServiceResult;
import com.sirma.itt.emf.time.TimeTracker;

/**
 * Data access object for the audit log database. Persists and retrieves activities.
 * 
 * @author Mihail Radkov
 * @author Vilizar Tsonev
 */
public class AuditDaoImpl implements AuditDao {

	/** Logs actions related to this class. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AuditDaoImpl.class);

	@PersistenceContext(unitName = "seip-auditlog")
	private EntityManager em;

	@Override
	public void publish(AuditActivity activity) {
		if (activity != null) {
			TimeTracker tracker = TimeTracker.createAndStart();
			em.persist(activity);
			LOGGER.debug("EMF activity persisted in {} ms", tracker.stop());
		}
	}

	@Override
	public ServiceResult getActivitiesByIDs(List<Long> ids) {
		ServiceResult result = new ServiceResult();

		if (!CollectionUtils.isEmpty(ids)) {
			TimeTracker tracker = TimeTracker.createAndStart();

			TypedQuery<AuditActivity> query = em.createQuery(AuditQueries.AUDIT_SELECT,
					AuditActivity.class);
			// TODO: Remove null ids!?
			query.setParameter("ids", ids);

			List<AuditActivity> activities = query.getResultList();
			List<AuditActivity> ordered = order(ids, activities);

			result.setRecords(ordered);
			result.setTotal(ids.size());

			LOGGER.debug("EMF activities selected and ordered in {} ms", tracker.stop());
			return result;
		}

		result.setRecords(new ArrayList<AuditActivity>());
		result.setTotal(0);
		return result;
	}

	/**
	 * Sorts given {@link List} of {@link AuditActivity} in specific order. The order is provided as
	 * {@link List} of IDs. Because the database returns results in a different order, we need to
	 * preserve the original one with this transformation.
	 * 
	 * @param ids
	 *            the original order
	 * @param activities
	 *            the result from the database
	 * @return ordered {@link List} of {@link AuditActivity}
	 */
	private List<AuditActivity> order(List<Long> ids, List<AuditActivity> activities) {
		Map<Long, AuditActivity> map = com.sirma.itt.emf.util.CollectionUtils
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
				LOGGER.debug("Mismatch between RDB and Solr index detected. No record for ID={}",
						id);
			}
		}
		return ordered;
	}
}
