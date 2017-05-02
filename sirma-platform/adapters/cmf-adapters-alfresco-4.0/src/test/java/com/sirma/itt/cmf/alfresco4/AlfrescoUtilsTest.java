package com.sirma.itt.cmf.alfresco4;

import java.util.UUID;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.domain.instance.DMSInstance;

/**
 * The AlfrescoUtilsTest tests the {@link AlfrescoUtils} methods
 *
 * @author bbanchev
 */
public class AlfrescoUtilsTest {

	/**
	 * Validate existing dms instance.
	 */
	@Test
	public void validateExistingDMSInstance() {
		try {
			AlfrescoUtils.validateExistingDMSInstance(null);
			Assert.fail("Should fail!");
		} catch (DMSException e) {
			Assert.assertEquals(e.getMessage(), "Invalid 'instance' is provided: null");
		}
		DMSInstance dmsInstance = Mockito.mock(DMSInstance.class);
		try {
			AlfrescoUtils.validateExistingDMSInstance(dmsInstance);
			Assert.fail("Should fail!");
		} catch (DMSException e) {
			Assert.assertTrue(e.getMessage().contains("missing DMS ID"));
		}
		Mockito.when(dmsInstance.getDmsId()).thenReturn(UUID.randomUUID().toString());
		try {
			boolean validateExistingDMSInstance = AlfrescoUtils.validateExistingDMSInstance(dmsInstance);
			Assert.assertTrue(validateExistingDMSInstance);
		} catch (DMSException e) {
			Assert.fail("Should not fail!");
		}
	}
}
