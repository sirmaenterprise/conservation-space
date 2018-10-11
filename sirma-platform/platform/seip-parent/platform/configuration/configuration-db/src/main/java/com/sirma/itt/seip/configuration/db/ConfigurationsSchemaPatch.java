/**
 *
 */
package com.sirma.itt.seip.configuration.db;

import com.sirma.itt.seip.db.patch.DbCoreSchemaPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Core db patch for initializing configurations tables
 *
 * @author BBonev
 */
@Extension(target = DbCoreSchemaPatch.TARGET_NAME, order = 5)
public class ConfigurationsSchemaPatch implements DbCoreSchemaPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("config-changelog.xml");
	}

}
