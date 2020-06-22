package com.sirma.sep.keycloak.ldap;

/**
 * Keycloak-LDAP related constants used for creating LDAP provider.
 *
 * @author smustafov
 */
class LdapConstants {

	private LdapConstants() {
		// hide
	}

	static final String LDAP_PROVIDER_ID = "ldap";
	static final String LDAP_PROVIDER_TYPE = "org.keycloak.storage.UserStorageProvider";
	static final String GROUP_MAPPER_ID = "group-ldap-mapper";
	static final String LDAP_MAPPER_PROVIDER_TYPE = "org.keycloak.storage.ldap.mappers.LDAPStorageMapper";
	static final String DEFAULT_USER_ATTRIBUTE_MAPPER = "user-attribute-ldap-mapper";

	static final String BASE_DN = "baseDn";
	static final String USERS_DN = "usersDn";
	static final String GROUPS_DN = "groups.dn";
	static final String GROUPS_MODE = "mode";
	static final String GROUPS_MEMBERSHIP_ATTRIBUTE = "membership.user.ldap.attribute";
	static final String ORGANIZATION_CLASS = "organizationClass";
	static final String ORGANIZATION_ATTRIBUTE = "organizationAttribute";
	static final String BIND_DN = "bindDn";
	static final String BIND_CREDENTIAL = "bindCredential";
	static final String CONNECTION_URL = "connectionUrl";
	static final String AUTH_TYPE = "authType";
	static final String SYNC_REGISTRATIONS = "syncRegistrations";
	static final String EDIT_MODE = "editMode";
	static final String RDN_ATTRIBUTE = "rdnLDAPAttribute";
	static final String USERNAME_ATTRIBUTE = "usernameLDAPAttribute";
	static final String USER_OBJECT_CLASS = "userObjectClasses";
	static final String UUID_ATTRIBUTE = "uuidLDAPAttribute";
	static final String IMPORT_ENABLED = "importEnabled";
	static final String CHANGED_SYNC_PERIOD = "changedSyncPeriod";
	static final String VALIDATE_PASSWORD_POLICY = "validatePasswordPolicy"; // NOSONAR

}
