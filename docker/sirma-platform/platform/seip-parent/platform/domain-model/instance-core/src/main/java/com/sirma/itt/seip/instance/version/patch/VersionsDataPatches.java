package com.sirma.itt.seip.instance.version.patch;

import com.sirma.itt.seip.db.patch.DbDataPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Data patches for the versions.
 *
 * @author smustafov
 */
@Extension(target = DbDataPatch.TARGET_NAME, order = 335)
public class VersionsDataPatches implements DbDataPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("versions-data-changelog.xml");
	}

}
