package com.sirma.itt.seip.security.configuration;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.exception.SecurityException;
import com.sirma.itt.seip.security.util.SecurityUtil;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Configuration properties for the security functionality. NOTE: Probably we should have this class return the admin
 * user instead we expose all the properties.
 *
 * @author Adrian Mitev
 * @author BBonev
 * @author bbanchev
 */
@ApplicationScoped
public class SecurityConfigurationImpl implements SecurityConfiguration {
	private static final long serialVersionUID = 4291518018239855880L;

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@ConfigurationPropertyDefinition(system = true, sensitive = true, label = "Path to the keystore holding the cerificate used for SAML response signature validation")
	private static final String TRUST_STORE_PATH = "security.trustStore.path";

	@ConfigurationPropertyDefinition(system = true, sensitive = true, password = true, label = "Password for the keystore holding the cerificate used for SAML response signature validation")
	private static final String TRUST_STORE_PWD = "security.trustStore.password"; //NOSONAR

	@ConfigurationGroupDefinition(system = true, properties = { TRUST_STORE_PATH,
			TRUST_STORE_PWD }, type = KeyStore.class, label = "System secure trust store")
	private static final String SECURITY_TRUST_STORE = "security.trustStore";

	@Inject
	@Configuration(SECURITY_TRUST_STORE)
	private ConfigurationProperty<KeyStore> trustKeyStore;

	@ConfigurationPropertyDefinition(defaultValue = "somePassword", system = true, password = true, sensitive = true, type = SecretKey.class, label = "Encription key to use when communicating with DMS. <br><b>NOTE: </b> the same key need to be defined in DMS server to work properly.")
	private static final String ENCRYPT_KEY = "cmf.encrypt.key";

	/** The CMF encrypt key. */
	@Inject
	@Configuration(ENCRYPT_KEY)
	private ConfigurationProperty<SecretKey> cryptoKey;

