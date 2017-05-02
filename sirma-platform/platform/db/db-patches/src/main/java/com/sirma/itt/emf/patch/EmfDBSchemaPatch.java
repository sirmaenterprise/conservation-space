package com.sirma.itt.emf.patch;

import com.sirma.itt.seip.db.patch.DbSchemaPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * DB patch for the EMF database.
 *
 * @author BBonev
 */
@Extension(target = DbSchemaPatch.TARGET_NAME, order = 0)
public class EmfDBSchemaPatch implements DbSchemaPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("emf-changelog.xml");
	}
}
