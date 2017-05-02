package com.sirma.itt.seip.permissions.db.patches;

import com.sirma.itt.seip.db.patch.DbDataPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Patches for permissions functionalities.
 *
 * @author Adrian Mitev
 */
@Extension(target = DbDataPatch.TARGET_NAME, order = 2000)
public class PermissionsDataPatches implements DbDataPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("permissions-changelog.xml");
	}

}