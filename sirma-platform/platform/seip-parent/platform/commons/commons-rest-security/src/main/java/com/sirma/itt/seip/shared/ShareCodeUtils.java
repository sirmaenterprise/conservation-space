package com.sirma.itt.seip.shared;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import com.google.common.primitives.Chars;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.shared.exception.ShareCodeValidationException;
import com.sirma.itt.seip.util.DigestUtils;

/**
 * Used to construct/deconstruct and verify share codes.
 * 
 * @author nvelkov
 */
public class ShareCodeUtils {

	private static final int SIGNATURE_LENGTH = 8;
	private static final int GENERATION_DATE_LENGTH = 13;

	/**
	 * Private constructor to disallow instantiation.
	 */
	private ShareCodeUtils() {
		// Disallow instantiation.
	}

	/**
	 * Construct a share code from the given resourceId and user. The resource id wont be in the
	 * shared code for simplicity's sake (We don't want the share code to be too long), but the user
	 * will be. It contains the following data:
	 * <ul>
	 * <li>The user - {username}@{tenantId}</li>
	 * <li>The date when the sharecode was generated</li>
	 * <li>The signature</li>
	 * </ul>
	 * The signature is generated from the user + the generation date + the resourceId + the secret
	 * key. The signature should be used to verify the validity of the share code. The secret key
	 * provides an extra layer of security to the share codes, because even if someone manages to
	 * reverse engineer the logic used to create the share code and tries to generate share codes
	 * for other resources, he wont be able to without knowing the secret key.
	 * 
	 * @param resourceId
	 *            id of the resource that will be accessed
	 * @param user
	 *            the user with which the resource should be accessed
	 * @param secretKey
	 *            the secret key to be applied when generating the signature
	 * @return the constructed share code
	 * @throws ShareCodeValidationException
	 *             if some of the parameters are missing
	 */
	public static String construct(String resourceId, String user, String secretKey)
			throws ShareCodeValidationException {
		checkForEmptyParameters(resourceId, user, secretKey);
		// Construct the share details from the encoded user + the current time in millis.
		String shareDetails = encodeUser(user) + new Date().getTime();

		// Generate a signature and append it to the share details.
		String signature = DigestUtils.truncateWithDigest(shareDetails + resourceId + secretKey, SIGNATURE_LENGTH);
		shareDetails += signature;

		// Use a seeded shuffle on the share details so they can't be read easily.
		return shuffleString(shareDetails, resourceId + secretKey);
	}

	/**
	 * Deconstruct the share code from the provided resourceId and already constructed share code.
	 * The resourceId will be used as the seed to deshuffle the share code.
	 * 
	 * @param resourceId
	 *            the resource id
	 * @param shareCode
	 *            the already-constructed share code
	 * @param secretKey
	 *            the secret key
	 * @return the deconstructed share code
	 * @throws ShareCodeValidationException
	 *             if some of the parameters are missing
	 */
	public static ShareCode deconstruct(String resourceId, String shareCode, String secretKey)
			throws ShareCodeValidationException {
		checkForEmptyParameters(resourceId, shareCode, secretKey);

		// Deshuffle the shuffled shareCode using the resourceId as the seed.
		String deshuffled = deshuffleString(shareCode, resourceId + secretKey);

		// The signature is the last 8 chars of the string.
		String signature = deshuffled.substring(deshuffled.length() - SIGNATURE_LENGTH);
		// The generation time is the last 13 chars before the signature.
		String dateString = deshuffled.substring(deshuffled.length() - (GENERATION_DATE_LENGTH + SIGNATURE_LENGTH),
				deshuffled.length() - SIGNATURE_LENGTH);
		if (!isComposedOnlyOfDigits(dateString)) {
			throw new ShareCodeValidationException(
					"Invalid share code. The date part of the share code couldn't be parsed correctly.");
		}
		Date generationTime = new Date(Long.parseLong(dateString));
		// The user is whatever remains from the begining to the generation time.
		String user = decodeUser(
				deshuffled.substring(0, deshuffled.length() - (GENERATION_DATE_LENGTH + SIGNATURE_LENGTH)));

		return new ShareCode(user, generationTime, signature);
	}

	/**
	 * Verify the resource id with the share code. If the resource id or the share code has been
	 * tampered with, the validation will fail.
	 * 
	 * @param resourceId
	 *            the resource id
	 * @param shareCode
	 *            the share code
	 * @param secretKey
	 *            the secret key
	 * @return true if the resource id or the share code match, false otherwise
	 * @throws ShareCodeValidationException
	 *             if some of the parameters are missing
	 */
	public static boolean verify(String resourceId, String shareCode, String secretKey)
			throws ShareCodeValidationException {
		checkForEmptyParameters(resourceId, shareCode, secretKey);
		ShareCode deconstructed = deconstruct(resourceId, shareCode, secretKey);
		// Generate the signature from the provided data so we can verify the validity of the share
		// code.
		String signature = DigestUtils.truncateWithDigest(encodeUser(deconstructed.getUser())
				+ deconstructed.getGenerationTime().getTime() + resourceId + secretKey, SIGNATURE_LENGTH);
		return signature.equals(deconstructed.getSignature());
	}

	private static String shuffleString(String toShuffle, String seed) {
		List<Character> shareDetailsList = new ArrayList<>(Chars.asList(toShuffle.toCharArray()));
		CollectionUtils.shuffle(shareDetailsList, new Random(stringToSeed(seed)));
		return StringUtils.join(shareDetailsList, null);
	}

	private static String deshuffleString(String toDeshuffle, String seed) {
		List<Character> shareCodeCharacters = Chars.asList(toDeshuffle.toCharArray());
		CollectionUtils.deshuffle(shareCodeCharacters, new Random(stringToSeed(seed)));
		return StringUtils.join(shareCodeCharacters, null);
	}

	/**
	 * Encode the user, since it can contain symbols such as '-' and '@' that are not permitted in a
	 * shared link.
	 * 
	 * @param user
	 *            the user to encode
	 * @return the encoded user
	 */
	private static String encodeUser(String user) {
		return Base64.getUrlEncoder().encodeToString(user.getBytes(StandardCharsets.UTF_8));
	}

	private static String decodeUser(String user) {
		return new String(Base64.getUrlDecoder().decode(user.getBytes(StandardCharsets.UTF_8)));

	}

	/**
	 * Convert the string to a long seed, since a {@link Random} can't be constructed with a string
	 * seed.
	 * 
	 * @param string
	 *            the string
	 * @return the long seed
	 */
	private static long stringToSeed(String string) {
		long hash = 0;
		for (char c : string.toCharArray()) {
			hash = 31L * hash + c;
		}
		return hash;
	}

	private static void checkForEmptyParameters(String... parameters) throws ShareCodeValidationException {
		for (String parameter : parameters) {
			if (StringUtils.isEmpty(parameter)) {
				throw new ShareCodeValidationException(
						"A valid share code can't be constructed with missing parameters");
			}
		}
	}

	private static boolean isComposedOnlyOfDigits(String string) {
		for (char character : string.toCharArray()) {
			if (!Character.isDigit(character)) {
				return false;
			}
		}
		return true;
	}
}
