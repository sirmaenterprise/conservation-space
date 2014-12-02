package com.sirma.itt.emf.security;

import com.sirma.itt.emf.configuration.Configuration;
import com.sirma.itt.emf.util.Documentation;

/**
 * Configuration properties for the security functionality.
 * 
 * @author Adrian Mitev
 */
@Documentation("Base security configuration properties.")
public interface SecurityConfigurationProperties extends Configuration {

	/** Path to signature validation trust store */
	@Documentation("Path to the keystore holding the cerificate used for SAML response signature validation")
	String TRUSTSTORE_PATH = "security.trustStore.path";

	/** Password for the signature validation trust store */
	@Documentation("Password for the keystore holding the cerificate used for SAML response signature validation")
	String TRUSTSTORE_PASSWORD = "security.trustStore.password";

	/** The CMF encrypt key. */
	@Documentation("Encription key to use when communicating with DMS. <br><b>NOTE: </b> the same key need to be defined in DMS server to work properly.")
	String SECURITY_ENCRYPT_KEY = "cmf.encrypt.key";

	/** is sso enabled. */
	@Documentation("Option to enable or disable Single Sigh On(SSO). <b>Default value is: false</b>.")
	String SECURITY_SSO_ENABLED = "security.sso.enabled";
	/** use 10 minutes gap of valid saml */
	@Documentation("To enable user token update against current time of the SAML token")
	String SECURITY_SSO_TIME_CONSTRAINTS_USED = "security.sso.useTimeConstraints";

}
