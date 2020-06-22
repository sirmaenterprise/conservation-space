package com.sirma.itt.seip.resources.patches;

import com.sirma.itt.seip.db.patch.DbDataPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Data patches for user and group management functionalities.
 *
 * @author smustafov
 */
@Extension(target = DbDataPatch.TARGET_NAME, order = 2100)
public class UserManagementPatches implements DbDataPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("user-management-changelog.xml");
	}

}
