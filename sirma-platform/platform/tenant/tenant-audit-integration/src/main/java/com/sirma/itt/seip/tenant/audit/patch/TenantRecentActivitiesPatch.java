package com.sirma.itt.seip.tenant.audit.patch;

import com.sirma.itt.seip.db.patch.DbRecentActivitiesPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Patches all tenants with the needed recebt activities changes.
 *
 * @author nvelkov
 */
@Extension(target = DbRecentActivitiesPatch.TARGET_NAME, order = 68)
public class TenantRecentActivitiesPatch implements DbRecentActivitiesPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("tenant-audit-changelog.xml");
	}
}
