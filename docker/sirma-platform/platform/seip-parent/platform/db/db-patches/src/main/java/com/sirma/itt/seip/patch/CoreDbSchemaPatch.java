package com.sirma.itt.seip.patch;

import com.sirma.itt.seip.db.patch.DbCoreSchemaPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Patch to add common tables to core database
 *
 * @author BBonev
 */
@Extension(target = DbCoreSchemaPatch.TARGET_NAME, order = 2)
public class CoreDbSchemaPatch implements DbCoreSchemaPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("core-changelog.xml");
	}

}
