package com.sirma.itt.patch;

import com.sirma.itt.seip.db.patch.DbSchemaPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Database patch that is executed at the end of the patches. This patches include data migrations.
 *
 * @author BBonev
 */
@Extension(target = DbSchemaPatch.TARGET_NAME, order = 9999)
public class MigrationPatches implements DbSchemaPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("changelog.xml");
	}
}
