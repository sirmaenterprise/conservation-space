package com.sirma.itt.seip.security.context;

import java.io.Serializable;
import java.util.Objects;

import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.exception.ContextNotActiveException;

/**
 * Security context based on the Linux model and supports real, effective authorities
 * <p>
 * The real authority is used for auditing and reporting who the user is etc. <br>
 * The effective authority is used for permission checks.
 * <p>
 * RunAs support leaves the real authority and changes only the effective authority That means "special" code can run
 * code as system but still be audited as Joe
 * <p>
 * In multi tenant context there some special terms for current tenant:
 * <ul>
 * <li>System tenant - this is special tenant that identifies the system itself. The only members of this tenant are the
 * system user used to represent the system to the users and the 'super' admin user that can manage other tenants. And
 * from them only the admin user could actually login into the system.<br>
 * To check if is in the context of the system tenant the method {@link #isSystemTenant()} or
 * {@link #isSystemTenant(String)} could be used. <br>
 * The identifier for the system tenant is {@value #SYSTEM_TENANT}.
 * <li>Default tenant - this is special tenant that identifies the existing installation then multi-tenantcy enabled for
 * the first time. Note that the system may not have a default tenant for clean installations. In that case is created a
 * regular tenant.<br>
 * In this case the registered users continue to login as normal and specifying a tenant domain at login is not required
 * unless specified by JVM application configuration {@link #DEFAULT_TENANT_REQUIRED_ON_AUTHENTICATION} =
 * {@value #DEFAULT_TENANT_REQUIRED_ON_AUTHENTICATION} set to <code>true</code>. <br>
 * The default id could be overridden by passing a JVM application configuration {@value #DEFAULT_TENANT_ID_KEY}<br>
 * Note that once initialized the default tenant id could not be changed to one of the other tenants. <br>
 * To check if the current tenant is default the methods {@link #isDefaultTenant()}, {@link #isDefaultTenant(String)},
 * {@link #getDefaultTenantId()} could be used. <br>
 * The default id for the default tenant is {@value #DEFAULT_TENANT}.<br>
 * Note: in order the system to now which tenant is default the configuration {@value #DEFAULT_TENANT_ID_KEY} should
 * always be present, If default tenant id is changed! If removed the 'default' tenant will no longer be default and
 * will be considered as regular tenant so it should meet all configuration requirements for regular tenant.
 * <li>Regular tenant - this is normal tenant. The users for this type of tenant are required to specify the tenant id
 * at login. <br>
 * Note that regular tenant could not be created with domain id that matches the values {@value #SYSTEM_TENANT} or
 * {@value #DEFAULT_TENANT}. <br>
 * <b>All tenants created for new clean installations are regular tenants!</b>
 * </ul>
 *
 * @author BBonev
 */
public interface SecurityContext extends Serializable {

	/**
	 * JVM application argument that controls if authentication mechanism should require specifying the default tenant
	 * id at login. Default behavior is NOT to require. Default value: <code>false</code>.
	 */
	String DEFAULT_TENANT_REQUIRED_ON_AUTHENTICATION = "default.tenantIdRequiredOnAuthentication";
	/**
	 * JVM application argument key to override the default tenant key. Usage:
	 * <code>-Ddefault.tenantId=myDefaultTenant</code>
	 */
	String DEFAULT_TENANT_ID_KEY = "default.tenantId";

	/**
	 * JVM application argument key to override the default systemadmin name. Usage:
	 * <code>-Dtenant.default.adminname=superadmin</code><br>
	 * Default is {@link #DEFAULT_SYSTEMADMIN_NAME}
	 */
	String DEFAULT_SYSTEMADMIN_NAME_KEY = "tenant.default.adminname";
	/**
	 * The default tenant identifier. This will be returned from the method {@link #getCurrentTenantId()} if the context
	 * is not active or multi tenant mode is not active.
	 */
	String DEFAULT_TENANT = "default.tenant";
	/**
	 * The default tenant identifier. This will be returned from the method {@link #getCurrentTenantId()} if the context
	 * is not active or multi tenant mode is not active.
	 */
	String SYSTEM_TENANT = "system.tenant";
	/**
	 * Default value for system admin
	 */
	String DEFAULT_SYSTEMADMIN_NAME = "systemadmin";

