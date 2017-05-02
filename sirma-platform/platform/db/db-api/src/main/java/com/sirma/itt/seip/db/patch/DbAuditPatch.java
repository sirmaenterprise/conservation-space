package com.sirma.itt.seip.db.patch;

import com.sirma.itt.seip.plugin.Plugin;

/**
 * Schema patch for the audit log database.
 *
 * @author nvelkov
 */
public interface DbAuditPatch extends DbPatch, Plugin {

	String TARGET_NAME = "DbAuditPatch";

}
