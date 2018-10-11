package com.sirma.itt.seip.instance.version.compare;

/**
 * Defines specified methods for comparing contents of version instances.
 *
 * @author A. Kunchev
 */
public interface VersionCompareService {

	/**
	 * Compares contents of version instances.
	 *
	 * @param compareContext
	 *            {@link VersionCompareContext} object the contains the information required for successful execution
	 *            of compare operation
	 * @return link to the generated file, which represents the result of the compared contents
	 */
	String compareVersionsContent(VersionCompareContext compareContext);

}
