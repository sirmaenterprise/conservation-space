package org.alfresco.repo.security.sync.ldap;

import org.alfresco.repo.security.SEIPTenantIntegration;
import org.alfresco.util.Pair;

/**
 * LDAP tool with tenant support based on WSO2 IDP 5.0 ldap
 * 
 * @author bbanchev
 */
public class LDAPWSO2TenantTools implements LDAPTenantTools {

	@Override
	public String getOrganizationUnit(String name) {
		String tenantDomain = "";
		for (String partition : name.split(",")) {
			partition = partition.trim();
			if (partition.toLowerCase().startsWith("ou=")
					&& !(partition.equalsIgnoreCase("ou=groups") || partition.equalsIgnoreCase("ou=users"))) {
				tenantDomain = partition.split("=")[1];
				break;
			}
		}
		return tenantDomain;
	}

	@Override
	public String prepareLDAPUserSearchBase(String searchBase, String tenantId) {
		if (tenantId != null && !tenantId.isEmpty()) {
			return buildSearchBaseForTenant(searchBase, tenantId);
		}
		return buildSearchBaseForTenant(searchBase, "users");
	}

	@Override
	public String prepareLDAPGroupSearchBase(String searchBase, String tenantId) {

		if (tenantId != null && !tenantId.isEmpty()) {
			return buildSearchBaseForTenant(searchBase, tenantId);
		}
		return buildSearchBaseForTenant(searchBase, "groups");
	}

	@Override
	public Pair<String, String> prepareLDAPSpecificUserSearchArgument(String userSearchBase, String userId) {
		String tenantId = SEIPTenantIntegration.getTenantId(userId);
		if (tenantId != null && !tenantId.isEmpty()) {
			return new Pair<String, String>(buildSearchBaseForTenant(userSearchBase, "users"),
					SEIPTenantIntegration.getBaseName(userId));
		}
		// unmodified
		return new Pair<String, String>(userSearchBase, userId);
	}

	private String buildSearchBaseForTenant(String searchBase, String tenantId) {
		StringBuilder filterBilder = new StringBuilder(tenantId.length() + searchBase.length() + 4);
		filterBilder.append("ou=");
		filterBilder.append(tenantId);
		filterBilder.append(",");
		filterBilder.append(searchBase);
		return filterBilder.toString();
	}

}
