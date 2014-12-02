package com.sirma.itt.pm.util;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.sirma.itt.emf.hash.HashCalculatorImpl;
import com.sirma.itt.pm.domain.definitions.impl.ProjectDefinitionImpl;

/**
 * @author bbanchev
 */
public class PmHashCalculatorExtensionTest {

	/**
	 * Test method for
	 * {@link com.sirma.itt.pm.util.PmHashCalculatorExtension#computeHash(com.sirma.itt.emf.hash.HashCalculator, java.lang.Object)}
	 * .
	 */
	@Test(enabled = true)
	public void testComputeHashHashCalculatorObject() throws Exception {
		ProjectDefinitionImpl projectDefinitionImpl = new ProjectDefinitionImpl();
		PmHashCalculatorExtension.computeHash(projectDefinitionImpl, new HashCalculatorImpl());
		assertTrue(true, "OK");
	}

}
