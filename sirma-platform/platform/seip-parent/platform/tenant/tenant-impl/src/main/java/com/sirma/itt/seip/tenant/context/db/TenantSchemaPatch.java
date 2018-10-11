/**
 *
 */
package com.sirma.itt.seip.tenant.context.db;

import com.sirma.itt.seip.db.patch.DbCoreSchemaPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Core db patch for initializing tenant tables
 *
 * @author BBonev
 */
@Extension(target = DbCoreSchemaPatch.TARGET_NAME, order = 7)
public class TenantSchemaPatch implements DbCoreSchemaPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("tenant-changelog.xml");
	}

}
