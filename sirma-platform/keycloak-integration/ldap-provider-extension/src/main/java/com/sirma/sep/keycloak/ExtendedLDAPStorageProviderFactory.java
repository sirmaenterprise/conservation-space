package com.sirma.sep.keycloak;

import static com.sirma.sep.keycloak.ExtendedLDAPConstants.DEFAULT_BASE_DN;
import static com.sirma.sep.keycloak.ExtendedLDAPConstants.DEFAULT_GROUPS_DN;
import static com.sirma.sep.keycloak.ExtendedLDAPConstants.DEFAULT_ORGANIZATION_ATTRIBUTE;
import static com.sirma.sep.keycloak.ExtendedLDAPConstants.DEFAULT_ORGANIZATION_CLASS;
import static com.sirma.sep.keycloak.ExtendedLDAPConstants.DEFAULT_USERS_DN;
import static com.sirma.sep.keycloak.ExtendedLDAPConstants.GROUPS_DN_KEY;
import static com.sirma.sep.keycloak.ExtendedLDAPConstants.ORGANIZATION_ATTRIBUTE_KEY;
import static com.sirma.sep.keycloak.ExtendedLDAPConstants.ORGANIZATION_CLASS_KEY;
import static com.sirma.sep.keycloak.ExtendedLDAPConstants.USERS_DN_KEY;
import static org.keycloak.models.LDAPConstants.BASE_DN;

import java.util.Collections;

import javax.naming.directory.SearchControls;

import org.jboss.logging.Logger;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.LDAPStorageProviderFactory;
import org.keycloak.storage.ldap.idm.model.LDAPDn;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.idm.query.Condition;
import org.keycloak.storage.ldap.idm.query.EscapeStrategy;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQueryConditionsBuilder;
import org.keycloak.storage.ldap.idm.store.ldap.LDAPIdentityStore;

/**
 * Extends the default {@link LDAPStorageProviderFactory} by managing realms(tenants) in the underlying LDAP.
 * The default implementation does not manage realms in LDAP, but expects to be managed externally.
 *
 * On ldap provider creation it creates an organization in the LDAP if configured as writable, if not checks for organization
 * in the LDAP and if not found fails with exception.
 *
 * Before removing ldap provider, removes the organization from the LDAP if configured as writable.
 *
 * Configurations can be passed for the LDAP organization.
 *
 * @author smustafov
 */
public class ExtendedLDAPStorageProviderFactory extends LDAPStorageProviderFactory {

	private static final Logger LOGGER = Logger.getLogger(ExtendedLDAPStorageProviderFactory.class);

	// called after ldap provider is created for realm
	@Override
	public void onCreate(KeycloakSession session, RealmModel realm, ComponentModel model) {
		super.onCreate(session, realm, model);

		LDAPStorageProvider ldapStorageProvider = (LDAPStorageProvider) session
				.getProvider(UserStorageProvider.class, model);

		MultivaluedHashMap<String, String> config = model.getConfig();
		String organizationAttribute = getConfigValue(config, ORGANIZATION_ATTRIBUTE_KEY,
				DEFAULT_ORGANIZATION_ATTRIBUTE);
		String organizationClass = getConfigValue(config, ORGANIZATION_CLASS_KEY, DEFAULT_ORGANIZATION_CLASS);
		String baseDn = getConfigValue(config, BASE_DN, DEFAULT_BASE_DN);
		String usersDn = getConfigValue(config, USERS_DN_KEY, DEFAULT_USERS_DN);
		String groupsDn = getConfigValue(config, GROUPS_DN_KEY, DEFAULT_GROUPS_DN);

		// realmName - corresponds to tenant identifier in SEP
		String realmName = realm.getName();
		if (isOrganizationCreated(ldapStorageProvider, realmName, organizationAttribute, organizationClass, baseDn)) {
			LOGGER.infof("Organization already created in LDAP for realm: %s. Skipping create.", realmName);
			return;
		}

		createLdapOrganization(ldapStorageProvider, realmName, organizationAttribute, organizationClass, baseDn,
				usersDn, groupsDn);
	}

	private boolean isOrganizationCreated(LDAPStorageProvider ldapStorageProvider, String realmName,
			String organizationAttribute, String organizationClass, String baseDn) {
		LDAPQuery ldapQuery = new LDAPQuery(ldapStorageProvider);
		ldapQuery.setSearchScope(SearchControls.ONELEVEL_SCOPE);
		ldapQuery.setSearchDn(baseDn);
		ldapQuery.addObjectClasses(Collections.singletonList(organizationClass));

		LDAPQueryConditionsBuilder conditionsBuilder = new LDAPQueryConditionsBuilder();
		Condition organizationCondition = conditionsBuilder
				.equal(organizationAttribute, realmName, EscapeStrategy.DEFAULT);
		ldapQuery.addWhereCondition(organizationCondition);

		return ldapQuery.getResultCount() != 0;
	}

