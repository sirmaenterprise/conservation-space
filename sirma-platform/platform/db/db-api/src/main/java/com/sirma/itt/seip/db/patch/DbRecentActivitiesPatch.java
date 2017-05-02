package com.sirma.itt.seip.db.patch;

import com.sirma.itt.seip.plugin.Plugin;

/**
 * Schema patch for the recent activities database.
 *
 * @author nvelkov
 */
public interface DbRecentActivitiesPatch extends DbPatch, Plugin {

	String TARGET_NAME = "DbRecentActivitiesPatch";

}
