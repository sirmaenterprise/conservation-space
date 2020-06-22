package com.sirma.itt.seip.instance.properties.patch;

import com.sirma.itt.seip.db.patch.DbDataPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Data patches for instance properties.
 *
 * @author smustafov
 */
@Extension(target = DbDataPatch.TARGET_NAME, order = 400)
public class PropertiesDataPatches implements DbDataPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("properties-data-changelog.xml");
	}

}
