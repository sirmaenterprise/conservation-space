package com.sirmaenterprise.sep.roles.provider;

import java.util.Arrays;

import com.tngtech.java.junit.dataprovider.DataProvider;

/**
 * Data provider for the external role definition provider tests
 *
 * @author BBonev
 */
public class ExternalRoleDataProvider {
	/**
	 * Gets the invalid file locations.
	 *
	 * @return the invalid file locations
	 */
	@DataProvider
	public static Object[][] invalidData() {
		return new Object[][] { { Arrays.asList("invalidData/missingDependecy1.xml") },
				{ Arrays.asList("invalidData/missingDependecy2.xml") },
				{ Arrays.asList("invalidData/missingDependecy3.xml") },
				{ Arrays.asList("invalidData/missingDependecy4.xml") },
				{ Arrays.asList("invalidData/missingDependecy5.xml") },
				{ Arrays.asList("invalidData/circularDependecy1.xml") },
				{ Arrays.asList("invalidData/circularDependecy2.xml") },
				{ Arrays.asList("invalidData/circularDependecy4.xml") },
				{ Arrays.asList("invalidData/circularDependecy5_part1.xml",
						"invalidData/circularDependecy5_part1.xml") } };
	}

	/**
	 * Gets the valid file locations.
	 *
	 * @return the valid file locations
	 */
	@DataProvider
	public static Object[][] validData() {
		return new Object[][] { { Arrays.asList("validData/dependecy1.xml") },
				{ Arrays.asList("validData/dependecy2.xml") },
				{ Arrays.asList("validData/dependecy3_part1.xml", "validData/dependecy3_part2.xml") },
				{ Arrays.asList("validData/dependecy3.xml") } };
	}
}
