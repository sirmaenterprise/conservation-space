/**
 *
 */
package com.sirma.sep.content;

import com.sirma.itt.seip.db.patch.DbSchemaPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Content patches provider. A plugin to provide patches for content module.
 *
 * @author BBonev
 */
@Extension(target = DbSchemaPatch.TARGET_NAME, order = 60)
public class ContentPatches implements DbSchemaPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("content-changelog.xml");
	}

}
