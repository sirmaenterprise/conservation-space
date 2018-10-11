package com.sirma.itt.emf.audit.command;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.activity.AuditablePayload;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Provides common logic for all classes that extends this one.
 *
 * @author Mihail Radkov
 */
public abstract class AuditAbstractCommand implements AuditCommand {

	/**
	 * Gets the instance inside {@link AuditablePayload} DTO. The method's purpose is for NPE safety.
	 *
	 * @param payload
	 *            - the DTO containing auditable information
	 * @return the instance or null if the DTO is null
	 */
	protected Instance getInstance(AuditablePayload payload) {
		if (payload != null) {
			return payload.getInstance();
		}
		return null;
	}

	/**
	 * Extracts specific value by given key from a Map of properties.
	 *
	 * @param properties
	 *            the map with properties
	 * @param key
	 *            the property's key
	 * @return the property or null
	 */
	protected String getProperty(Map<String, Serializable> properties, String key) {
		if (properties != null) {
			Serializable value = properties.get(key);
			if (value != null) {
				return value.toString();
			}
		}
		return null;
	}

	@Override
	public void assignLabel(AuditActivity activity, AuditContext context) {
		// Overridden here so not all commands have to implement it.
	}
}
