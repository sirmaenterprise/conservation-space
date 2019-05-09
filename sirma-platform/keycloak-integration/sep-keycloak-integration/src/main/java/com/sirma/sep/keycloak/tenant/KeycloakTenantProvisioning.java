package com.sirma.sep.keycloak.tenant;

import static com.sirma.sep.keycloak.ClientProperties.OIDC_LOGIN_PROTOCOL;
import static com.sirma.sep.keycloak.ClientProperties.PROTOCOL_MAPPER_ACCESS_TOKEN;
import static com.sirma.sep.keycloak.ClientProperties.PROTOCOL_MAPPER_CLAIM_NAME;
import static com.sirma.sep.keycloak.ClientProperties.PROTOCOL_MAPPER_ID_TOKEN;
import static com.sirma.sep.keycloak.ClientProperties.PROTOCOL_MAPPER_SCRIPT;
import static com.sirma.sep.keycloak.ClientProperties.PROTOCOL_MAPPER_USER_ATTRIBUTE;
import static com.sirma.sep.keycloak.ClientProperties.PROTOCOL_MAPPER_USER_INFO_TOKEN;
import static com.sirma.sep.keycloak.ClientProperties.PROTOCOL_MAPPER_VALUE_TYPE;
import static com.sirma.sep.keycloak.ClientProperties.SCRIPT_PROTOCOL_MAPPER;
import static com.sirma.sep.keycloak.ClientProperties.SEP_BACKEND_CLIENT_ID;
import static com.sirma.sep.keycloak.ClientProperties.SEP_EAI_CLIENT_ID;
import static com.sirma.sep.keycloak.ClientProperties.SEP_THEME;
import static com.sirma.sep.keycloak.ClientProperties.SEP_UI_CLIENT_ID;
import static com.sirma.sep.keycloak.ClientProperties.TENANT_MAPPER_NAME;
import static com.sirma.sep.keycloak.ClientProperties.USERNAME_CLAIM_NAME;
import static com.sirma.sep.keycloak.ClientProperties.USERNAME_MAPPER_NAME;
import static com.sirma.sep.keycloak.ClientProperties.USER_MODEL_PROPERTY_MAPPER;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.RolesRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.util.SecurityUtil;
import com.sirma.itt.seip.tenant.provision.IdpTenantInfo;
import com.sirma.itt.seip.tenant.provision.IdpTenantProvisioning;
import com.sirma.itt.seip.tenant.step.TenantIdpStep;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.sep.keycloak.ClientProperties;
import com.sirma.sep.keycloak.config.KeycloakConfiguration;
import com.sirma.sep.keycloak.exception.KeycloakClientException;
import com.sirma.sep.keycloak.flow.KeycloakAuthFlowConfigurator;
import com.sirma.sep.keycloak.ldap.KeycloakLdapProvider;
import com.sirma.sep.keycloak.producers.KeycloakClientProducer;
import com.sirma.sep.keycloak.util.KeycloakApiUtil;

/**
 * Keycloak tenant provisioning implementation.
 *
 * @author smustafov
 */
@Extension(target = TenantIdpStep.PROVISION, order = 2)
public class KeycloakTenantProvisioning implements IdpTenantProvisioning {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static final String REALM_MANAGEMENT_CLIENT_ID = "realm-management";
	public static final String REALM_ADMIN_ROLE = "realm-admin";

	static final String DEFAULT_POLICY = "length(8) and upperCase(1) and lowerCase(1) and digits(1) and specialChars(1) and notUsername(undefined)";
	static final String NAME = "keycloak";

	@Inject
	private KeycloakClientProducer keycloakClientProducer;

	@Inject
	private KeycloakLdapProvider keycloakLdapProvider;

	@Inject
	private KeycloakConfiguration keycloakConfiguration;

	@Inject
	private KeycloakMailConfigurator keycloakMailConfigurator;

	@Inject
	private SystemConfiguration systemConfiguration;

	@Override
	public boolean isApplicable(String idpName) {
		return NAME.equals(idpName);
	}

