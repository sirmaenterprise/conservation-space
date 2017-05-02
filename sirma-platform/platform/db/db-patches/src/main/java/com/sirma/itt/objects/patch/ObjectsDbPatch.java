package com.sirma.itt.objects.patch;

import com.sirma.itt.seip.db.patch.DbSchemaPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Base database patch for Objects module
 *
 * @author BBonev
 */
@Extension(target = DbSchemaPatch.TARGET_NAME, order = 40)
public class ObjectsDbPatch implements DbSchemaPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("objects-changelog.xml");
	}

}
