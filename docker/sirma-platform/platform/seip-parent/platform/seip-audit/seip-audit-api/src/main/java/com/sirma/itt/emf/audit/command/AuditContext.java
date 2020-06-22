package com.sirma.itt.emf.audit.command;

import java.util.Map;

import com.sirma.itt.emf.audit.activity.AuditActivity;

/**
 * Stores contextual information about a {@link AuditActivity} when assigning labels via
 * {@link AuditCommand#assignLabel(AuditActivity, AuditContext)}.
 *
 * @author Mihail Radkov
 */
public class AuditContext {

	private final Map<String, String> objectHeaders;

	/**
	 * Constructs a new contextual object with the provided maps.
	 *
	 * @param contextHeaders
	 *            - map that stores the context headers
	 * @param objectHeaders
	 *            - map that stores the object headers
	 */
	public AuditContext(Map<String, String> objectHeaders) {
		this.objectHeaders = objectHeaders;
	}

	/**
	 * Getter method for objectHeaders.
	 *
	 * @return the objectHeaders
	 */
	public Map<String, String> getObjectHeaders() {
		return objectHeaders;
	}

}
