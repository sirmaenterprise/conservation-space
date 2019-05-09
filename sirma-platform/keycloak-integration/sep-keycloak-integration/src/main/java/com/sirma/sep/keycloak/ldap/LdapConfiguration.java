package com.sirma.sep.keycloak.ldap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.representations.idm.ComponentRepresentation;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;

/**
 * Contains configurations related to LDAP that will be used for Keycloak.
 *
 * @author smustafov
 */
@ApplicationScoped
public class LdapConfiguration {

	static final String LDAP_EDIT_MODE_NAME = "ldap.edit.mode";

	@ConfigurationPropertyDefinition(defaultValue = "ldap://ldap:10389", sensitive = true,
			label = "Connection URL to the LDAP in format: ldap://IP:PORT")
	private static final String CONNECTION_URL = "ldap.connection.url";

	@ConfigurationPropertyDefinition(defaultValue = "cn=Manager,dc=sirmaitt,dc=bg", sensitive = true,
			label = "The DN that will be bind to authenticate with LDAP")
	private static final String BIND_DN = "ldap.bindDn";

	@ConfigurationPropertyDefinition(defaultValue = "secret", sensitive = true, password = true,
			label = "The credential that will be bind to authenticate with LDAP")
	private static final String BIND_CREDENTIAL = "ldap.bind.credential";

	@ConfigurationPropertyDefinition(defaultValue = "organizationalUnit", sensitive = true,
			label = "Value for LDAP objectClass. This is used for creating organization for a particular tenant")
	private static final String ORGANIZATION_CLASS = "ldap.organizationClass";

	@ConfigurationPropertyDefinition(defaultValue = "ou", sensitive = true,
			label = "Name of LDAP attribute, which is used as unique organization identifier. Default value: ou")
	private static final String ORGANIZATION_ATTRIBUTE = "ldap.organizationAttribute";

	@ConfigurationPropertyDefinition(defaultValue = "dc=SIRMAITT,dc=BG", sensitive = true,
			label = "Base (root) DN that all LDAP data is contained. Default value: dc=SIRMAITT,dc=BG")
	private static final String BASE_DN = "ldap.baseDn";

	@ConfigurationPropertyDefinition(defaultValue = "ou=users,ou={tenantId},dc=SIRMAITT,dc=BG", sensitive = true, label =
			"Full DN of LDAP tree where all the users are. This DN is parent of LDAP users. "
					+ "There should be mandatory tenant id placeholder: {tenantId}. Which will be replaced with the created tenant id. "
					+ "Default value: ou=users,ou={tenantId},dc=SIRMAITT,dc=BG")
	private static final String USERS_DN = "ldap.usersDn";

	@ConfigurationPropertyDefinition(defaultValue = "ou=groups,ou={tenantId},dc=SIRMAITT,dc=BG", sensitive = true, label =
			"Full DN of LDAP tree where all the groups are. This DN is parent of LDAP groups."
					+ "There should be mandatory tenant id placeholder: {tenantId}. Which will be replaced with the created tenant id. "
					+ "Default value: ou=groups,ou={tenantId},dc=SIRMAITT,dc=BG")
	private static final String GROUPS_DN = "ldap.groupsDn";

	@ConfigurationPropertyDefinition(defaultValue = "uid", sensitive = true,
			label = "Name of the LDAP attribute, which is used for group membership mappings")
	private static final String GROUP_MEMBER_ATTRIBUTE = "ldap.group.member.attribute";

	@ConfigurationPropertyDefinition(defaultValue = "uid", sensitive = true,
			label = "Name of the LDAP attribute, which is mapped as username")
	private static final String USERNAME_ATTRIBUTE = "ldap.username.attribute";

	@ConfigurationPropertyDefinition(defaultValue = "uid", sensitive = true,
			label = "Name of the LDAP attribute, which is mapped as RDN")
	private static final String RDN_ATTRIBUTE = "ldap.rdn.attribute";

