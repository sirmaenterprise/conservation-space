package com.sirma.itt.seip.instance.version.compare;

import java.io.File;

/**
 * Defines methods for comparing contents of version instances.
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
	 * @return {@link File} representing the diff between the versions
	 */
	File compareVersionsContent(VersionCompareContext compareContext);

}
