package com.sirma.itt.seip.db.patch;

import com.sirma.itt.seip.plugin.Plugin;

/**
 * Schema patch for the base data database.
 *
 * @author Adrian Mitev
 */
public interface DbSchemaPatch extends DbPatch, Plugin {

	String TARGET_NAME = "DbSchemaPatch";

}
