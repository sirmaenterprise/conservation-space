package com.sirma.itt.cmf.patch;

import com.sirma.itt.seip.db.patch.DbSchemaPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * DB patch for the CMF database.
 *
 * @author Adrian Mitev
 */
@Extension(target = DbSchemaPatch.TARGET_NAME, order = 10)
public class CMFDBSchemaPatch implements DbSchemaPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("cmf-changelog.xml");
	}

}
