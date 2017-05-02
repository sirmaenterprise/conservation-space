package com.sirmaenterprise.sep.roles.persistence;

import com.sirma.itt.seip.db.patch.DbSchemaPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Extension to add the schema patch file for the role management persistence model
 *
 * @author BBonev
 */
@Extension(target = DbSchemaPatch.TARGET_NAME, order = 234)
public class RolesDbSchemaPatch implements DbSchemaPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("roles-changelog.xml");
	}
}
