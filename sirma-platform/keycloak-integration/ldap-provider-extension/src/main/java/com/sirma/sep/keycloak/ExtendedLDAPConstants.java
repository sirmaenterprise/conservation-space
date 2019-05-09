package com.sirma.sep.keycloak;

import org.keycloak.models.LDAPConstants;

/**
 * Contains constants used in Keycloak ldap provider extension.
 *
 * @author smustafov
 */
public class ExtendedLDAPConstants extends LDAPConstants {

	public static final String ORGANIZATION_CLASS_KEY = "organizationClassKey";
	public static final String ORGANIZATION_ATTRIBUTE_KEY = "organizationAttributeKey";

	public static final String DEFAULT_ORGANIZATION_CLASS = "organizationalUnit";
	public static final String DEFAULT_ORGANIZATION_ATTRIBUTE = "ou";

	public static final String USERS_DN_KEY = "usersDnKey";
	public static final String GROUPS_DN_KEY = "groupsDnKey";

	public static final String DEFAULT_USERS_DN = "users";
	public static final String DEFAULT_GROUPS_DN = "groups";

	public static final String DEFAULT_BASE_DN = "dc=SIRMAITT,dc=BG";

}
