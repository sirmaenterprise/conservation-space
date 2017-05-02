package com.sirma.itt.seip.resources;

import java.security.Principal;

/**
 * Interface identifying a user in the CMF system in order to calculate his roles and actions.
 *
 * @author BBonev
 */
public interface User extends Principal, Resource, com.sirma.itt.seip.security.User {

	/**
	 * Gets user tenant id.
	 *
	 * @return the tenant id
	 */
	@Override
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
	@Override
	String getLanguage();
}
