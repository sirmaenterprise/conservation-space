package com.sirma.itt.seip.util;

import java.security.MessageDigest;

import org.apache.commons.codec.binary.Hex;

/**
 * Utility class for containing method for working with {@link java.security.MessageDigest} API.
 *
 * @author BBonev
 */
public class DigestUtils {

	/**
	 * Prevent instantiation.
	 */
	private DigestUtils() {
	}

	/**
	 * Calculate SHA-1 digest of the given string content.
	 *
	 * @param content
	 *            the content
	 * @return the calculated digest
	 */
	public static String calculateDigest(String content) {
		if (content == null) {
			return null;
		}
		// use this to not depend on the sha1 method of the DigestUtils that is added in the newer versions of the
		// library because it has conflicts with other libraries like json-ld-java
		MessageDigest digest = org.apache.commons.codec.digest.DigestUtils.getDigest("SHA-1");
		byte[] bs = digest.digest(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(content));
		return Hex.encodeHexString(bs);
	}

	/**
	 * Truncate with digest.
	 *
	 * @param data
	 *            the data
	 * @param limit
	 *            the limit
	 * @return the string
	 */
	public static String truncateWithDigest(String data, int limit) {
		if (data == null || data.length() <= limit) {
			return data;
		}
		String digest = DigestUtils.calculateDigest(data);
		if (digest.length() == limit) {
			return digest;
		} else if (digest.length() > limit) {
			return digest.substring(0, limit);
		}

		StringBuilder builder = new StringBuilder(limit);
		builder.append(digest);
		int startOffset = data.length() - (limit - digest.length());
		builder.append(data.substring(startOffset, data.length()));

		return builder.toString();
	}
}