	/**
	 * Provisions tenant in Keycloak. It consists of several steps:
	 * <ol>
	 * <li>creates realm for the tenant with configured authentication clients</li>
	 * <li>creates LDAP storage provider for the tenant</li>
	 * <li>creates admin group with admin role assigned to it</li>
	 * <li>creates admin user as member of the admin group</li>
	 * </ol>
	 *
	 * @param tenantInfo info about the tenant to be created
	 */
	@Override
	public void provision(IdpTenantInfo tenantInfo) {
		try {
			TimeTracker timeTracker = TimeTracker.createAndStart();

			Keycloak keycloakClient = keycloakClientProducer.produceClient();

			createRealm(keycloakClient, tenantInfo);

			configureAuthFlow(keycloakClient, tenantInfo);

			createLdapProvider(keycloakClient, tenantInfo.getTenantId());

			String adminGroupId = createAdminGroup(keycloakClient, tenantInfo.getTenantId());

			createAdminUser(keycloakClient, tenantInfo, adminGroupId);

			LOGGER.info("Created keycloak tenant in {} seconds", timeTracker.stopInSeconds());
		} catch (WebApplicationException e) {
			throw new KeycloakClientException(KeycloakApiUtil.readErrorMessage(e.getResponse()), e);
		} catch (Exception e) {
			throw new KeycloakClientException("Failed to create tenant in Keycloak IdP", e);
		}
	}

	private void configureAuthFlow(Keycloak keycloakClient, IdpTenantInfo tenantInfo) {
		KeycloakAuthFlowConfigurator.configure(keycloakClient.realm(tenantInfo.getTenantId()));
	}

	private void createRealm(Keycloak keycloakClient, IdpTenantInfo tenantInfo) {
		RealmRepresentation realm = new RealmRepresentation();
		realm.setId(tenantInfo.getTenantId());
		realm.setRealm(tenantInfo.getTenantId());
		realm.setDisplayName(tenantInfo.getTenantDisplayName());
		realm.setDisplayNameHtml(tenantInfo.getTenantDescription());
		realm.setEnabled(true);
		realm.setRegistrationAllowed(false);
		realm.setVerifyEmail(false);
		realm.setLoginWithEmailAllowed(false);
		realm.setDuplicateEmailsAllowed(true);
		realm.setResetPasswordAllowed(true);
		realm.setEditUsernameAllowed(false);
		realm.setRememberMe(true);
		realm.setEventsListeners(Arrays.asList("jboss-logging", "jms"));
		// maximum time a user session can remain active, regardless of activity
		realm.setSsoSessionMaxLifespan(ClientProperties.ONE_WEEK_IN_SECONDS);
		realm.setPasswordPolicy(DEFAULT_POLICY);

		configureUserRoles(realm);
		configureClients(realm);
		configureSmtpServer(realm);
		configureTheme(realm);

		keycloakClient.realms().create(realm);
	}

	private void configureSmtpServer(RealmRepresentation realm) {
		keycloakMailConfigurator.configureSmtpServer(realm);
	}

	private void configureUserRoles(RealmRepresentation realm) {
		// Keycloak requires a role for the users in order to authenticate
		RolesRepresentation roles = new RolesRepresentation();

		RoleRepresentation userRole = new RoleRepresentation();
		userRole.setName(keycloakConfiguration.getKeycloakUserRole().get());
		roles.setRealm(Collections.singletonList(userRole));

		realm.setRoles(roles);
		realm.setDefaultRoles(Collections.singletonList(keycloakConfiguration.getKeycloakUserRole().get()));
	}