	/** use 10 minutes gap of valid saml. */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "security.sso.useTimeConstraints", system = true, sensitive = true, type = Boolean.class, defaultValue = "false", label = "To enable user token update against current time of the SAML token")
	private ConfigurationProperty<Boolean> ssoTimeConstraintsUsed;

	@ConfigurationPropertyDefinition(defaultValue = "System", label = "User name of for the system user. Typically System")
	static final String SYSTEM_USER_NAME = "security.user.system.name";

	@ConfigurationPropertyDefinition(defaultValue = "System", label = "Display name of for the system user")
	static final String SYSTEM_USER_DISPLAYNAME = "security.user.system.displayname";

	@ConfigurationPropertyDefinition(sensitive = true, password = true, label = "The system user password. If SSO/SAML is enabled this property is not used.")
	static final String SYSTEM_USER_PWD = "security.user.system.password"; //NOSONAR

	@ConfigurationPropertyDefinition(defaultValue = "admin", label = "The admin user name. This user is used to connect to DMS when no other authenticated user is logged in.")
	static final String ADMIN_NAME = "admin.username";

	/** The Constant ADMIN_PASSWORD. */
	@ConfigurationPropertyDefinition(sensitive = true, password = true, label = "The admin user password. WARNING: This value should be changed after the password is changed via the 'Change password' dialog for the admin user!")
	static final String ADMIN_PWD = "admin.password"; //NOSONAR

	@ConfigurationGroupDefinition(type = User.class, properties = { ADMIN_NAME,
			ADMIN_PWD }, label = "Admin user instance.")
	private static final String ADMIN_USER = "admin.user";

	@ConfigurationGroupDefinition(type = User.class, properties = { SYSTEM_USER_NAME, SYSTEM_USER_PWD,
			SYSTEM_USER_DISPLAYNAME }, label = "System user instance.")
	private static final String SYSTEM_USER = "security.user.system.user";

	@ConfigurationPropertyDefinition(defaultValue = "System Administrator", system = true, label = "The sysem admin display name.")
	private static final String SYSTEMADMIN_USER_DISPLAYNAME = "security.user.systemadmin.displayName";

	@ConfigurationGroupDefinition(type = User.class, system = true, label = "System user instance.", properties = {
			SYSTEMADMIN_USER_DISPLAYNAME })
	private static final String SYSTEMADMIN_USER = "security.user.systemadmin.user";

	@Inject
	@Configuration(ADMIN_NAME)
	private ConfigurationProperty<String> adminUsername;

	@Inject
	@Configuration(ADMIN_PWD)
	private ConfigurationProperty<String> adminPassword;

	@Inject
	@Configuration(ADMIN_USER)
	private ConfigurationProperty<User> adminUser;

	@Inject
	@Configuration(SYSTEM_USER_NAME)
	private ConfigurationProperty<String> systemUserName;

	@Inject
	@Configuration(SYSTEM_USER)
	private ConfigurationProperty<User> systemUser;

	@Inject
	@Configuration(SYSTEMADMIN_USER)
	private ConfigurationProperty<User> systemAdminUser;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "admin.groupname", sensitive = true, label = "The admin group name.The default is null, so no group is considered as admin.")
	private ConfigurationProperty<String> adminGroupName;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "security.permissions.roleid.manager", defaultValue = "MANAGER", label = "The id of manager system role")
	private ConfigurationProperty<String> managerRoleConfig;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "security.idp.provider", defaultValue = SecurityConfiguration.KEYCLOAK_IDP, sensitive = true, label = "The name of the IdP provider that will be used for this tenant. One of the possible values are: keycloak (default), wso2Idp")
	private ConfigurationProperty<String> idpProviderName;

	private static User systemAdminUserCached = null;

	/**
	 * Builds the key store.
	 *
	 * @param context
	 *            the context
	 * @return the key store
	 */
	@ConfigurationConverter(SECURITY_TRUST_STORE)
	static KeyStore buildKeyStore(GroupConverterContext context) {
		if (context.getValue(TRUST_STORE_PATH).isSet()) {
			context.getValue(TRUST_STORE_PWD).requireConfigured(
					"The trust store password must be set. Missing configuration: " + TRUST_STORE_PWD);

			String path = context.get(TRUST_STORE_PATH);
			String password = context.get(TRUST_STORE_PWD);

			System.setProperty("javax.net.ssl.trustStore", path);
			System.setProperty("javax.net.ssl.trustStorePassword", password);

			try {
				return SecurityUtil.buildKeyStore(path, password);
			} catch (SecurityException e) {
				throw new ConfigurationException(e);
			}
		}
		return null;
	}

	/**
	 * Initializes the trust store as soon as the deployment is started because of a race condition and initializing the
	 * SSLContext before setting the trusted store in the system properties
	 */
	@Startup(phase = StartupPhase.DEPLOYMENT, order = 0.1)
	public void initTrustStore() {
		getTrustStore().requireConfigured();
	}

	/**
	 * Builds the secret key.
	 *
	 * @param context
	 *            the context
	 * @return the secret key
	 */
	@ConfigurationConverter
	static SecretKey buildSecretKey(ConverterContext context) {
		try {
			return SecurityUtil.createSecretKey(context.getRawValue());
		} catch (SecurityException e) {
			throw new ConfigurationException(e);
		}
	}

	/**
	 * Builds the admin user.
	 *
	 * @param context
	 *            the context
	 * @param userStore
	 *            the user store
	 * @param securityContext
	 *            the security context
	 * @return the tenant admin user
	 */
	@ConfigurationConverter(ADMIN_USER)
	static User buildAdminUser(GroupConverterContext context, UserStore userStore, SecurityContext securityContext) {
		String currentTenantId = securityContext.getCurrentTenantId();
		String adminName = context.get(ADMIN_NAME);
		String password = context.get(ADMIN_PWD);
		String fullIdentity = SecurityUtil.buildTenantUserId(adminName, currentTenantId);
		AdminUser adminUser = new AdminUser(fullIdentity, currentTenantId, password, "Admin " + currentTenantId);
		return userStore.wrap(adminUser);
	}

	/**
	 * Builds the system user.
	 *
	 * @param context
	 *            the context
	 * @param userStore
	 *            the user store
	 * @param securityContext
	 *            the security context
	 * @return the tenant system user
	 */
	@ConfigurationConverter(SYSTEM_USER)
	static User buildSystemUser(GroupConverterContext context, UserStore userStore, SecurityContext securityContext) {
		String currentTenantId = securityContext.getCurrentTenantId();
		if (SecurityContext.isSystemTenant(currentTenantId)) {
			LOGGER.error("System user for system tenant should not be requested!");
		}
		String systemUserName = SecurityContext.SYSTEM_USER_NAME;
		String password = context.get(SYSTEM_USER_PWD);
		String displayName = context.get(SYSTEM_USER_DISPLAYNAME);
		String fullIdentity = SecurityUtil.buildTenantUserId(systemUserName, currentTenantId);
		SystemUser systemUser = new SystemUser(fullIdentity, currentTenantId, null, password, displayName, false);
		return userStore.wrap(systemUser);
	}

	/**
	 * Builds the system admin user.
	 *
	 * @param context
	 *            the context
	 * @param userStore
	 *            the user store
	 * @return the tenant system user
	 */
	@ConfigurationConverter(SYSTEMADMIN_USER)
	static User buildSystemAdminUser(GroupConverterContext context, UserStore userStore) {
		String currentTenantId = SecurityContext.SYSTEM_TENANT;
		String systemUserName = SecurityContext.getSystemAdminName();
		String fullIdentity = SecurityUtil.buildTenantUserId(systemUserName, currentTenantId);
		SystemUser systemUser = new SystemUser(fullIdentity, currentTenantId, null, null,
				context.get(SYSTEMADMIN_USER_DISPLAYNAME), true);
		return userStore.wrap(systemUser);
	}

	@Override
	public ConfigurationProperty<KeyStore> getTrustStore() {
		return trustKeyStore;
	}

	@Override
	public ConfigurationProperty<SecretKey> getCryptoKey() {
		return cryptoKey;
	}

	@Override
	public ConfigurationProperty<Boolean> isSSOTimeContraintsUsed() {
		return ssoTimeConstraintsUsed;
	}

	@Override
	public ConfigurationProperty<String> getAdminUserName() {
		return adminUsername;
	}

	@Override
	public ConfigurationProperty<String> getSystemUserName() {
		return systemUserName;
	}

	@Override
	public ConfigurationProperty<String> getAdminUserPassword() {
		return adminPassword;
	}

	@Override
	public String getAdminPasswordConfiguration() {
		return ADMIN_PWD;
	}

	@Override
	public ConfigurationProperty<String> getAdminGroup() {
		return adminGroupName;
	}

	@Override
	public User getSystemAdminUser() {
		if (systemAdminUserCached == null) {
			setSystemAdminUserCache(systemAdminUser.get());
		}
		return systemAdminUserCached;
	}

	private static void setSystemAdminUserCache(User user) {
		systemAdminUserCached = user;
	}

	@Override
	public ConfigurationProperty<User> getAdminUser() {
		return adminUser;
	}

	@Override
	public ConfigurationProperty<User> getSystemUser() {
		return systemUser;
	}

	@Override
	public String getSystemAdminUsername() {
		return getSystemAdminUser().getIdentityId();
	}

	@Override
	public ConfigurationProperty<String> getManagerRole() {
		return managerRoleConfig;
	}

	@Override
	public ConfigurationProperty<String> getIdpProviderName() {
		return idpProviderName;
	}

	/**
	 * Extracted base logic for system and admin users
	 *
	 * @author bbanchev
	 */
	private abstract static class PredefinedUser implements User {

		private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

		private static final long serialVersionUID = 1L;

		private static final String PROP_IS_DELETED = "emf:isDeleted";
		/** The is active. Property that defines if the instance is in active state or not. */
		private static final String PROP_IS_ACTIVE = "emf:isActive";
		private static final String PROP_TITLE = "title";

		/** The token. */
		private String ticket;

		private String fullIdentity;

		private String tenantId;

		private String dbId;

		private Map<String, Serializable> properties;

		protected Serializable credential;

		protected String displayName;

		PredefinedUser(String identity, String tenantId, String ticket, Serializable credential,
				Map<String, Serializable> properties) {
			fullIdentity = identity;
			this.tenantId = tenantId;
			this.ticket = ticket;
			this.credential = credential;
			this.properties = properties;
		}

		/**
		 * Gets the tenant id.
		 *
		 * @return the tenant id
		 */
		@Override
		public String getTenantId() {
			return tenantId;
		}

		/**
		 * Gets the system id.
		 *
		 * @return the system id
		 */
		@Override
		public Serializable getSystemId() {
			if (dbId == null) {
				dbId = generateResourceDbId();
			}
			return dbId;
		}

		protected String generateResourceDbId() {
			try {
				String updated = getIdentityId().replace('@', '-');
				return "emf:" + URIUtil.encodeWithinPath(updated, "UTF-8");
			} catch (URIException e) {
				LOG.warn("Detected invalid value [{}] for semantic URI.", getIdentityId());
				LOG.trace("Detected invalid value [{}] for semantic URI.", getIdentityId(), e);
				return "emf:" + UUID.randomUUID().toString();
			}
		}

		/**
		 * Gets the properties.
		 *
		 * @return the properties
		 */
		@Override
		public Map<String, Serializable> getProperties() {
			return properties;
		}

		/**
		 * Gets the display name.
		 *
		 * @return the display name
		 */
		@Override
		public String getDisplayName() {
			return displayName;
		}

		/**
		 * Sets the display name.
		 *
		 * @param displayName
		 *            the new display name
		 */
		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		/**
		 * Gets the language.
		 *
		 * @return the language
		 */
		@Override
		public String getLanguage() {
			return "en";
		}

		/**
		 * Gets the identity id.
		 *
		 * @return the identity id
		 */
		@Override
		public String getIdentityId() {
			return fullIdentity;
		}

		/**
		 * Gets the ticket.
		 *
		 * @return the ticket
		 */
		@Override
		public String getTicket() {
			return ticket;
		}

		protected void setTicket(String ticket) {
			this.ticket = ticket;
		}

		@Override
		public Object getCredentials() {
			return credential;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(this.getClass().getSimpleName());
			builder.append(" Username=");
			builder.append(fullIdentity);
			builder.append(", tenantId=");
			builder.append(tenantId);
			return builder.toString();
		}

		@Override
		public int hashCode() {
			final int PRIME = 31;
			int result = 1;
			result = PRIME * result + (fullIdentity == null ? 0 : fullIdentity.hashCode());
			result = PRIME * result + (tenantId == null ? 0 : tenantId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof User)) {
				return false;
			}
			User other = (User) obj;
			return EqualsHelper.nullSafeEquals(fullIdentity, other.getIdentityId())
					&& EqualsHelper.nullSafeEquals(tenantId, other.getTenantId());
		}

		@Override
		public TimeZone getTimezone() {
			return TimeZone.getDefault();
		}
	}

	/**
	 * Admin user instance
	 *
	 * @author BBonev
	 */
	private static final class AdminUser extends PredefinedUser {
		private static final long serialVersionUID = 5338782599550233030L;

		AdminUser(String identifier, String tenantId, String password, String displayName) {
			super(identifier, tenantId, null, password, new HashMap<>(3));
			getProperties().put(PredefinedUser.PROP_IS_ACTIVE, Boolean.TRUE);
			getProperties().put(PredefinedUser.PROP_IS_DELETED, Boolean.FALSE);
			setDisplayName(displayName);
		}

		@Override
		public boolean isActive() {
			return true;
		}

	}

	/**
	 * User implementation for the system user.
	 *
	 * @author BBonev
	 */
	private static final class SystemUser extends PredefinedUser {

		private static final long serialVersionUID = 892761965855617614L;
		private final boolean canLogin;

		SystemUser(String identity, String tenantId, String ticket, String password, String displayName,
				boolean canLogin) {
			super(identity, tenantId, ticket, password, new HashMap<>(3));
			this.canLogin = canLogin;
			setDisplayName(displayName);
			getProperties().put(PredefinedUser.PROP_TITLE, displayName);
			getProperties().put(PredefinedUser.PROP_IS_DELETED, Boolean.FALSE);
			getProperties().put(PredefinedUser.PROP_IS_ACTIVE, Boolean.TRUE);
		}

		@Override
		public boolean isActive() {
			return true;
		}

		@Override
		public boolean canLogin() {
			return canLogin;
		}
	}
}
