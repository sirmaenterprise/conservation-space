package com.sirma.sep.email.patch;

import com.sirma.itt.seip.db.patch.DbSchemaPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Patch DB schema with changes needed for email integration.
 *
 * @author S.Djulgerova
 */
@Extension(target = DbSchemaPatch.TARGET_NAME, order = 67)
public class EmailIntegrationDBSchemaPatch implements DbSchemaPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("email-integration-changelog.xml");
	}
}
