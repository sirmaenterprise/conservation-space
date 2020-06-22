package com.sirma.sep.definition.patches;

import com.sirma.itt.seip.db.patch.DbDataPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Data patches for the template functionality.
 *
 * @author Adrian Mitev
 */
@Extension(target = DbDataPatch.TARGET_NAME, order = 5000)
public class DefinitionDataPatches implements DbDataPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("definition-data-changelog.xml");
	}

}