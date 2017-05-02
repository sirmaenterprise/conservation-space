package com.sirma.itt.objects.patch;

import com.sirma.itt.seip.db.patch.DbDataPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Patches for permissions functionalities.
 *
 * @author Adrian Mitev
 */
@Extension(target = DbDataPatch.TARGET_NAME, order = 3000)
public class EntityManagementPatches implements DbDataPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("entity-management-changelog.xml");
	}

}