package com.sirma.itt.emf.audit.db;

import java.util.List;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.solr.query.ServiceResult;

/**
 * Provides an abstract interface to the audit log database.
 * 
 * @author Nikolay Velkov
 */
public interface AuditDao {

	String DATASOURCE_NAME = "java:jboss/datasources/bamDS";

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

}
