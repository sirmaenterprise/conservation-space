package com.sirma.sep.keycloak.ldap;

/**
 * Represents edit mode of the underlying LDAP in Keycloak IdP.
 */
public enum LdapMode {

	/**
	 * Data will be synced back to LDAP on demand.
	 */
	WRITABLE,

	/**
	 * Read only LDAP store.
	 */
	READ_ONLY,

	/**
	 * Data will be imported to Keycloak, but not synced back to LDAP.
	 */
	UNSYNCED

}
