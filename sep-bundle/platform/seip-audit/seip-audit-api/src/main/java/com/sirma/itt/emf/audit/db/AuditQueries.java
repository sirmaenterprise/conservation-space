package com.sirma.itt.emf.audit.db;

/**
 * Queries related to the audit log.
 * 
 * @author Mihail Radkov
 */
public interface AuditQueries {

	/** Key to {@link #AUDIT_SELECT} */
	String AUDIT_SELECT_KEY = "AUDIT_SELECT";

	/**
	 * Query for selecting activities by their IDs.
	 */
	String AUDIT_SELECT = "select aa from AuditActivity aa where aa.id in (:ids)";
}
