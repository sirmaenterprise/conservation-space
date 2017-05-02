/**
 *
 */
package com.sirma.itt.emf.audit.activity;

import java.util.List;

/**
 * Allows retrieval of recorded activities.
 *
 * @author Nikolay Velkov
 */
public interface AuditActivityRetriever {

	/**
	 * Retrieves Audit activities by a provided criteria.
	 *
	 * @param activityCriteria
	 *            the provided criteria
	 * @return a list of {@link AuditActivity}. If the list is empty then no activity is present for the criteria
	 */
	List<AuditActivity> getActivities(AuditActivityCriteria activityCriteria);
}
