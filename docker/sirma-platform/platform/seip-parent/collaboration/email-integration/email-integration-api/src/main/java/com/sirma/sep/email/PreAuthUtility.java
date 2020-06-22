package com.sirma.sep.email;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.sep.email.exception.EmailIntegrationException;

/**
 * Contains common methods used for generation of preAuth token
 *
 * @author S.Djulgerova
 */
public final class PreAuthUtility {

	private PreAuthUtility() {
		// utility class
	}

	public static long preAuthRequestTimestamp;

	/**
	 * Get preAuth token for given account name
	 * 
	 * @param accountName
	 *            account name
	 * @return preAuth token
	 * @throws EmailIntegrationException
	 */
	public static String getPreAuthToken(String accountName) {
		String domainKey = generateDomainKey(accountName);
		HashMap<String, String> params = new HashMap<>();
		preAuthRequestTimestamp = System.currentTimeMillis();
		String ts = preAuthRequestTimestamp + "";

		params.put("account", accountName);
		params.put("by", "name");
		params.put("timestamp", ts);
		params.put("expires", "0");
		return computePreAuth(params, domainKey);
	}

	private static String computePreAuth(Map<String, String> params, String key) {
		TreeSet<String> names = new TreeSet<>(params.keySet());
		StringBuilder sb = new StringBuilder();
		for (Iterator<String> it = names.iterator(); it.hasNext();) {
			if (sb.length() > 0)
				sb.append('|');
			sb.append(params.get(it.next()));
		}
		return getHmac(sb.toString(), key.getBytes());
	}

	private static String generateDomainKey(String accountName) {
		String extracted = null;
		extracted = accountName.substring(accountName.indexOf('@') + 1);
		return generatePreauthHash(extracted);
	}

	public static String generatePreauthHash(String domainName) {
		if (domainName == null || domainName.length() == 0) {
			throw new IllegalArgumentException("There is no domain name specified");
		}

		byte[] digest;
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
			md.update(domainName.getBytes("UTF-8")); // Change this to "UTF-16" if needed
			digest = md.digest();
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			throw new EmfRuntimeException("Error during the hash generation " + e.getMessage(), e);
		}

		if (digest == null || digest.length == 0) {
			throw new EmfRuntimeException("Hash generation failed " + domainName);
		}

		return DatatypeConverter.printHexBinary(digest);
	}

	private static String getHmac(String data, byte[] key) {
		try {
			ByteKey bk = new ByteKey(key);
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(bk);
			return toHex(mac.doFinal(data.getBytes()));
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			throw new EmfRuntimeException("Error convert value " + e.getMessage(), e);
		}
	}

	private static class ByteKey implements SecretKey {

		private static final long serialVersionUID = 5118172588845183033L;
		private byte[] mKey;

		ByteKey(byte[] key) {
			mKey = key.clone();
		}

		public byte[] getEncoded() {
			return mKey;
		}

		public String getAlgorithm() {
			return "HmacSHA1";
		}

		public String getFormat() {
			return "RAW";
		}
	}

	private static String toHex(byte[] data) {
		StringBuilder sb = new StringBuilder(data.length * 2);
		for (int i = 0; i < data.length; i++) {
			sb.append(hex[(data[i] & 0xf0) >>> 4]);
			sb.append(hex[data[i] & 0x0f]);
		}
		return sb.toString();
	}

	private static final char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f' };

}