package com.sirma.itt.patch;

import com.sirma.itt.seip.db.patch.DbSchemaPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Database patch that is executed right after migration patches. This patches include data migrations.
 *
 * @author Valeri Tishev
 */
@Extension(target = DbSchemaPatch.TARGET_NAME, order = 10000)
public class SeipMigrationPatches implements DbSchemaPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("/seip-changelog.xml");
	}

}