	private void configureClients(RealmRepresentation realm) {
		// SEP OpenID Connect clients used for authentication (equivalent to service providers in WSO)

		ClientRepresentation backendClient = new ClientRepresentation();
		backendClient.setClientId(SEP_BACKEND_CLIENT_ID);
		backendClient.setEnabled(true);
		backendClient.setProtocol(OIDC_LOGIN_PROTOCOL);
		backendClient.setBearerOnly(true);
		backendClient.setPublicClient(false);

		ClientRepresentation uiClient = new ClientRepresentation();
		uiClient.setClientId(SEP_UI_CLIENT_ID);
		uiClient.setEnabled(true);
		uiClient.setProtocol(OIDC_LOGIN_PROTOCOL);
		uiClient.setBearerOnly(false);
		uiClient.setPublicClient(true);
		uiClient.setStandardFlowEnabled(true);
		uiClient.setDirectAccessGrantsEnabled(true);
		uiClient.setFullScopeAllowed(false);
		// restrict this client to be used only by UI2 for improved security
		uiClient.setRedirectUris(Collections.singletonList(getUi2Url() + "*"));
		uiClient.setWebOrigins(Collections.singletonList("+"));
		uiClient.setBaseUrl(getUi2Url());
		uiClient.setProtocolMappers(Arrays.asList(createTenantProtocolMapper(), createUsernameProtocolMapper()));

		ClientRepresentation eaiClient = new ClientRepresentation();
		eaiClient.setClientId(SEP_EAI_CLIENT_ID);
		eaiClient.setEnabled(true);
		eaiClient.setProtocol(OIDC_LOGIN_PROTOCOL);
		eaiClient.setPublicClient(true);
		eaiClient.setBearerOnly(false);
		eaiClient.setStandardFlowEnabled(true);
		eaiClient.setFullScopeAllowed(false);
		eaiClient.setRedirectUris(Collections.singletonList("http://localhost:*"));
		eaiClient.setProtocolMappers(Arrays.asList(createTenantProtocolMapper(), createUsernameProtocolMapper()));

		realm.setClients(Arrays.asList(backendClient, uiClient, eaiClient));
	}

	private void configureTheme(RealmRepresentation realm) {
		realm.setLoginTheme(SEP_THEME);
		realm.setEmailTheme(SEP_THEME);
	}

	/**
	 * Creates protocol mapper that will include the tenant as property in the tokens. Uses the script based
	 * mapper, because the others do not have the realm(tenant) property.
	 *
	 * @return the created protocol mapper
	 */
	private ProtocolMapperRepresentation createTenantProtocolMapper() {
		ProtocolMapperRepresentation protocolMapperRepresentation = new ProtocolMapperRepresentation();
		protocolMapperRepresentation.setName(TENANT_MAPPER_NAME);
		protocolMapperRepresentation.setProtocol(OIDC_LOGIN_PROTOCOL);
		protocolMapperRepresentation.setProtocolMapper(SCRIPT_PROTOCOL_MAPPER);

		Map<String, String> config = new HashMap<>();
		config.put(PROTOCOL_MAPPER_CLAIM_NAME, TENANT_MAPPER_NAME);
		config.put(PROTOCOL_MAPPER_ACCESS_TOKEN, "true");
		config.put(PROTOCOL_MAPPER_ID_TOKEN, "true");
		config.put(PROTOCOL_MAPPER_USER_INFO_TOKEN, "true");
		config.put(PROTOCOL_MAPPER_VALUE_TYPE, "String");
		config.put(PROTOCOL_MAPPER_SCRIPT, "realm.getId();");

		protocolMapperRepresentation.setConfig(config);
		return protocolMapperRepresentation;
	}

	/**
	 * Creates protocol mapper that will include the user's username in the tokens. The username is without the tenant
	 * identifier.
	 * By default Keycloak creates default set of mappers (including for username) when creating new clients, but
	 * when creating clients via the rest api with custom mappers, it does not create default mappers.
	 *
	 * @return the created protocol mapper
	 */
	private ProtocolMapperRepresentation createUsernameProtocolMapper() {
		ProtocolMapperRepresentation protocolMapperRepresentation = new ProtocolMapperRepresentation();
		protocolMapperRepresentation.setName(USERNAME_MAPPER_NAME);
		protocolMapperRepresentation.setProtocol(OIDC_LOGIN_PROTOCOL);
		protocolMapperRepresentation.setProtocolMapper(USER_MODEL_PROPERTY_MAPPER);

		Map<String, String> config = new HashMap<>();
		config.put(PROTOCOL_MAPPER_CLAIM_NAME, USERNAME_CLAIM_NAME);
		config.put(PROTOCOL_MAPPER_ACCESS_TOKEN, "true");
		config.put(PROTOCOL_MAPPER_ID_TOKEN, "true");
		config.put(PROTOCOL_MAPPER_USER_INFO_TOKEN, "true");
		config.put(PROTOCOL_MAPPER_VALUE_TYPE, "String");
		config.put(PROTOCOL_MAPPER_USER_ATTRIBUTE, USERNAME_MAPPER_NAME);

		protocolMapperRepresentation.setConfig(config);
		return protocolMapperRepresentation;
	}

