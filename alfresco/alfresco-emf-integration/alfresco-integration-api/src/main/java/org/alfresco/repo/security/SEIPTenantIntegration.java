package org.alfresco.repo.security;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;

/**
 * The SEIPTenantIntegration is utility service to provide easier data processing/checks of information regarding
 * tenants and authorities.
 */
public class SEIPTenantIntegration {
	private SEIPTenantIntegration() {
	}

	private static final String EMPTY_STRING = "";
	private static final char SEPARATOR = TenantService.SEPARATOR.charAt(0);

	/**
	 * Gets the system user using provide name. If user is part of default domain the default system user is returned.
	 * If user is part of any other domain a user id with system@... format is returned
	 *
	 * @param userName
	 *            the user name
	 * @return the system user
	 */
	public static String getSystemUser(String userName) {
		String tenantId = getTenantId(userName);
		return getSystemUserByTenantId(tenantId);
	}

	/**
	 * Gets the system user using provided tenant id. If tenant is empty the default system user is returned. If the
	 * tenant is non null a user with id with system@... format is returned
	 *
	 * @param tenantId
	 *            the tenant id
	 * @return the system user
	 */
	public static String getSystemUserByTenantId(String tenantId) {
		if (tenantId != null && !tenantId.isEmpty()) {
			StringBuilder builder = new StringBuilder();
			builder.append(AuthenticationUtil.getSystemUserName());
			builder.append(SEPARATOR);
			builder.append(tenantId);
			return builder.toString();
		}
		return AuthenticationUtil.getSystemUserName();
	}

	/**
	 * Gets the system user using current runAs user.
	 *
	 * @return the system user
	 */
	public static String getSystemUser() {
		return getSystemUser(AuthenticationUtil.getRunAsUser());
	}

	/**
	 * Extract the tenant Id from runAs user.
	 *
	 * @return the tenant id or empty string if this is default tenant
	 */
	public static String getTenantId() {
		return getTenantId(AuthenticationUtil.getRunAsUser());
	}

	/**
	 * Extract the tenant Id from authority.
	 *
	 * @param fullName
	 *            to get for
	 * @return the tenant id or empty string if this is default tenant
	 */
	public static String getTenantId(String fullName) {
		if (fullName == null) {
			return EMPTY_STRING;
		}
		int idx = fullName.lastIndexOf(SEPARATOR);
		if ((idx > 0) && (idx < (fullName.length() - 1))) {
			return fullName.substring(idx + 1);
		}
		return EMPTY_STRING;
	}

	/**
	 * Gets the base name.
	 *
	 * @param fullName
	 *            the full name
	 * @return the base name
	 */
	public static String getBaseName(String fullName) {
		int idx = fullName.lastIndexOf(SEPARATOR);
		if ((idx > 0) && (idx < (fullName.length() - 1))) {
			return fullName.substring(0, idx);
		}
		return fullName;
	}

	/**
	 * Checks if is valid tenant.
	 *
	 * @param tenantId
	 *            the tenantid
	 * @return true, if is valid tenant
	 */
	public static boolean isValidTenant(String tenantId) {
		if (tenantId == null || tenantId.trim().isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	 * Checks if user is from tenant.
	 *
	 * @param userId
	 *            the full user id
	 * @return true if the user is format userame@tenant
	 */
	public static boolean isTenantUser(String userId) {
		if (userId == null || userId.trim().isEmpty()) {
			return false;
		}
		// at least 1 symbol
		if (userId.indexOf(SEPARATOR) > 0) {
			return true;
		}
		return false;
	}
}
