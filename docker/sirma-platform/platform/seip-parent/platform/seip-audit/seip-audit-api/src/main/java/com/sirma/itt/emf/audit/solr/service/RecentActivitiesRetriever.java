package com.sirma.itt.emf.audit.solr.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

import com.sirma.itt.emf.audit.processor.StoredAuditActivitiesWrapper;
import com.sirma.itt.seip.time.DateRange;

/**
 * Provide functionality for retrieving activities from the recent activities core.
 *
 * @author nvelkov
 */
public interface RecentActivitiesRetriever {

	/**
	 * Retrieve all audit activities for the instances with the specified ids with offset and limit.
	 *
	 * @param ids
	 *            the ids of the instances
	 * @param offset
	 *            offset of returned activities
	 * @param limit
	 *            max number of returned activities
	 * @return the audit activities for the given instances including a total
	 */
	default StoredAuditActivitiesWrapper getActivities(Collection<Serializable> ids, int offset, int limit) {
		return getActivities(ids, offset, limit, Optional.empty());
	}

	/**
	 * Retrieve all audit activities for the instances with the specified ids with offset and limit. Supports filtering
	 * by {@link DateRange} by building additional filter that is added to the main query.
	 *
	 * @param ids
	 *            the ids of the instances
	 * @param offset
	 *            offset of returned activities
	 * @param limit
	 *            max number of returned activities
	 * @param range
	 *            optional date range for additional activities filtering, if present to the main query will be added
	 *            additional filter query for the passed range
	 * @return the audit activities for the given instances including a total
	 */
	StoredAuditActivitiesWrapper getActivities(Collection<Serializable> ids, int offset, int limit,
			Optional<DateRange> range);

	/**
	 * Retrieve all audit activities for the instances with the specified ids without offset and limit.
	 *
	 * @param ids
	 *            the ids of the instances
	 * @return the audit activities for the given instances including a total
	 */
	default StoredAuditActivitiesWrapper getActivities(Collection<Serializable> ids) {
		return getActivities(ids, 0, Integer.MAX_VALUE, Optional.empty());
	}

	/**
	 * Retrieve all audit activities for the instances with the specified ids without offset and limit. Supports
	 * filtering by {@link DateRange}.
	 *
	 * @param ids
	 *            the ids of the instances
	 * @param range
	 *            optional date range for additional activities filtering, if present to the main query will be added
	 *            additional filter query for the passed range
	 * @return the audit activities for the given instances including a total
	 */
	default StoredAuditActivitiesWrapper getActivities(Collection<Serializable> ids, Optional<DateRange> range) {
		return getActivities(ids, 0, Integer.MAX_VALUE, range);
	}

}
