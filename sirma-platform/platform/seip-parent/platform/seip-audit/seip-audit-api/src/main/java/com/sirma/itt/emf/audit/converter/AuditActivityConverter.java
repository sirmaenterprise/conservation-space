package com.sirma.itt.emf.audit.converter;

import java.util.List;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.command.AuditCommand;

/**
 * Converts {@link AuditActivity} objects to a more user-friendly, readable form.
 *
 * @see {@link AuditCommand#assignLabel(AuditActivity, com.sirma.itt.emf.audit.command.AuditContext)}
 * @author nvelkov
 * @author Mihail Radkov
 */
public interface AuditActivityConverter {

	/**
	 * Convert any given list of audit activities. After the converting, the list will contain copies of the original
	 * activities.
	 *
	 * @param activities
	 *            the converted activities
	 */
	void convertActivities(List<AuditActivity> activities);

}
