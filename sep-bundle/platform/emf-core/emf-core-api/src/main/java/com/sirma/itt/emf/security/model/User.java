package com.sirma.itt.emf.security.model;

import java.io.Serializable;
import java.security.Principal;

import com.sirma.itt.emf.resources.model.Resource;

/**
 * Interface identifying a user in the CMF system in order to calculate his roles and actions.
 *
 * @author BBonev
 */
public interface User extends Principal, Resource, Serializable {

	/**
	 * Gets user tenant id.
	 *
	 * @return the tenant id
	 */
	String getTenantId();

	/**
	 * Sets the tenant id.
	 * 
	 * @param tenantId
	 *            the new tenant id
	 */
	void setTenantId(String tenantId);

	/**
	 * Gets the preferred user language.
	 * 
	 * @return the language
	 */
	String getLanguage();
}