	@ConfigurationPropertyDefinition(defaultValue = "entryUUID", sensitive = true,
			label = "Name of LDAP attribute, which is used as unique object identifier (UUID) for objects in LDAP")
	private static final String UUID_ATTRIBUTE = "ldap.uuid.attribute";

	@ConfigurationPropertyDefinition(defaultValue = "inetOrgPerson, organizationalPerson, identityPerson", sensitive = true, label =
			"All values of LDAP objectClass attribute for users in LDAP divided by comma. Newly created users will "
					+ "be written to LDAP with all those object classes and existing LDAP user records are found just if they contain all those object classes")
	private static final String USER_CLASSES = "ldap.user.classes";

	@ConfigurationPropertyDefinition(type = Boolean.class, defaultValue = "true", sensitive = true,
			label = "If true, LDAP users will be imported into Keycloak DB and synced via the configured sync policies. Default value: true")
	private static final String IMPORT_USERS = "ldap.import.users";

	@ConfigurationPropertyDefinition(type = Boolean.class, defaultValue = "true", sensitive = true,
			label = "Should newly created users be created within LDAP store. Default value: true")
	private static final String SYNC_REGISTRATIONS = "ldap.sync.registrations";

	@ConfigurationPropertyDefinition(defaultValue = "86400", sensitive = true,
			label = "Period for synchronization of changed or newly created LDAP users in seconds. Default value: 86400 (1 day)")
	private static final String CHANGED_SYNC_PERIOD = "ldap.sync.period";

	@ConfigurationGroupDefinition(type = ComponentRepresentation.class, properties = { CONNECTION_URL, BIND_DN,
			BIND_CREDENTIAL, ORGANIZATION_CLASS, ORGANIZATION_ATTRIBUTE, BASE_DN, USERS_DN,
			USERNAME_ATTRIBUTE, RDN_ATTRIBUTE, UUID_ATTRIBUTE, USER_CLASSES, IMPORT_USERS,
			SYNC_REGISTRATIONS }, label = "Keycloak LDAP component representation instance")
	private static final String LDAP_COMPONENT_REPRESENTATION = "ldap.component.representation";

	@ConfigurationGroupDefinition(type = ComponentRepresentation.class, properties = { GROUPS_DN,
			GROUP_MEMBER_ATTRIBUTE }, label = "Keycloak LDAP group mapper component representation instance")
	private static final String GROUP_COMPONENT_REPRESENTATION = "group.component.representation";

	@Inject
	@Configuration(LDAP_COMPONENT_REPRESENTATION)
	private ConfigurationProperty<ComponentRepresentation> ldapComponent;

