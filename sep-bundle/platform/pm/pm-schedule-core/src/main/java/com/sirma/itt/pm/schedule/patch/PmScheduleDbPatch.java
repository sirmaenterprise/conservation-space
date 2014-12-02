package com.sirma.itt.pm.schedule.patch;

import com.sirma.itt.emf.patch.DBSchemaPatch;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Base database patch for PM schedule module
 * 
 * @author BBonev
 */
@Extension(target = DBSchemaPatch.TARGET_NAME, order = 30)
public class PmScheduleDbPatch implements DBSchemaPatch {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return getClass().getPackage().getName().replace(".", "/") + "/pm-schedule-changelog.xml";
	}

}
