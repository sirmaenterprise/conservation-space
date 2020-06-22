package com.sirma.itt.seip.shared;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Base64;

import org.junit.Test;

import com.sirma.itt.seip.shared.exception.ShareCodeValidationException;
import com.sirma.itt.seip.util.DigestUtils;

/**
 * Test the share code utils.
 * 
 * @author nvelkov
 */
public class ShareCodeUtilsTest {

	@Test
	public void should_constructAndDeconstructShareCodes() throws ShareCodeValidationException {
		String user = "emf:admin-tenant.com";
		String resourceId = "ac9ab7d0-40d4-4de1-95cb-924a170e2e81";
		String secretKey = "key";
		String shareCode = ShareCodeUtils.construct(resourceId, user, secretKey);
		// Assert that the user is nowhere to be found in the constructed share code.
		assertFalse(shareCode.contains(user));

		ShareCode deconstructed = ShareCodeUtils.deconstruct(resourceId, shareCode, secretKey);
		String expectedSignature = DigestUtils.truncateWithDigest(Base64.getUrlEncoder().encodeToString(user.getBytes())
				+ deconstructed.getGenerationTime().getTime() + resourceId + secretKey, 8);
		assertEquals(user, deconstructed.getUser());
		assertEquals(expectedSignature, deconstructed.getSignature());
	}

	@Test
	public void should_validateShareCodes() throws ShareCodeValidationException {
		String user = "admin@tenant.com";
		String resourceId = "ac9ab7d0-40d4-4de1-95cb-924a170e2e81";
		String secretKey = "key";
		String shareCode = ShareCodeUtils.construct(resourceId, user, secretKey);

		assertTrue(ShareCodeUtils.verify(resourceId, shareCode, secretKey));
	}

	@Test(expected = ShareCodeValidationException.class)
	public void should_throwException_onTamperedShareCode() throws ShareCodeValidationException {
		String user = "admin@tenant.com";
		String secretKey = "key";
		String shareCode = ShareCodeUtils.construct("another-resource-id", user, secretKey);

		assertFalse(ShareCodeUtils.verify("some-other-resource", shareCode, secretKey));
	}

	@Test(expected = ShareCodeValidationException.class)
	public void should_throwException_onValidation_withEmptyParameters() throws ShareCodeValidationException {
		ShareCodeUtils.verify(null, null, null);
	}

	@Test(expected = ShareCodeValidationException.class)
	public void should_throwException_onConstruction_withEmptyParameters() throws ShareCodeValidationException {
		ShareCodeUtils.construct(null, null, null);
	}

	@Test(expected = ShareCodeValidationException.class)
	public void should_throwException_onDeconstruction_withEmptyParameters() throws ShareCodeValidationException {
		ShareCodeUtils.deconstruct(null, null, null);
	}

}
