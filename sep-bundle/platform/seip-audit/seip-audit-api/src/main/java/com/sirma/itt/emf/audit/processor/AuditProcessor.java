package com.sirma.itt.emf.audit.processor;

import com.sirma.itt.emf.audit.activity.AuditActivity;

/**
 * Processor for audit activities.
 * 
 * @author Mihail Radkov
 */
public interface AuditProcessor {

	/**
	 * Processes audit activities.
	 * 
	 * @param activity
	 *            the activity
	 */
	void processActivity(AuditActivity activity);
}
