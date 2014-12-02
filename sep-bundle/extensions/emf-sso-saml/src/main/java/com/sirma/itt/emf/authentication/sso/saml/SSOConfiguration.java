package com.sirma.itt.emf.authentication.sso.saml;

import com.sirma.itt.emf.configuration.Configuration;
import com.sirma.itt.emf.util.Documentation;

/**
 * SSO module specific configuration.
 *
 * @author BBonev
 */
@Documentation("SSO module specific configuration")
public interface SSOConfiguration extends Configuration {

	/** url for sso server. */
	@Documentation("Identity provider server URL used for SSO. <br><b>NOTE: </b> "
			+ "The property need to end with the IP address of the calling machine (JBOSS server host name)."
			+ " The same address that is configured in the SSO server. <b>Default value is: https://127.0.0.1:9448/samlsso</b>")
	String SECURITY_SSO_IDP_URL = "security.sso.idpUrl";

	@Documentation("Identity provider server URL used for SSO. <br><b>NOTE: </b> "
			+ "The property need to end with the IP address of the calling machine (JBOSS server host name)."
			+ " The same address that is configured in the SSO server. <b>Example value is: https://127.0.0.1:9448/commonauth</b>")
	String SECURITY_SSO_IDP_AUTH_CUSTOM_URL = "security.sso.idpUrl.customAuth";

	@Documentation("IDP server admin username.")
	String SECURITY_IDP_SERVER_ADMIN_USERNAME = "security.identity.server.admin.username";

	@Documentation("IDP server admin password.")
	String SECURITY_IDP_SERVER_ADMIN_PASSWORD = "security.identity.server.admin.password";

	@Documentation("Identifier of the issuer (this application) that will be send to the IdP when requesting security tokens")
	String ISSUER_ID = "security.sso.issuerId";

	/** Enable signature validation */
	@Documentation("Indicates if the API should expect the security token to be signed."
			+ " If <b>true</b> the signature will be validated agains a key in a keystore defined in <code>security.sso.trustStore.path</code> property."
			+ "Default value is: false")
	String SIGNATURE_VALIDATION_ENABLED = "security.sso.signatureValidation.enabled";

	/** Aliased for the certificate used for signature validation */
	@Documentation("Alias of the certificate used for SAML response signature validation")
	String SIGNATURE_TRUSTSTORE_CERTIFICATE_ALIAS = "security.sso.trustStore.cert.alias";

	/** Password for the certificate used for signature validation */
	@Documentation("Password of the certificate used for SAML response signature validation")
	String SIGNATURE_TRUSTSTORE_CERTIFICATE_PASSWORD = "security.sso.trustStore.cert.password";

	@Documentation("Timer rate in minutes to display the current logged in user. Default is 15")
	String INFORMATION_RATE_LOGGING = "security.info.loggedusers.rate";
}
