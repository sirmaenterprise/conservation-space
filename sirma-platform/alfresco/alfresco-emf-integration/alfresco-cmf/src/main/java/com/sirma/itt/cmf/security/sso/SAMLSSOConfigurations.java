package com.sirma.itt.cmf.security.sso;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.springframework.core.io.ClassPathResource;

import com.sirma.itt.cmf.integration.exception.SEIPRuntimeException;

/**
 * The Class SAMLSSOConfigurations encapsulate the sso needed configurations.
 * 
 * @author bbanchev
 */
public class SAMLSSOConfigurations {
	/** UTF encoding. */
	public static final String UTF_8 = "UTF-8";

	private static final String DEFAULT_HOST = "127.0.0.1";

	/** the read global properties from file. */
	private static final Properties GLOBAL_PROPERTIES = readGlobalProperties();
	/** Wildcard for allowed audience. */
	private static final String AUDIENCE_ALLOW_ALL = "*";
	/** the retrieved cache. */
	private static final byte[] CONFIG_VALUE_ENCRYPT_KEY = readEncryptKey();
	/** The allowed audience list. */
	private static final List<String> CONFIG_VALUE_ALLOWED_AUDIENCE = readAllowedAudienceList();
	/** Do we use time constraint for requests?. */
	private static final boolean CONFIG_VALUE_USE_TIMECONSTRAINTS = readIsUsingTimeConstraints();
	/** Is request expected to be encrypted. */
	private static final boolean CONFIG_VALUE_REQUEST_ENCRYPTED = readIsRequestEncrypted();

	private static final boolean CONFIG_VALUE_SSO_ENABLED = readIsSSOEnabled();
	/** The attrib index. */
	private static final Integer CONFIG_VALUE_SSO_ATTRINDEX = readAttributeIndex();

	private static SecretKey cipherKey;

	private SAMLSSOConfigurations() {
	}

	/**
	 * Checks if is sSO enabled.
	 *
	 * @return true, if is sSO enabled
	 */
	private static synchronized boolean readIsSSOEnabled() {
		Object object = GLOBAL_PROPERTIES.get("security.sso.enabled");
		return object == null ? true : Boolean.valueOf(object.toString()).booleanValue();
	}

	/**
	 * Retrieve <strong>cmf.encrypt.key</strong> from properties file.
	 *
	 *
	 * @return the chiper key or the default value of none is found
	 */
	private static synchronized byte[] readEncryptKey() {
		// cache for optimization
		Object object = GLOBAL_PROPERTIES.get("cmf.encrypt.key");
		String encryptKey = object == null || object.toString().trim().isEmpty() ? "somePassword" : object.toString();
		try {
			return encryptKey.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new SEIPRuntimeException("Failed to extract request encrypt key!", e);
		}
	}

	/**
	 * Gets the allowed audience list ip addresses that are allowed to request
	 * saml2 tokens. use <code>security.sso.list.allowed.audience</code> to set
	 * it with , separation
	 *
	 * @return the allowed audience list or default <code>
	 *         {@link #DEFAULT_HOST}</code>
	 */
	private static synchronized List<String> readAllowedAudienceList() {
		// cache for optimization
		Object object = GLOBAL_PROPERTIES.get("security.sso.list.allowed.audience");
		return (object == null || object.toString().trim().isEmpty()) ? Collections.singletonList(DEFAULT_HOST)
				: Arrays.asList(object.toString().split(","));
	}

	/**
	 * Gets the allowed audience list ip addresses that are allowed to request
	 * saml2 tokens. use <code>security.sso.list.allowed.audience</code> to set
	 * it with , separation
	 *
	 * @return the allowed audience list or default <code>
	 *         {@link #DEFAULT_HOST}</code>
	 */
	private static synchronized boolean readIsUsingTimeConstraints() {
		// cache for optimization
		Object object = GLOBAL_PROPERTIES.get("security.sso.useTimeConstraints");
		return (object == null || object.toString().trim().isEmpty()) ? Boolean.FALSE
				: Boolean.valueOf(object.toString()).booleanValue();
	}

