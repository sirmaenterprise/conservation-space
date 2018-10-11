package com.sirma.sep.instance.content.patch;

import com.sirma.itt.seip.db.patch.DbDataPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Provides path to the change log file for instance content patches.
 *
 * @author A. Kunchev
 */
@Extension(target = DbDataPatch.TARGET_NAME, order = 666)
public class InstanceContentDataPatches implements DbDataPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("instance-content-changelog.xml");
	}
}