	@Inject
	@Configuration(GROUP_COMPONENT_REPRESENTATION)
	private ConfigurationProperty<ComponentRepresentation> groupMapperComponent;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = LDAP_EDIT_MODE_NAME, defaultValue = "WRITABLE", sensitive = true, type = LdapMode.class, converter = "enum", label =
			"READ_ONLY is a read only LDAP store. WRITABLE means data will be synced back to LDAP on demand. "
					+ "UNSYNCED means user data will be imported, but not synced back to LDAP. Default value: WRITABLE")
	private ConfigurationProperty<LdapMode> ldapMode;

	@ConfigurationConverter(LDAP_COMPONENT_REPRESENTATION)
	static ComponentRepresentation buildLdapComponent(GroupConverterContext context) {
		ComponentRepresentation ldapStorageComponent = new ComponentRepresentation();
		ldapStorageComponent.setName(LdapConstants.LDAP_PROVIDER_ID);
		ldapStorageComponent.setProviderId(LdapConstants.LDAP_PROVIDER_ID);
		ldapStorageComponent.setProviderType(LdapConstants.LDAP_PROVIDER_TYPE);

		LdapMode ldapMode = context.get(LDAP_EDIT_MODE_NAME);

		MultivaluedHashMap<String, String> config = new MultivaluedHashMap<>();
		config.putSingle(LdapConstants.BASE_DN, context.get(BASE_DN));
		config.putSingle(LdapConstants.ORGANIZATION_CLASS, context.get(ORGANIZATION_CLASS));
		config.putSingle(LdapConstants.ORGANIZATION_ATTRIBUTE, context.get(ORGANIZATION_ATTRIBUTE));
		config.putSingle(LdapConstants.USERS_DN, context.get(USERS_DN));
		config.putSingle(LdapConstants.BIND_DN, context.get(BIND_DN));
		config.putSingle(LdapConstants.BIND_CREDENTIAL, context.get(BIND_CREDENTIAL));
		config.putSingle(LdapConstants.CONNECTION_URL, context.get(CONNECTION_URL));
		config.putSingle(LdapConstants.AUTH_TYPE, "simple");
		config.putSingle(LdapConstants.SYNC_REGISTRATIONS, context.get(SYNC_REGISTRATIONS).toString());
		config.putSingle(LdapConstants.EDIT_MODE, ldapMode.toString());
		config.putSingle(LdapConstants.RDN_ATTRIBUTE, context.get(RDN_ATTRIBUTE));
		config.putSingle(LdapConstants.USERNAME_ATTRIBUTE, context.get(USERNAME_ATTRIBUTE));
		config.putSingle(LdapConstants.USER_OBJECT_CLASS, context.get(USER_CLASSES));
		config.putSingle(LdapConstants.UUID_ATTRIBUTE, context.get(UUID_ATTRIBUTE));
		config.putSingle(LdapConstants.IMPORT_ENABLED, context.get(IMPORT_USERS).toString());
		config.putSingle(LdapConstants.CHANGED_SYNC_PERIOD, context.get(CHANGED_SYNC_PERIOD));
		config.putSingle("priority", "0");
		config.putSingle("vendor", "other");
		config.putSingle("enabled", "true");
		ldapStorageComponent.setConfig(config);
		return ldapStorageComponent;
	}

	@ConfigurationConverter(GROUP_COMPONENT_REPRESENTATION)
	static ComponentRepresentation buildGroupMapperComponent(GroupConverterContext context) {
		ComponentRepresentation ldapGroupMapperSubComponent = new ComponentRepresentation();
		ldapGroupMapperSubComponent.setName(LdapConstants.GROUP_MAPPER_ID);
		ldapGroupMapperSubComponent.setProviderId(LdapConstants.GROUP_MAPPER_ID);
		ldapGroupMapperSubComponent.setProviderType(LdapConstants.LDAP_MAPPER_PROVIDER_TYPE);

		MultivaluedHashMap<String, String> config = new MultivaluedHashMap<>();
		config.putSingle(LdapConstants.GROUPS_DN, context.get(GROUPS_DN));
		config.putSingle(LdapConstants.GROUPS_MODE, "LDAP_ONLY");
		config.putSingle(LdapConstants.GROUPS_MEMBERSHIP_ATTRIBUTE, context.get(GROUP_MEMBER_ATTRIBUTE));
		ldapGroupMapperSubComponent.setConfig(config);
		return ldapGroupMapperSubComponent;
	}

	ComponentRepresentation getLdapComponent() {
		return copyComponent(ldapComponent.get());
	}

	ComponentRepresentation getGroupMapperComponent() {
		return copyComponent(groupMapperComponent.get());
	}

	private ComponentRepresentation copyComponent(ComponentRepresentation componentRepresentation) {
		ComponentRepresentation copy = new ComponentRepresentation();
		copy.setName(componentRepresentation.getName());
		copy.setProviderId(componentRepresentation.getProviderId());
		copy.setProviderType(componentRepresentation.getProviderType());
		copy.setConfig(new MultivaluedHashMap<>(componentRepresentation.getConfig()));
		return copy;
	}

	public ConfigurationProperty<LdapMode> getLdapMode() {
		return ldapMode;
	}
}
