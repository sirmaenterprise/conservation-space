package com.sirma.itt.emf.audit.patch;

import com.sirma.itt.seip.db.patch.DbDataPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Data patches for the audit log.
 * 
 * @author nvelkov
 */
@Extension(target = DbDataPatch.TARGET_NAME, order = 2999)
public class AuditDataPatches implements DbDataPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("audit-data-changelog.xml");
	}

}