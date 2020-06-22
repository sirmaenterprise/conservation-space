package com.sirma.sep.email.patch;

import com.sirma.itt.seip.db.patch.DbCoreSchemaPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Patch DB core schema with changes needed for email integration.
 *
 * @author S.Djulgerova
 */
@Extension(target = DbCoreSchemaPatch.TARGET_NAME, order = 8)
public class EmailIntegrationDBCoreSchemaPatch implements DbCoreSchemaPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("email-integration-core-changelog.xml");
	}
}
