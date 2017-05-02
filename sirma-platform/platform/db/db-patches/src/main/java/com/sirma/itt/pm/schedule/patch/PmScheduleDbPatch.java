package com.sirma.itt.pm.schedule.patch;

import com.sirma.itt.seip.db.patch.DbSchemaPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Base database patch for PM schedule module
 *
 * @author BBonev
 */
@Extension(target = DbSchemaPatch.TARGET_NAME, order = 30)
public class PmScheduleDbPatch implements DbSchemaPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("pm-schedule-changelog.xml");
	}

}
