package com.sirma.itt.seip.security.configuration;

import java.io.Serializable;
import java.security.KeyStore;

import javax.crypto.SecretKey;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.security.User;

/**
 * Configuration properties for the security functionality.
 *
 * @author Adrian Mitev
 */
public interface SecurityConfiguration extends Serializable {

	String WSO_IDP = "wso2Idp";
	String KEYCLOAK_IDP = "keycloak";

	/**
	 * Gets the trust store.
	 *
	 * @return the trust store
	 */
	ConfigurationProperty<KeyStore> getTrustStore();

	/**
	 * Gets the encript key.
	 *
	 * @return the encript key
	 */
	ConfigurationProperty<SecretKey> getCryptoKey();

	/**
	 * Checks if is sso time contraints used.
	 *
	 * @return the configuration property
	 */
	ConfigurationProperty<Boolean> isSSOTimeContraintsUsed();

	/**
	 * Gets the system user name configuration. It's the user identifier
	 *
	 * @return the system user name
	 */
	ConfigurationProperty<String> getSystemUserName();

	/**
	 * Gets the system user name.
	 *
	 * @return the system user name
	 */
	ConfigurationProperty<User> getSystemUser();

	/**
	 * Gets the admin user name.
	 *
	 * @return the admin user name
	 */
	ConfigurationProperty<String> getAdminUserName();

	/**
	 * Gets the admin user password.
	 *
	 * @return the admin user password
	 */
	ConfigurationProperty<String> getAdminUserPassword();

	/**
	 * Gets the admin password configuration.
	 *
	 * @return the admin password configuration
	 */
	String getAdminPasswordConfiguration();

	/**
	 * Gets the admin user.
	 *
	 * @return the admin user
	 */
	ConfigurationProperty<User> getAdminUser();

	/**
	 * Gets the system admin username.
	 *
	 * @return the system admin username.
	 */
	String getSystemAdminUsername();

	/**
	 * Gets the system admin user resource.
	 *
	 * @return the system admin user.
	 */
	User getSystemAdminUser();

	/**
	 * Gets the admin group.
	 *
	 * @return the admin group
	 */
	ConfigurationProperty<String> getAdminGroup();

	/**
	 * Gets the manager role name used for permission assignments
	 *
	 * @return the manager role
	 */
	ConfigurationProperty<String> getManagerRole();

	/**
	 * Returns string configuration of the IdP provider name.
	 *
	 * @return string configuration of the IdP provider name
	 */
	ConfigurationProperty<String> getIdpProviderName();

}
