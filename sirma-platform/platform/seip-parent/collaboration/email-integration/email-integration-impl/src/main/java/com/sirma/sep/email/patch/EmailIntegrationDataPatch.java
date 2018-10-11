package com.sirma.sep.email.patch;

import com.sirma.itt.seip.db.patch.DbDataPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Patch DB data with changes needed for email integration.
 *
 * @author S.Djulgerova
 */
@Extension(target = DbDataPatch.TARGET_NAME, order = 50)
public class EmailIntegrationDataPatch implements DbDataPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("email-integration-data-changelog.xml");
	}
}