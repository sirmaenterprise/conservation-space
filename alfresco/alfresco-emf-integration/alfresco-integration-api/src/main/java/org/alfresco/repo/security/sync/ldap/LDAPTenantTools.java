package org.alfresco.repo.security.sync.ldap;

import org.alfresco.util.Pair;

/**
 * The Interface LDAPTenantTools.
 *
 * @author bbanchev
 */
public interface LDAPTenantTools {

	/**
	 * Gets the organization unit from the sourceId.
	 *
	 * @param name
	 *            the name
	 * @return the organization unit
	 */
	String getOrganizationUnit(String name);

	/**
	 * Prepare ldap user search argument.
	 *
	 * @param userSearchBase
	 *            the user search base
	 * @param teanatId
	 *            the teanat id
	 * @return the search base
	 */
	String prepareLDAPUserSearchBase(String userSearchBase, String teanatId);

	/**
	 * Prepare ldap group search argument.
	 *
	 * @param groupSearchBase
	 *            the user search base
	 * @param teanatId
	 *            the teanat id
	 * @return the search base
	 */
	String prepareLDAPGroupSearchBase(String groupSearchBase, String teanatId);

	/**
	 * Prepare ldap specific user search argument.
	 *
	 * @param userSearchBase
	 *            the user search base
	 * @param userId
	 *            the user id
	 * @return the pair of search base and user id
	 */
	Pair<String, String> prepareLDAPSpecificUserSearchArgument(String userSearchBase, String userId);
}
