package com.sirma.itt.emf.audit.db;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.solr.query.ServiceResult;

/**
 * Provides an abstract interface to the audit log database.
 *
 * @author Nikolay Velkov
 */
public interface AuditDao {

	/**
	 * Persist an audit activity to the audit log database.
	 *
	 * @param activity
	 *            the activity
	 */
	void publish(AuditActivity activity);

	/**
	 * Retrieves the activities from the database by the given ids.
	 *
	 * @param ids
	 *            the ids of the activities to be retrieved
	 * @return the activities matching the ids from the database
	 */
	ServiceResult getActivitiesByIDs(List<Long> ids);

	/**
	 * Gets activities that are inserted after the given date.
	 * <p>
	 * The method will return at most the given number of unique request ids. Note that the actual results most likely
	 * will be more than the given limit. If no limit is specified then a default limit of {@code 50} will be used. <br>
	 * If <code>null</code> is passed for {@code lastKnownDate} then this will return entries starting from the first
	 * database record.
	 *
	 * @param lastKnownDate
	 *            the last known date, optional
	 * @param requestIdLimit
	 *            the request id limit, optional
	 * @return the activities after the given date sorted by {@link AuditActivity#getDateReceived()}
	 */
	Collection<AuditActivity> getActivitiesAfter(Date lastKnownDate, Integer requestIdLimit);

}
