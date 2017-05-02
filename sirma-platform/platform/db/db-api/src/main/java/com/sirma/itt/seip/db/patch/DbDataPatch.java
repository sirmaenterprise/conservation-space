package com.sirma.itt.seip.db.patch;

import com.sirma.itt.seip.plugin.Plugin;

/**
 * All db data patch, executed after successful schema patch
 *
 * @author bbanchev
 */
public interface DbDataPatch extends DbPatch, Plugin {

	String TARGET_NAME = "DbDataPatch";

}