package com.sirma.itt.emf.authentication.sso.saml.authenticator;

import java.nio.charset.StandardCharsets;

/**
 * The SAMLTokenInfo is wrapper for token information.
 *
 * @author bbanchev
 */
class SAMLTokenInfo {

	byte[] encrypted;
	byte[] decrypted;
	private String token;

	/**
	 * Instantiates a new SAML token info.
	 *
	 * @param encrypted
	 *            the encrypted data
	 * @param decrypted
	 *            the decrypted data
	 */
	public SAMLTokenInfo(byte[] encrypted, byte[] decrypted) {
		this.encrypted = encrypted;
		this.decrypted = decrypted;
		if (encrypted != null && token == null) {
			token = new String(encrypted, StandardCharsets.UTF_8);
		}
	}

	/**
	 * Gets the encrypted.
	 *
	 * @return the encrypted
	 */
	public byte[] getEncrypted() {
		return encrypted;
	}

	/**
	 * Gets the decrypted.
	 *
	 * @return the decrypted
	 */
	public byte[] getDecrypted() {
		return decrypted;
	}

	/**
	 * Get the encrypted token as string.
	 *
	 * @return the encrypted token or null if not set
	 */
	public String getToken() {
		return token;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SAMLTokenInfo [encrypted=");
		builder.append(new String(encrypted, StandardCharsets.UTF_8));
		builder.append(", decrypted=");
		builder.append(new String(decrypted, StandardCharsets.UTF_8));
		builder.append("]");
		return builder.toString();
	}

}
