/**
 *
 */
package com.sirma.itt.seip.security.util;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.exception.SecurityException;

/**
 * Contains methods for building secure objects and working with them.
 *
 * @author BBonev
 */
public class SecurityUtil {

	private static final String SUN_JCE_PROVIDER = "SunJCE";
	private static final String CIPHER_ALGORITHM_DES = "DES";
	private static final Pattern LINE_SEPARATOR = Pattern.compile("lineSeparator");
	/** The standard id/domain separator for full tenant names. */
	public static final char TENANT_ID_SEPARATOR = '@';

	private SecurityUtil() {
		// utility class
	}

	/**
	 * Clean token.
	 *
	 * @param token
	 *            the token
	 * @return the string
	 */
	public static String cleanToken(String token) {
		return LINE_SEPARATOR.matcher(token).replaceAll(System.lineSeparator());
	}

	/**
	 * http://stackoverflow.com/questions/339004/java-encrypt-decrypt-user-name- and-password-from-a-configuration-file.
	 * <br>
	 * Throws runtime exception on some error
	 *
	 * @param plainText
	 *            is the plain text
	 * @param key
	 *            the key
	 * @return the encrypted plain text
	 */
	public static byte[] encrypt(byte[] plainText, SecretKey key) {
		// only the first 8 Bytes of the constructor argument are used
		// as material for generating the keySpec
		try {
			// cipher is not thread safe
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_DES, SUN_JCE_PROVIDER);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			// ENCODE plainText byte[]
			return cipher.doFinal(plainText);
		} catch (Exception e) {
			throw new IllegalStateException(
					new String(plainText, StandardCharsets.UTF_8) + " is not encrypted, due to exception", e);
		}
	}

	/**
	 * http://stackoverflow.com/questions/339004/java-encrypt-decrypt-user-name- and-password-from-a-configuration-file.
	 * <br>
	 * Descrypts the text provided<br>
	 * The used security provider is SunJCE's provider.
	 *
	 * @param encryptedBytes
	 *            is the encrypted text
	 * @param key
	 *            the key
	 * @return the plain text or throws runtime exception on some error
	 */
	public static byte[] decrypt(byte[] encryptedBytes, SecretKey key) {
		try {
			// cipher is not thread safe
			// The SunJCE provider is explicitly set for the cipher and the secret keys, because keys from one provider
			// are not valid for the others (specifically bouncycastle's keys are too long for sunjce's providers), so
			// we need to make sure SunJCE is chosen every time.
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_DES, SUN_JCE_PROVIDER);
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(encryptedBytes);
		} catch (Exception e) {
			throw new IllegalStateException(new String(encryptedBytes, StandardCharsets.UTF_8)
					+ " is not decrypted, due to exception: " + e.getMessage(), e);
		}
	}

	/**
	 * Creates the secret key.
	 * The used security provider is SunJCE's provider.
	 *
	 * @param key
	 *            the key
	 * @return the secret key
	 */
	public static SecretKey createSecretKey(String key) {
		try {
			SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), CIPHER_ALGORITHM_DES);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(CIPHER_ALGORITHM_DES, SUN_JCE_PROVIDER);
			return keyFactory.generateSecret(keySpec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
			throw new SecurityException(e);
		}
	}

	/**
	 * Builds the key store.
	 *
	 * @param path
	 *            the path
	 * @param password
	 *            the password
	 * @return the key store
	 */
	public static KeyStore buildKeyStore(String path, String password) {
		try {
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			loadKeyStore(path, password, keystore);
			return keystore;
		} catch (KeyStoreException kse) {
			throw new ConfigurationException(kse);
		}
	}

	private static void loadKeyStore(String path, String password, KeyStore keystore) {
		try (FileInputStream inputStream = new FileInputStream(path)) {
			keystore.load(inputStream, password.toCharArray());
		} catch (Exception e) {
			throw new SecurityException(e);
		}
	}

	/**
	 * Builds user id with tenant domain. Ensures that system tenant is not included in the user name.
	 *
	 * @param username
	 *            the username
	 * @param tenantId
	 *            the tenant id
	 * @return the string
	 */
	public static String buildTenantUserId(String username, String tenantId) {
		if (StringUtils.isBlank(tenantId)) {
			return username;
		}
		if (SecurityContext.isSystemTenant(tenantId)) {
			return getUserWithoutTenant(username);
		}
		if (SecurityContext.isDefaultTenant(tenantId)) {
			return getUserWithoutTenant(username);
		}
		// if the name already contains a tenant id just return it
		if (hasTenant(username)) {
			return validateUserAndTenant(username, tenantId);
		}
		return username + TENANT_ID_SEPARATOR + tenantId;
	}

	private static String validateUserAndTenant(String username, String tenantId) {
		if (!username.endsWith(tenantId)) {
			throw new SecurityException("User [" + username + "] and tenant id [" + tenantId + "] does not match");
		}
		return username;
	}

	/**
	 * Gets the user and tenant from full userId.
	 *
	 * @param username
	 *            the username in format user@domain
	 * @return the user and tenant as first/second
	 */
	public static StringPair getUserAndTenant(String username) {
		if (hasTenant(username)) {
			String[] userAndTenant = username.split(Character.toString(TENANT_ID_SEPARATOR));
			return new StringPair(userAndTenant[0], userAndTenant[1]);
		}
		if (SecurityContext.getSystemAdminName().equalsIgnoreCase(username)) {
			return new StringPair(username, SecurityContext.SYSTEM_TENANT);
		}
		// this is for system admin where he is in the default system tenant
		return new StringPair(username, SecurityContext.getDefaultTenantId());
	}

	/**
	 * Review - this method is almost duplicate of getUserAndTenant<br>
	 * Gets the user without tenant.
	 *
	 * @param username
	 *            the username
	 * @return the user without tenant
	 */
	public static String getUserWithoutTenant(String username) {
		if (hasTenant(username)) {
			String[] userAndTenant = username.split(Character.toString(TENANT_ID_SEPARATOR));
			return userAndTenant[0];
		}
		return username;
	}

	private static boolean hasTenant(String username) {
		return (username != null) && (username.indexOf(TENANT_ID_SEPARATOR) > 0);
	}

}
