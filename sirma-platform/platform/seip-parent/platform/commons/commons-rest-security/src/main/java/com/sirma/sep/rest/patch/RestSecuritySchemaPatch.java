package com.sirma.sep.rest.patch;

import com.sirma.itt.seip.db.patch.DbCoreSchemaPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Db patch for initializing rest security tables.
 *
 * @author smustafov
 */
@Extension(target = DbCoreSchemaPatch.TARGET_NAME, order = 6)
public class RestSecuritySchemaPatch implements DbCoreSchemaPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("rest-security-changelog.xml");
	}

}
