package com.sirma.itt.pm.web.label;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * The Class PMWebLabelBundleProviderTest.
 * 
 * @author svelikov
 */
public class PMWebLabelBundleProviderTest {

	/** The provider. */
	private PMWebLabelBundleProvider provider;

	/**
	 * Instantiates a new pM web label bundle provider test.
	 */
	public PMWebLabelBundleProviderTest() {
		provider = new PMWebLabelBundleProvider();
	}

	/**
	 * Gets the base name test.
	 */
	@Test
	public void getBaseNameTest() {
		String baseName = provider.getBaseName();
		Assert.assertEquals(baseName, "com.sirma.itt.pm.i18n.i18n");
	}
}
