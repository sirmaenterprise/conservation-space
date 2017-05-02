package com.sirma.itt.emf.audit.processor;

import java.util.List;

/**
 * Wrapper for the returned recent activities.
 *
 * @author nvelkov
 */
public class StoredAuditActivitiesWrapper {

	private List<StoredAuditActivity> activities;

	private long total;

	public List<StoredAuditActivity> getActivities() {
		return activities;
	}

	public StoredAuditActivitiesWrapper setActivities(List<StoredAuditActivity> activities) {
		this.activities = activities;
		return this;
	}

	public long getTotal() {
		return total;
	}

	public StoredAuditActivitiesWrapper setTotal(long total) {
		this.total = total;
		return this;
	}

}