	/**
	 * User name of the system user.
	 */
	String SYSTEM_USER_NAME = "System";

	/**
	 * Checks if is active.
	 *
	 * @return true, if is active
	 */
	boolean isActive();

	/**
	 * Gets the current tenant id.
	 *
	 * @return the current tenant id
	 */
	String getCurrentTenantId();

	/**
	 * Gets the real authentication used to for auditing.
	 * <p>
	 * Note that if the method {@link #isActive()} returns <code>false</code> this method should throw a
	 * {@link ContextNotActiveException}
	 *
	 * @return the authenticated
	 */
	User getAuthenticated();

	/**
	 * Gets a request correlation id. This id should be generated on context initialization per request and should be
	 * copied upon context transfer between threads or service requests over the wire.
	 *
	 * @return the request correlation id
	 */
	String getRequestId();

	/**
	 * Get the effective authentication - used for permission checks and accessing the subsystems system and for current
	 * user operations.
	 * <p>
	 * Note that if the method {@link #isActive()} returns <code>false</code> this method should throw a
	 * {@link ContextNotActiveException}
	 *
	 * @return the effective authentication
	 */
	default User getEffectiveAuthentication() {
		return getAuthenticated();
	}

	/**
	 * Checks if current context is active and is authenticated.
	 *
	 * @return true, if is authenticated
	 */
	boolean isAuthenticated();

	/**
	 * Checks if the given argument resolves to the current user. The argument could be a string to be matched to user
	 * id or {@link User} instance itself.
	 *
	 * @param object
	 *            the object
	 * @return true, if is current user
	 */
	default boolean isCurrentUser(Object object) {
		if (object == null || !isActive()) {
			return false;
		}
		if (object instanceof String) {
			return Objects.equals(getAuthenticated().getIdentityId(), object.toString());
		}
		if (object instanceof User) {
			return getAuthenticated().equals(object);
		}
		return false;
	}

	/**
	 * Checks if is system tenant. If <code>true</code> this means that tenant module is not active or security context
	 * active is initialized for System user.
	 *
	 * @return true, if is default tenant
	 */
	default boolean isSystemTenant() {
		return SYSTEM_TENANT.equals(getCurrentTenantId());
	}

	/**
	 * Checks if is default tenant. If <code>true</code> this means that the tenant module is not active or is the
	 * default tenant.
	 *
	 * @return true, if is default tenant
	 */
	default boolean isDefaultTenant() {
		return getDefaultTenantId().equals(getCurrentTenantId());
	}

	/**
	 * Checks if the given tenant id is the system tenant.
	 *
	 * @param tenant
	 *            the tenant
	 * @return true, if is system tenant
	 */
	static boolean isSystemTenant(String tenant) {
		return SYSTEM_TENANT.equals(tenant);
	}

	/**
	 * Gets the default tenant id. The id could be defined using the JVM argument {@value #DEFAULT_TENANT_ID_KEY}. If
	 * not specified then {@link #DEFAULT_TENANT} will be used as identifier.
	 *
	 * @return the default tenant id
	 */
	static String getDefaultTenantId() {
		return System.getProperty(DEFAULT_TENANT_ID_KEY, DEFAULT_TENANT);
	}

	/**
	 * Checks if the given tenant id is default tenant.
	 *
	 * @param tenantId
	 *            the tenant id
	 * @return true, if is default tenant
	 */
	static boolean isDefaultTenant(String tenantId) {
		return getDefaultTenantId().equals(tenantId);
	}

	/**
	 * Get the system admin username. This is the administrator of system and all tenants
	 *
	 * @return the configuration of {@value #DEFAULT_SYSTEMADMIN_NAME_KEY} or {@value #DEFAULT_SYSTEMADMIN_NAME} as
	 *         default
	 */
	static String getSystemAdminName() {
		return System.getProperty(DEFAULT_SYSTEMADMIN_NAME_KEY, DEFAULT_SYSTEMADMIN_NAME);
	}

}