	private String getUi2Url() {
		return systemConfiguration.getUi2Url().requireConfigured().get();
	}

	private void createLdapProvider(Keycloak keycloakClient, String tenantId) {
		keycloakLdapProvider.createLdapProvider(keycloakClient, tenantId);
	}

	protected String createAdminGroup(Keycloak keycloakClient, String tenantId) {
		GroupRepresentation adminGroup = new GroupRepresentation();
		adminGroup.setName("admin");

		// create the group
		RealmResource realm = keycloakClient.realm(tenantId);
		Response response = realm.groups().add(adminGroup);
		String adminGroupId = KeycloakApiUtil.getCreatedId(response);
		response.close();

		assignAdminRole(realm, adminGroupId);

		return adminGroupId;
	}

	protected void assignAdminRole(RealmResource realm, String adminGroupId) {
		// fetch admin role
		ClientRepresentation realmManagementClient = realm.clients().findByClientId(REALM_MANAGEMENT_CLIENT_ID).get(0);
		RoleResource adminRoleResource = realm.clients().get(realmManagementClient.getId()).roles()
				.get(REALM_ADMIN_ROLE);

		// assign admin role to the group
		RoleMappingResource groupRoles = realm.groups().group(adminGroupId).roles();
		groupRoles.clientLevel(realmManagementClient.getId())
				.add(Collections.singletonList(adminRoleResource.toRepresentation()));
	}

	protected void createAdminUser(Keycloak keycloakClient, IdpTenantInfo tenantInfo, String adminGroupId) {
		UserRepresentation adminUser = new UserRepresentation();
		adminUser.setUsername(getAdminUsername(tenantInfo));
		adminUser.setEnabled(true);
		adminUser.setEmail(tenantInfo.getAdminMail());
		adminUser.setFirstName(tenantInfo.getAdminFirstName());
		adminUser.setLastName(tenantInfo.getAdminLastName());

		// create the user
		RealmResource realm = keycloakClient.realm(tenantInfo.getTenantId());
		Response response = realm.users().create(adminUser);
		String adminUserId = KeycloakApiUtil.getCreatedId(response);
		response.close();

		UserResource userResource = realm.users().get(adminUserId);

		// join admin group
		userResource.joinGroup(adminGroupId);

		// set password after the user is created for proper pattern validation response
		CredentialRepresentation credential = new CredentialRepresentation();
		credential.setValue(tenantInfo.getAdminPassword());
		credential.setType(CredentialRepresentation.PASSWORD);
		credential.setTemporary(false);
		userResource.resetPassword(credential);
	}

	private static String getAdminUsername(IdpTenantInfo tenantInfo) {
		return SecurityUtil.getUserWithoutTenant(tenantInfo.getAdminUsername());
	}

	/**
	 * Deletes all data about the tenant from LDAP and Keycloak IdP.
	 *
	 * @param tenantId the id of the tenant that will be deleted
	 */
	@Override
	public void delete(String tenantId) {
		try {
			Keycloak keycloakClient = keycloakClientProducer.produceClient();

			// delete ldap data
			keycloakLdapProvider.deleteLdapProvider(keycloakClient, tenantId);

			// delete the realm
			keycloakClient.realm(tenantId).remove();
		} catch (Exception e) {
			throw new KeycloakClientException("Failed to delete tenant from Keycloak IdP", e);
		}
	}

}
