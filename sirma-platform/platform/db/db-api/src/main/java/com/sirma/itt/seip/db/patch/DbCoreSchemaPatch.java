package com.sirma.itt.seip.db.patch;

import com.sirma.itt.seip.plugin.Plugin;

/**
 * Patches related only to core db
 *
 * @author bbanchev
 */
public interface DbCoreSchemaPatch extends DbPatch, Plugin {

	String TARGET_NAME = "DbCoreSchemaPatch";

}
