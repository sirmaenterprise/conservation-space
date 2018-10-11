package com.sirma.itt.emf.audit.solr.query;

import java.util.List;

import com.sirma.itt.emf.audit.activity.AuditActivity;

/**
 * The wrapper object that will be returned to the client from the rest service.
 *
 * @author Nikolay Velkov
 */
public class ServiceResult {

	/** The total. */
	private long total;

	/** The audit records retrieved from the rdb. */
	private List<AuditActivity> records;

	/**
	 * Gets the total.
	 *
	 * @return the total
	 */
	public long getTotal() {
		return total;
	}

	/**
	 * Sets the total.
	 *
	 * @param total
	 *            the new total
	 */
	public void setTotal(long total) {
		this.total = total;
	}

	/**
	 * Gets the records.
	 *
	 * @return the records
	 */
	public List<AuditActivity> getRecords() {
		return records;
	}

	/**
	 * Sets the records.
	 *
	 * @param records
	 *            the new records
	 */
	public void setRecords(List<AuditActivity> records) {
		this.records = records;
	}
}