	/**
	 * Checks if is request encrypted.
	 *
	 * @return true, if is request encrypted
	 */
	private static synchronized boolean readIsRequestEncrypted() {
		// cache for optimization
		Object object = GLOBAL_PROPERTIES.get("security.sso.request.encrypted");
		return (object == null || object.toString().trim().isEmpty()) ? Boolean.FALSE
				: Boolean.valueOf(object.toString()).booleanValue();
	}

	private static synchronized Integer readAttributeIndex() {
		// cache for optimization
		Object object = GLOBAL_PROPERTIES.get("security.sso.request.attrIndex");
		if (object == null || object.toString().trim().isEmpty()) {
			return Integer.MAX_VALUE - 1;
		}
		return Integer.valueOf(object.toString().trim());
	}

	/**
	 * Read and cache global properties as map.
	 * 
	 * @return the read properties
	 */
	private static synchronized Properties readGlobalProperties() {
		ClassPathResource globalProps = new ClassPathResource("alfresco-global.properties");
		Properties globalProperties = new Properties();
		try {
			globalProperties.load(globalProps.getInputStream());
		} catch (Exception e) {
			throw new SEIPRuntimeException("Failed to read properties from configuration!", e);
		}
		return globalProperties;
	}

	/**
	 * Checks if is using time constraints.
	 *
	 * @return true, if is using time constraints
	 */
	public static boolean isUsingTimeConstraints() {
		return CONFIG_VALUE_USE_TIMECONSTRAINTS;
	}

	/**
	 * Checks if allowed audience list contains userAddr or wildcard "*" symbol.
	 * 
	 * @param userAddr
	 *            the user address
	 * @return true if allowed audience list contains userAddr or wildcard "*"
	 *         symbol.
	 */
	public static boolean isAllowedAudience(String userAddr) {
		if (CONFIG_VALUE_ALLOWED_AUDIENCE.contains(userAddr)
				|| CONFIG_VALUE_ALLOWED_AUDIENCE.contains(AUDIENCE_ALLOW_ALL)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if is SSO enabled.
	 *
	 * @return true, if is SSO enabled
	 */
	public static boolean isSSOEnabled() {
		return CONFIG_VALUE_SSO_ENABLED;
	}

	/**
	 * Gets the attribute index.
	 *
	 * @return the attribute index
	 */
	public static Integer getAttributeIndex() {
		return CONFIG_VALUE_SSO_ATTRINDEX;
	}

	/**
	 * Checks if is encrypted request.
	 *
	 * @return true, if is encrypted request
	 */
	public static boolean isEncryptedRequest() {
		return CONFIG_VALUE_REQUEST_ENCRYPTED;
	}

	/**
	 * Retrieve <strong>security.sso.idpUrl</strong> from properties file.
	 *
	 * @param localAddress
	 *            the local address
	 * @return the url of idp or <code>https://127.0.0.1:9448/samlsso</code> as
	 *         fallback
	 */
	public static synchronized String getIdpURLInternal(String localAddress) {
		Object object = GLOBAL_PROPERTIES.get("security.sso.idpUrl." + localAddress);
		return object == null || object.toString().trim().isEmpty() ? "https://" + DEFAULT_HOST + ":9448/samlsso"
				: object.toString();
	}

	/**
	 * Gets the cipher key.
	 *
	 * @return the cipher key in DES for the configured private key.
	 * 
	 * @throws GeneralSecurityException
	 *             the on any problem related to key generation
	 */
	public static final synchronized SecretKey getDESEncryptKey() throws GeneralSecurityException {
		if (cipherKey == null) {
			DESKeySpec keySpec = new DESKeySpec(CONFIG_VALUE_ENCRYPT_KEY);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			cipherKey = keyFactory.generateSecret(keySpec);
		}
		return cipherKey;
	}

}
