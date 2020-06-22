package com.sirma.itt.seip.idp.config;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

/**
 * The IDPConfiguration contains specific for IDP configuration as connection parameters or configuration values
 *
 * @author bbanchev
 */
@ApplicationScoped
public class IDPConfiguration implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4291518018239855880L;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "security.identity.server.admin.username", defaultValue = "admin", sensitive = true, system = true, label = "User name of the admin user communicating with WSO IDP server")
	private ConfigurationProperty<String> idpServerUserId;
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "security.identity.server.admin.password", system = true, sensitive = true, label = "User password of the admin user communicating with WSO IDP server")
	private ConfigurationProperty<String> idpServerUserPass;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "security.sso.idpUrl", system = true, sensitive = true, shared = false, label = "Identity provider server URL used for SSO. <br><b>NOTE: </b> "
			+ "The property need to end with the IP address of the calling machine (JBOSS server host name)."
			+ " The same address that is configured in the SSO server.")
	private ConfigurationProperty<String> idpServerURL;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(system = true, sensitive = true, name = "security.identity.ldapstore.template", defaultValue = "classApplied=org.wso2.carbon.user.core.ldap.ReadWriteLDAPUserStoreManager&domainId={0}&previousDomainId=&description=&propertyName_1=ConnectionName&propertyValue_1=uid%3Dadmin%2Cou%3Dsystem&propertyName_2=ConnectionURL&propertyValue_2=ldap%3A%2F%2Flocalhost%3A%24%7BPorts.EmbeddedLDAP.LDAPServerPort%7D&propertyName_3=ConnectionPassword&propertyValue_3={1}&propertyName_4=UserSearchBase&propertyValue_4=ou%3Dusers%2Cou%3D{0}%2Cdc%3DWSO2%2Cdc%3DORG&propertyName_5=Disabled&propertyName_6=UserNameListFilter&propertyValue_6=(objectClass%3Dperson)&propertyName_7=UserNameAttribute&propertyValue_7=uid&propertyName_8=UserNameSearchFilter&propertyValue_8=(%26(objectClass%3Dperson)(uid%3D%3F))&propertyName_9=UserEntryObjectClass&propertyValue_9=wso2Person&propertyName_10=GroupEntryObjectClass&propertyValue_10=groupOfNames&propertyName_11=ReadGroups&propertyValue_11=on&propertyName_12=GroupSearchBase&propertyValue_12=ou%3Dgroups%2Cou%3D{0}%2Cdc%3DWSO2%2Cdc%3DORG&propertyName_13=GroupNameAttribute&propertyValue_13=cn&propertyName_14=GroupNameListFilter&propertyValue_14=(objectClass%3DgroupOfNames)&propertyName_15=MembershipAttribute&propertyValue_15=member&propertyName_16=GroupNameSearchFilter&propertyValue_16=(%26(objectClass%3DgroupOfNames)(cn%3D%3F))&propertyName_17=MaxUserNameListLength&propertyValue_17=100&propertyName_18=MaxRoleNameListLength&propertyValue_18=100&propertyName_19=UserRolesCacheEnabled&propertyValue_19=on&propertyName_20=SCIMEnabled&propertyName_21=PasswordHashMethod&propertyValue_21=SHA&propertyName_22=UserDNPattern&propertyValue_22=uid%3D%7B0%7D%2Cou%3DUsers%2Cou%3D{0}%2Cdc%3DWSO2%2Cdc%3DORG&propertyName_23=PasswordJavaScriptRegEx&propertyValue_23=%5E%5B%5CS%5D%7B5%2C30%7D%24&propertyName_24=UserNameJavaScriptRegEx&propertyValue_24=%5E%5B%5CS%5D%7B3%2C30%7D%24&propertyName_25=UserNameJavaRegEx&propertyValue_25=%5Ba-zA-Z0-9._-%7C%2F%2F%5D%7B3%2C30%7D%24&propertyName_26=RoleNameJavaScriptRegEx&propertyValue_26=%5E%5B%5CS%5D%7B3%2C30%7D%24&propertyName_27=RoleNameJavaRegEx&propertyValue_27=%5Ba-zA-Z0-9._-%7C%2F%2F%5D%7B3%2C30%7D%24&propertyName_28=WriteGroups&propertyValue_28=on&propertyName_29=EmptyRolesAllowed&propertyValue_29=on&propertyName_30=MemberOfAttribute&propertyValue_30=member&propertyName_31=UniqueID&propertyValue_31=&defaultProperties=32%2F", label = "Raw url encoded string to create new user store. {..} are the templates to be substituted. ")
	private ConfigurationProperty<String> idpStoreTemplate;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(system = true, sensitive = true, name = "security.identity.permissions.role.admin", defaultValue = "/permission/admin;/permission/admin/configure;/permission/admin/configure/datasources;/permission/admin/configure/security;/permission/admin/configure/security/usermgt;/permission/admin/configure/security/usermgt/passwords;/permission/admin/configure/security/usermgt/profiles;/permission/admin/configure/security/usermgt/provisioning;/permission/admin/configure/security/usermgt/users;/permission/admin/configure/theme;/permission/admin/login;/permission/admin/manage;/permission/admin/manage/add;/permission/admin/manage/add/service;/permission/admin/manage/add/webapp;/permission/admin/manage/extensions;/permission/admin/manage/extensions/add;/permission/admin/manage/extensions/list;/permission/admin/manage/modify;/permission/admin/manage/modify/service;/permission/admin/manage/modify/user-profile;/permission/admin/manage/modify/webapp;/permission/admin/manage/resources;/permission/admin/manage/resources/browse;/permission/admin/manage/resources/notifications;/permission/admin/manage/search;/permission/admin/manage/search/advanced-search;/permission/admin/manage/search/resources;/permission/admin/monitor;/permission/admin/monitor/logging;/permission/admin/monitor/tenantUsage;/permission/admin/monitor/tenantUsage/customUsage", label = "; separated set of admin role permissions. See https://docs.wso2.com/display/IS500/Role-based+Permissions.")
	private ConfigurationProperty<String> idpAdminPermissions;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "security.sso.idpServicesPath", defaultValue = "/services/", system = true, sensitive = true, shared = false, label = "Path for the identity provider's services. The property need to start and end with /")
	private ConfigurationProperty<String> idpServicesPath;

	/**
	 * Getter method for idpServerUserId.
	 *
	 * @return the {@link #idpServerUserId}
	 */
	public ConfigurationProperty<String> getIdpServerUserId() {
		return idpServerUserId;
	}

	/**
	 * Getter method for idpServerUserPass.
	 *
	 * @return the {@link #idpServerUserPass}
	 */
	public ConfigurationProperty<String> getIdpServerUserPass() {
		return idpServerUserPass;
	}

	/**
	 * Getter method for idpServerURLConfig.
	 *
	 * @return the {@link #idpServerURLConfig}
	 */
	public ConfigurationProperty<String> getIdpServerURL() {
		return idpServerURL;
	}

	/**
	 * Getter method for idpStoreTemplate.
	 *
	 * @return the {@link #idpStoreTemplate}
	 */
	public ConfigurationProperty<String> getIdpStoreTemplate() {
		return idpStoreTemplate;
	}

	/**
	 * Permissions for admin role to assign on tenant init.
	 *
	 * @return the {@link #idpAdminPermissions}
	 */
	public ConfigurationProperty<String> getIdpAdminPermissions() {
		return idpAdminPermissions;
	}

	/**
	 * Path for the idp's own services.
	 *
	 * @return the idpServicesPath
	 */
	public ConfigurationProperty<String> getIdpServicesPath() {
		return idpServicesPath;
	}
}