	private void createLdapOrganization(LDAPStorageProvider ldapStorageProvider, String realmName,
			String organizationAttribute, String organizationClass, String baseDn, String usersDn, String groupsDn) {
		if (!UserStorageProvider.EditMode.WRITABLE.equals(ldapStorageProvider.getEditMode())) {
			// no organization for read only LDAP, we cannot continue without this
			throw new ReadOnlyException("Organization do not exist on read only LDAP provider for realm: " + realmName);
		}

		// e.g: ou={realmName},dc=SIRMAITT,dc=BG - place for the realm
		createOrganizationalContext(ldapStorageProvider, realmName, organizationAttribute, organizationClass, baseDn);

		// e.g: ou=users,ou={realmName},dc=SIRMAITT,dc=BG - place to store realm's users
		createSubOrganizationalContext(ldapStorageProvider, realmName, organizationAttribute, organizationClass, baseDn,
				usersDn);

		// e.g: ou=groups,ou={realmName},dc=SIRMAITT,dc=BG - place to store realm's groups
		createSubOrganizationalContext(ldapStorageProvider, realmName, organizationAttribute, organizationClass, baseDn,
				groupsDn);

		LOGGER.infof("Successfully created organization in LDAP for realm: %s", realmName);
	}

	private void createOrganizationalContext(LDAPStorageProvider ldapStorageProvider, String realmName,
			String organizationAttribute, String organizationClass, String baseDn) {
		LDAPIdentityStore ldapIdentityStore = ldapStorageProvider.getLdapIdentityStore();
		ldapIdentityStore.add(buildOrganizationalContext(realmName, organizationAttribute, organizationClass, baseDn));
	}

	private LDAPObject buildOrganizationalContext(String realmName, String organizationAttribute,
			String organizationClass, String baseDn) {
		LDAPObject ldapOrganization = new LDAPObject();
		ldapOrganization.setRdnAttributeName(organizationAttribute);
		ldapOrganization.setObjectClasses(Collections.singletonList(organizationClass));

		LDAPDn dn = LDAPDn.fromString(baseDn);
		dn.addFirst(organizationAttribute, realmName);
		ldapOrganization.setDn(dn);

		return ldapOrganization;
	}

	private void createSubOrganizationalContext(LDAPStorageProvider ldapStorageProvider, String realmName,
			String organizationAttribute, String organizationClass, String baseDn,
			String dnValue) {
		LDAPIdentityStore ldapIdentityStore = ldapStorageProvider.getLdapIdentityStore();

		LDAPObject ldapSubOrganization = new LDAPObject();
		ldapSubOrganization.setRdnAttributeName(organizationAttribute);
		ldapSubOrganization.setObjectClasses(Collections.singletonList(organizationClass));

		LDAPDn dn = LDAPDn.fromString(organizationAttribute + "=" + realmName + "," + baseDn);
		dn.addFirst(organizationAttribute, dnValue);
		ldapSubOrganization.setDn(dn);

		ldapIdentityStore.add(ldapSubOrganization);
	}

	// called before removing ldap provider for realm
	@Override
	public void preRemove(KeycloakSession session, RealmModel realm, ComponentModel model) {
		super.preRemove(session, realm, model);

		LDAPStorageProvider ldapStorageProvider = (LDAPStorageProvider) session
				.getProvider(UserStorageProvider.class, model);
		MultivaluedHashMap<String, String> config = model.getConfig();

		if (UserStorageProvider.EditMode.WRITABLE.equals(ldapStorageProvider.getEditMode())) {
			removeOrganization(ldapStorageProvider, config, realm);
		} else {
			LOGGER.infof("Skipped removing organization on read only LDAP provider for realm: %s", realm.getName());
		}
	}

	/**
	 * Removes everything(organization, users, groups) about realm in the underlying LDAP.
	 *
	 * @param ldapStorageProvider the ldap storage provider
	 * @param config the ldap provider configuration map
	 * @param realm the realm model
	 */
	private void removeOrganization(LDAPStorageProvider ldapStorageProvider, MultivaluedHashMap<String, String> config,
			RealmModel realm) {
		String realmName = realm.getName();
		String organizationAttribute = getConfigValue(config, ORGANIZATION_ATTRIBUTE_KEY,
				DEFAULT_ORGANIZATION_ATTRIBUTE);
		String organizationClass = getConfigValue(config, ORGANIZATION_CLASS_KEY, DEFAULT_ORGANIZATION_CLASS);
		String baseDn = getConfigValue(config, BASE_DN, DEFAULT_BASE_DN);

		if (!isOrganizationCreated(ldapStorageProvider, realmName, organizationAttribute, organizationClass, baseDn)) {
			LOGGER.infof("Organization does not exists in LDAP for realm: %s. Skipping remove.", realmName);
			return;
		}

		LDAPIdentityStore ldapIdentityStore = ldapStorageProvider.getLdapIdentityStore();
		// passing only the context, the children will be deleted recursively
		ldapIdentityStore
				.remove(buildOrganizationalContext(realmName, organizationAttribute, organizationClass, baseDn));

		LOGGER.infof("Removed organization in LDAP for realm: %s", realmName);
	}

	private static String getConfigValue(MultivaluedHashMap<String, String> configMap, String key,
			String defaultValue) {
		String configValue = configMap.getFirst(key);
		return configValue != null ? configValue : defaultValue;
	}

}
