package com.sirma.sep.template.patches;

import com.sirma.itt.seip.db.patch.DbDataPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Data patches for the template functionality.
 *
 * @author Adrian Mitev
 */
@Extension(target = DbDataPatch.TARGET_NAME, order = 4000)
public class TemplateDataPatches implements DbDataPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("template-data-changelog.xml");
	}

}