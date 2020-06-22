package com.sirma.itt.sep.content.extraction.patch;

import com.sirma.itt.seip.db.patch.DbCoreSchemaPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Core db patch for content extraction configuration.
 *
 * @author Boyan Tonchev.
 */
@Extension(target = DbCoreSchemaPatch.TARGET_NAME, order = 10)
public class SystemDataBasePatch implements DbCoreSchemaPatch {
	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("system-data-base-changelog.xml");
	}
}
