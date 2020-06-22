package com.sirma.itt.emf.authentication.sso.saml;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.opensaml.xml.security.Criteria;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.KeyStoreCredentialResolver;
import org.opensaml.xml.security.criteria.EntityIDCriteria;

import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.idp.config.IDPConfiguration;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;

/**
 * SSO module specific configuration.
 *
 * @author BBonev
 */
@Singleton
public class SSOConfiguration {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "security.sso.signatureValidation.enabled", type = Boolean.class, system = true, sensitive = true, defaultValue = "false", label = "Indicates if the API should expect the security token to be signed."
			+ " If <b>true</b> the signature will be validated agains a key in a keystore defined in <code>security.sso.trustStore.path</code> property.")
	private ConfigurationProperty<Boolean> signatureValidationEnabled;

	@ConfigurationPropertyDefinition(system = true, sensitive = true, label = "Alias of the certificate used for SAML response signature validation")
	private static final String SIGNATURE_TRUSTSTORE_CERTIFICATE_ALIAS = "security.sso.trustStore.cert.alias";

	@ConfigurationPropertyDefinition(system = true, sensitive = true, label = "Password of the certificate used for SAML response signature validation")
	private static final String SIGNATURE_TRUSTSTORE_CERTIFICATE_CREDENTIAL = "security.sso.trustStore.cert.password";

	@ConfigurationPropertyDefinition(system = true, sensitive = true, label = "SSO assertion url. Default is constructed using system.default.host.* properties.")
	private static final String ASSERTION_URL = "security.sso.assertionUrl";

	@ConfigurationGroupDefinition(properties = { SIGNATURE_TRUSTSTORE_CERTIFICATE_ALIAS,
			SIGNATURE_TRUSTSTORE_CERTIFICATE_CREDENTIAL }, label = "Validated SSO credentials", system = true, type = String.class)
	private static final String SECURITY_SSO_CREDENTIAL = "security.sso.credential";

	@Inject
	@Configuration(SECURITY_SSO_CREDENTIAL)
	private ConfigurationProperty<Credential> credential;

	@ConfigurationPropertyDefinition(system = true, sensitive = true, label = "Identifier of the issuer (this application) that will be send to the IdP when requesting security tokens")
	private static final String SECURITY_SSO_ISSUER_ID = "security.sso.issuerId";

	@Inject
	@Configuration(SECURITY_SSO_ISSUER_ID)
	private ConfigurationProperty<String> issuerIdProperty;

	@Inject
	@Configuration(ASSERTION_URL)
	private ConfigurationProperty<String> assertionURL;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "security.sso.idpUrl.customAuth", system = true, sensitive = true, label = "Identity provider server URL used for SSO. <br><b>NOTE: </b> "
			+ "The property need to end with the IP address of the calling machine (JBOSS server host name)."
			+ " The same address that is configured in the SSO server. <b>Example value is: https://127.0.0.1:9448/commonauth</b>")
	private ConfigurationProperty<String> ssoAuthenticationUrl;

	@Inject
	private IDPConfiguration idpConfiguration;

	@Inject
	private SystemConfiguration systemConfiguration;

	@ConfigurationConverter(ASSERTION_URL)
	String buildAssertionUrl(ConverterContext context) {
		String rawValue = context.getRawValue();
		if (StringUtils.isNotBlank(rawValue)) {
			return rawValue;
		}

		String protocol = systemConfiguration.getDefaultProtocol().get();
		String host = systemConfiguration.getDefaultHost().get();
		Integer port = systemConfiguration.getDefaultPort().get();
		String contextPath = systemConfiguration.getDefaultContextPath().get().replace("/", "");

		return String.format("%s://%s:%d/%s/ServiceLogin", protocol, host, port, contextPath);
	}

	/**
	 * Initializes the Credential used for SAML signature validation.
	 *
	 * @param context
	 *            the context
	 * @param securityConfiguration
	 *            the security configuration
	 * @return the credential
	 */
	@ConfigurationConverter(SECURITY_SSO_CREDENTIAL)
	static Credential initSignatureValidationCredential(GroupConverterContext context,
			SecurityConfiguration securityConfiguration) {

		try {
			KeyStore keystore = securityConfiguration.getTrustStore().get();
			if (keystore == null) {
				return null;
			}

			context.getValue(SIGNATURE_TRUSTSTORE_CERTIFICATE_ALIAS).requireConfigured(
					"Certificate alias not configured!");

			Map<String, String> passwordMap = new HashMap<>();
			passwordMap.put(context.get(SIGNATURE_TRUSTSTORE_CERTIFICATE_ALIAS),
					context.get(SIGNATURE_TRUSTSTORE_CERTIFICATE_CREDENTIAL));
			KeyStoreCredentialResolver resolver = new KeyStoreCredentialResolver(keystore, passwordMap);

			Criteria criteria = new EntityIDCriteria(context.get(SIGNATURE_TRUSTSTORE_CERTIFICATE_ALIAS));
			CriteriaSet criteriaSet = new CriteriaSet(criteria);

			return resolver.resolveSingle(criteriaSet);
		} catch (Exception e) {
			throw new ConfigurationException("SignatureValidationCredential initilization failed!", e);
		}
	}

	/**
	 * Gets the issuer id.
	 *
	 * @return the issuer id
	 */
	public ConfigurationProperty<String> getIssuerId() {
		return issuerIdProperty;
	}

	/**
	 * Gets the assertion url.
	 *
	 * @return the assertion url
	 */
	public ConfigurationProperty<String> getAssertionURL() {
		return assertionURL;
	}

	/**
	 * Gets the credential.
	 *
	 * @return the credential
	 */
	public ConfigurationProperty<Credential> getCredential() {
		return credential;
	}

	/**
	 * Gets the idp url.
	 *
	 * @return the idp url
	 */
	public ConfigurationProperty<String> getIdpUrl() {
		return idpConfiguration.getIdpServerURL();
	}

	/**
	 * Gets the ido url for interface.
	 *
	 * @param address
	 *            the address
	 * @return the ido url for interface
	 */
	public String getIdpUrlForInterface(String address) {
		return getIdpUrl().get();
	}

	/**
	 * Gets the signature validation enabled.
	 *
	 * @return the signature validation enabled
	 */
	public ConfigurationProperty<Boolean> getSignatureValidationEnabled() {
		return signatureValidationEnabled;
	}

	/**
	 * Checks if is signature validation enabled.
	 *
	 * @return true, if is signature validation enabled
	 */
	public boolean isSignatureValidationEnabled() {
		return signatureValidationEnabled.get().booleanValue();
	}

	/**
	 * Gets the sso authentication url.
	 *
	 * @return the sso authentication url
	 */
	public ConfigurationProperty<String> getSsoAuthenticationUrl() {
		return ssoAuthenticationUrl;
	}
}
