package com.sirma.itt.seip.instance.content;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Boyan Tonchev.
 */
public class MimetypeConstantsTest {

	@Test
	public void should_ReturnTrue_When_MimeTypeIsSupportedWithoutCaseMatch() {
		Assert.assertTrue(MimetypeConstants.isMimeTypeSupported(MimetypeConstants.DOTM.toUpperCase()));
	}

	@Test
	public void should_ReturnTrue_When_MimeTypeIsSupportedWithCaseMatch() {
		Assert.assertTrue(MimetypeConstants.isMimeTypeSupported(MimetypeConstants.DOTM));
	}

	@Test
	public void should_ReturnFalse_When_MimeTypeIsNotSupportedWithCaseMatch() {
		Assert.assertFalse(MimetypeConstants.isMimeTypeSupported("application/pdf"));
	}

	@Test
	public void should_ReturnFalse_When_MimeTypeIsNotSupportedWithoutCaseMatch() {
		Assert.assertFalse(MimetypeConstants.isMimeTypeSupported("APPLICATION/PDF"));
	}
}