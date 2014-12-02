package com.sirma.itt.emf.audit.converter;

import com.sirma.itt.emf.audit.activity.AuditActivity;

/**
 * Converts {@link AuditActivity} objects to a more user-friendly, readable form.
 * 
 * @author nvelkov
 */
public interface AuditActivityConverter {

	/**
	 * Convert activity.
	 * 
	 * @param activity
	 *            the activity
	 */
	void convertActivity(AuditActivity activity);

}
