package com.sirma.itt.emf.audit.patch;

import com.sirma.itt.seip.db.patch.DbAuditPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Patches the DB for BAM. Code is taken from EMF.
 *
 * @author Mihail Radkov
 */
@Extension(target = DbAuditPatch.TARGET_NAME, order = 67)
public class AuditDbSchemaPatch implements DbAuditPatch {

	@Override
	public String getPath() {
		return "com/sirma/itt/emf/bam/patch/bam-changelog.xml";
	}
}
